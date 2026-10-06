package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import apoioteste.configuracao.ApoioDeTeste;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Relogio;
import br.com.sirej.configuracao.aplicacao.CargaDoRegimento;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento;
import br.com.sirej.configuracao.infraestrutura.RegimentoVersaoRepository;

/**
 * PT-03: {@code regimento_versao} imutável com hash, em PostgreSQL 17 (Testcontainers), e {@link RegimentoVigente}
 * carregado na subida.
 */
@SpringBootTest(classes = {AplicacaoDeTeste.class, ApoioDeTeste.Banco.class, ApoioDeTeste.CofrePresente.class,
        ApoioDeTeste.Ouvinte.class}, properties = "sirej.regimento=sp")
class PersistenciaDoRegimentoTest {

    @Autowired
    RegimentoVigente vigente;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    ApoioDeTeste.EventosRegistrados eventos;

    @Autowired
    LeitorDeRegimento leitor;

    @Autowired
    RegimentoVersaoRepository repositorio;

    @Autowired
    TrilhaAuditoria trilha;

    @Autowired
    TransactionTemplate transacao;

    @Autowired
    ApplicationEventPublisher publicador;

    @Autowired
    Relogio relogio;

    @Autowired
    ObjectProvider<VerificadorCofreChaves> cofres;

    private long linhas() {
        return jdbc.sql("SELECT count(*) FROM configuracao.regimento_versao").query(Long.class).single();
    }

    private long registrosDeCriacao() {
        return jdbc.sql("SELECT count(*) FROM auditoria.registro_auditoria WHERE acao = 'REGIMENTO_VERSAO_CRIADA'")
                .query(Long.class).single();
    }

    private CargaDoRegimento carga(String nome) {
        return new CargaDoRegimento(leitor, repositorio, trilha, transacao, publicador, relogio, cofres, nome);
    }

    @Test
    @DisplayName("PT-03: a primeira subida grava a versão 1 com o SHA-256 do conteúdo canônico, pelo INSTALADOR")
    void PT03_primeira_subida_grava_versao_com_hash() {
        Map<String, Object> linha = jdbc.sql("SELECT id, conteudo::text AS conteudo, hash, aplicado_por, criado_por"
                + " FROM configuracao.regimento_versao").query().singleRow();

        Hash esperado = RegimentoFixtures.lido("sp").hash();
        assertThat(linhas()).isEqualTo(1);
        assertThat(linha.get("hash")).isEqualTo(esperado.hex());
        assertThat(linha.get("aplicado_por")).isEqualTo("INSTALADOR");
        assertThat(linha.get("criado_por")).isEqualTo("INSTALADOR");
        assertThat(vigente.versao().id()).isEqualTo(linha.get("id"));
        assertThat(vigente.versao().hash()).isEqualTo(esperado);
        assertThat(vigente.regimento()).isEqualTo(RegimentoFixtures.sp());
        assertThat(vigente.versao(vigente.versao().id())).contains(vigente.versao());

        // o conteúdo gravado (jsonb) recanonizado dá o mesmo hash
        String gravado = (String) linha.get("conteudo");
        String recanonizado = leitor.lerConteudoCanonico("sp", gravado, true).conteudoCanonico();
        assertThat(Hash.sha256(recanonizado.getBytes(StandardCharsets.UTF_8))).isEqualTo(esperado);
    }

    @Test
    @DisplayName("PT-03: a gravação publica RegimentoVersaoPublicada dentro da transação")
    void PT03_gravacao_publica_evento_na_mesma_transacao() {
        assertThat(eventos.eventos).singleElement().satisfies(e -> {
            assertThat(e.configVersao()).isEqualTo(vigente.versao().id());
            assertThat(e.hash()).isEqualTo(vigente.versao().hash());
            assertThat(e.ator()).isEqualTo("INSTALADOR");
        });
        assertThat(eventos.emTransacao).containsExactly(true);
    }

    @Test
    @DisplayName("D-48: a criação da versão gera registro de auditoria (ação, alvo, INSTALADOR e hash), sem dado pessoal")
    void D48_criacao_da_versao_registrada_na_trilha() {
        Map<String, Object> registro = jdbc.sql("""
                SELECT ator_id, ator_papel, ip, alvo_tipo, alvo_id, detalhe ->> 'hash' AS hash,
                       detalhe ->> 'regimento' AS regimento
                  FROM auditoria.registro_auditoria WHERE acao = 'REGIMENTO_VERSAO_CRIADA'
                """).query().singleRow();

        assertThat(registro.get("ator_id")).isEqualTo("INSTALADOR");
        assertThat(registro.get("ator_papel")).isEqualTo("SISTEMA");
        assertThat(registro.get("ip")).isNull();
        assertThat(registro.get("alvo_tipo")).isEqualTo("regimento_versao");
        assertThat(registro.get("alvo_id")).isEqualTo(vigente.versao().id().toString());
        assertThat(registro.get("hash")).isEqualTo(vigente.versao().hash().hex());
        assertThat(registro.get("regimento")).isEqualTo("sp");
    }

    @Test
    @DisplayName("PT-03: conteúdo inalterado reaproveita a versão existente")
    void PT03_conteudo_inalterado_reaproveita_versao() {
        CargaDoRegimento outraSubida = carga("sp");
        outraSubida.carregar();

        assertThat(linhas()).isEqualTo(1);
        assertThat(outraSubida.versao().id()).isEqualTo(vigente.versao().id());
        assertThat(registrosDeCriacao()).as("reaproveitar não cria versão nem registro").isEqualTo(1);
    }

    @Test
    @DisplayName("PT-03: YAML do pacote divergente não é aplicado sozinho; o banco prevalece (ADR-0011)")
    void PT03_yaml_divergente_nao_e_aplicado() {
        CargaDoRegimento subidaComOutroYaml = carga("curitiba");
        subidaComOutroYaml.carregar();

        assertThat(linhas()).isEqualTo(1);
        assertThat(subidaComOutroYaml.versao().hash()).isEqualTo(RegimentoFixtures.lido("sp").hash());
        assertThat(subidaComOutroYaml.regimento().orgao().codigo()).isEqualTo("CET-SP");
    }

    @Test
    @DisplayName("PT-03: UPDATE em regimento_versao é recusado pelo banco (invariante 5)")
    void PT03_update_recusado_no_banco() {
        assertThatThrownBy(() -> jdbc.sql("UPDATE configuracao.regimento_versao SET aplicado_por = 'X'").update())
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("tabela imutável");
        assertThat(jdbc.sql("SELECT aplicado_por FROM configuracao.regimento_versao").query(String.class).single())
                .isEqualTo("INSTALADOR");
    }

    @Test
    @DisplayName("PT-03: DELETE e TRUNCATE em regimento_versao são recusados pelo banco (invariante 5)")
    void PT03_delete_e_truncate_recusados_no_banco() {
        assertThatThrownBy(() -> jdbc.sql("DELETE FROM configuracao.regimento_versao").update())
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("tabela imutável");
        assertThatThrownBy(() -> jdbc.sql("TRUNCATE configuracao.regimento_versao").update())
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("tabela imutável");
        assertThat(linhas()).isEqualTo(1);
    }

    @Test
    @DisplayName("PT-03: o papel de banco da aplicação só lê e inclui em regimento_versao")
    void PT03_papel_da_aplicacao_so_le_e_inclui() {
        List<Boolean> privilegios = jdbc.sql("""
                SELECT has_table_privilege('sirej_aplicacao', 'configuracao.regimento_versao', p)
                  FROM unnest(ARRAY['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE']) AS p
                """).query(Boolean.class).list();
        assertThat(privilegios).containsExactly(true, true, false, false, false);
    }
}
