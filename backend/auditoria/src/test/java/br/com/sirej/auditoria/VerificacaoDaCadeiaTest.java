package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.sirej.auditoria.aplicacao.AncoragemDiariaService;
import br.com.sirej.auditoria.aplicacao.VerificacaoDaCadeiaService;
import br.com.sirej.auditoria.dominio.JsonCanonico;
import br.com.sirej.auditoria.dominio.MotivoDaQuebra;
import br.com.sirej.auditoria.dominio.QuebraDaCadeia;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.dominio.ResultadoVerificacao;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;
import br.com.sirej.compartilhado.Hash;

/**
 * PT-04, critério "job de verificação detecta adulteração simulada": adulterações feitas direto no banco,
 * fora da aplicação, por um superusuário com os triggers desligados. A verificação aponta o ponto exato.
 */
class VerificacaoDaCadeiaTest extends TesteComBanco {

    @Autowired
    private VerificacaoDaCadeiaService verificacao;

    @Autowired
    private AncoragemDiariaService ancoragem;

    @Autowired
    private RegistroAuditoriaRepository registros;

    @Test
    @DisplayName("PT-04: cadeia sem adulteração é íntegra")
    void PT04_cadeia_sem_adulteracao_e_integra() {
        List<Long> seqs = registrarCinco();

        ResultadoVerificacao resultado = verificacao.verificar();

        assertThat(resultado.integra()).isTrue();
        assertThat(resultado.registrosVerificados()).isEqualTo(5);
        assertThat(resultado.ultimoSeqIntegro()).isEqualTo(seqs.get(4));
    }

    @Test
    @DisplayName("PT-04: UPDATE direto no banco é detectado no registro alterado")
    void PT04_update_fora_da_aplicacao_e_detectado_no_registro_alterado() throws SQLException {
        List<Long> seqs = registrarCinco();

        comoSuperusuarioSemTriggers("UPDATE auditoria.registro_auditoria SET detalhe = '{\"situacao\": \"ARQUIVADO\"}'"
                + " WHERE seq = " + seqs.get(2));

        assertThat(verificacao.verificar().quebra())
                .isEqualTo(new QuebraDaCadeia(seqs.get(2), seqs.get(1), MotivoDaQuebra.HASH_DIVERGENTE));
    }

    @Test
    @DisplayName("PT-04: DELETE direto no banco é detectado no registro seguinte ao removido")
    void PT04_delete_fora_da_aplicacao_e_detectado() throws SQLException {
        List<Long> seqs = registrarCinco();

        comoSuperusuarioSemTriggers("DELETE FROM auditoria.registro_auditoria WHERE seq = " + seqs.get(2));

        assertThat(verificacao.verificar().quebra())
                .isEqualTo(new QuebraDaCadeia(seqs.get(3), seqs.get(1), MotivoDaQuebra.ENCADEAMENTO_ROMPIDO));
    }

    @Test
    @DisplayName("PT-04: inserção no meio, mesmo com hash bem calculado, é detectada")
    void PT04_insercao_no_meio_fora_da_aplicacao_e_detectada() throws SQLException {
        RegistroAuditoriaGravado primeiro = registrarEmTransacao("ATO_DE_TESTE", "P-1");
        RegistroAuditoriaGravado segundo = registrarEmTransacao("ATO_DE_TESTE", "P-2");
        // Transação desfeita: consome um número de sequência e deixa um buraco legítimo.
        transacao.executeWithoutResult(status -> {
            trilha.registrar(new NovoRegistroAuditoria(new Ator("usuario-1", "MEMBRO"), null, "ATO_DESFEITO",
                    new AlvoAuditoria("processo", "P-X")));
            status.setRollbackOnly();
        });
        RegistroAuditoriaGravado quarto = registrarEmTransacao("ATO_DE_TESTE", "P-4");
        registrarEmTransacao("ATO_DE_TESTE", "P-5");
        assertThat(verificacao.verificar().integra()).isTrue();

        long buraco = segundo.seq() + 1;
        assertThat(quarto.seq()).isGreaterThan(buraco);
        RegistroAuditoria forjado = RegistroAuditoria.encadear(buraco, segundo.em().plusMillis(1), "usuario-2",
                "PRESIDENTE", null, "VOTO_REGISTRADO", "processo", "P-2", new TreeMap<>(), segundo.hash());
        inserirComoSuperusuario(forjado);

        ResultadoVerificacao resultado = verificacao.verificar();
        assertThat(resultado.quebra())
                .isEqualTo(new QuebraDaCadeia(quarto.seq(), buraco, MotivoDaQuebra.ENCADEAMENTO_ROMPIDO));
        assertThat(primeiro.seq()).isLessThan(buraco);
    }

    @Test
    @DisplayName("PT-04: reescrita de toda a cadeia depois de uma âncora é detectada pela âncora")
    void PT04_reescrita_da_cadeia_inteira_e_detectada_pela_ancora() throws SQLException {
        List<Long> seqs = registrarCinco();
        relogio.ajustar(INICIO.plusSeconds(86_400));
        ancoragem.ancorar(LocalDate.ofInstant(INICIO, FUSO), Ator.sistema("auditoria.ancoragem"));
        registrarEmTransacao("ATO_DE_TESTE", "P-6");

        // O DBA reescreve o registro 2 e recalcula todos os hashes seguintes: a cadeia fica coerente.
        List<RegistroAuditoria> todos = new ArrayList<>();
        transacao.executeWithoutResult(s -> registros.percorrerEmOrdem(todos::add));
        reescreverAPartirDe(todos, 1);
        assertThat(cadeiaInternamenteCoerente()).isTrue();

        assertThat(verificacao.verificar().quebra())
                .isEqualTo(new QuebraDaCadeia(seqs.get(4), seqs.get(3), MotivoDaQuebra.ANCORA_DIVERGENTE));
    }

    @Test
    @DisplayName("PT-04: remoção do fim da cadeia depois de uma âncora é detectada")
    void PT04_remocao_do_fim_depois_da_ancora_e_detectada() throws SQLException {
        List<Long> seqs = registrarCinco();
        relogio.ajustar(INICIO.plusSeconds(86_400));
        ancoragem.ancorar(LocalDate.ofInstant(INICIO, FUSO), Ator.sistema("auditoria.ancoragem"));

        comoSuperusuarioSemTriggers("DELETE FROM auditoria.registro_auditoria WHERE seq >= " + seqs.get(3));

        assertThat(verificacao.verificar().quebra())
                .isEqualTo(new QuebraDaCadeia(seqs.get(4), seqs.get(2), MotivoDaQuebra.ANCORA_SEM_REGISTRO));
    }

    @Test
    @DisplayName("PT-04: a rotina de verificação registra o resultado e o ponto de quebra na trilha")
    void PT04_rotina_registra_resultado_e_ponto_de_quebra() throws SQLException {
        List<Long> seqs = registrarCinco();
        comoSuperusuarioSemTriggers("UPDATE auditoria.registro_auditoria SET alvo_id = 'P-99' WHERE seq = "
                + seqs.get(1));

        ResultadoVerificacao resultado = verificacao.verificarERegistrar(Ator.sistema("auditoria.verificacao"));

        assertThat(resultado.integra()).isFalse();
        List<RegistroAuditoria> verificacoes = registros.porAlvo("registro_auditoria", "cadeia");
        assertThat(verificacoes).singleElement().satisfies(registro -> {
            assertThat(registro.acao()).isEqualTo(VerificacaoDaCadeiaService.ACAO);
            assertThat(registro.detalhe()).containsEntry("integra", "false")
                    .containsEntry("seq_quebra", Long.toString(seqs.get(1)))
                    .containsEntry("motivo", "HASH_DIVERGENTE");
        });
    }

    private List<Long> registrarCinco() {
        List<Long> seqs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            seqs.add(registrarEmTransacao("ATO_DE_TESTE", "P-" + i).seq());
        }
        return seqs;
    }

    private boolean cadeiaInternamenteCoerente() {
        List<RegistroAuditoria> todos = new ArrayList<>();
        transacao.executeWithoutResult(s -> registros.percorrerEmOrdem(todos::add));
        Hash anterior = RegistroAuditoria.GENESE;
        for (RegistroAuditoria registro : todos) {
            if (!registro.hashAnterior().equals(anterior) || !registro.hashRecalculado().equals(registro.hash())) {
                return false;
            }
            anterior = registro.hash();
        }
        return true;
    }

    private static void reescreverAPartirDe(List<RegistroAuditoria> todos, int indice) throws SQLException {
        try (Connection conexao = comoSuperusuario()) {
            conexao.createStatement().execute("SET session_replication_role = replica");
            Hash anterior = todos.get(indice - 1).hash();
            for (int i = indice; i < todos.size(); i++) {
                RegistroAuditoria original = todos.get(i);
                TreeMap<String, String> detalhe = new TreeMap<>(original.detalhe());
                if (i == indice) {
                    detalhe.put("situacao", "ARQUIVADO");
                }
                RegistroAuditoria novo = RegistroAuditoria.encadear(original.seq(), original.em(), original.atorId(),
                        original.atorPapel(), original.ip(), original.acao(), original.alvoTipo(), original.alvoId(),
                        detalhe, anterior);
                try (PreparedStatement sql = conexao.prepareStatement("UPDATE auditoria.registro_auditoria"
                        + " SET detalhe = ?::jsonb, hash_anterior = ?, hash = ? WHERE seq = ?")) {
                    sql.setString(1, JsonCanonico.objeto(novo.detalhe()));
                    sql.setString(2, novo.hashAnterior().hex());
                    sql.setString(3, novo.hash().hex());
                    sql.setLong(4, novo.seq());
                    sql.executeUpdate();
                }
                anterior = novo.hash();
            }
        }
    }

    private static void inserirComoSuperusuario(RegistroAuditoria registro) throws SQLException {
        try (Connection conexao = comoSuperusuario(); PreparedStatement sql = conexao.prepareStatement("""
                INSERT INTO auditoria.registro_auditoria
                    (seq, em, ator_id, ator_papel, ip, acao, alvo_tipo, alvo_id, detalhe, hash_anterior, hash)
                VALUES (?, ?, ?, ?, NULL, ?, ?, ?, ?::jsonb, ?, ?)
                """)) {
            sql.setLong(1, registro.seq());
            sql.setObject(2, registro.em().atOffset(ZoneOffset.UTC));
            sql.setString(3, registro.atorId());
            sql.setString(4, registro.atorPapel());
            sql.setString(5, registro.acao());
            sql.setString(6, registro.alvoTipo());
            sql.setString(7, registro.alvoId());
            sql.setString(8, JsonCanonico.objeto(registro.detalhe()));
            sql.setString(9, registro.hashAnterior().hex());
            sql.setString(10, registro.hash().hex());
            sql.executeUpdate();
        }
    }
}
