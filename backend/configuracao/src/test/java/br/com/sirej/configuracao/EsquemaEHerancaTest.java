package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** PT-03: JSON Schema e carga de {@code sp.yaml} e {@code curitiba.yaml} com {@code herda}. */
class EsquemaEHerancaTest {

    private static Regimento ler(Map<String, String> arquivos, String nome) {
        return RegimentoFixtures.leitor(arquivos).ler(nome, true).regimento();
    }

    @Test
    @DisplayName("PT-03: o JSON Schema existe em config/regimentos/esquema.json e é draft 2020-12")
    void PT03_esquema_json_existe() throws IOException {
        Path esquema = RaizDoRepositorio.caminho("config/regimentos/esquema.json");
        assertThat(Files.readString(esquema)).contains("https://json-schema.org/draft/2020-12/schema");
    }

    @Test
    @DisplayName("PT-03: sp.yaml é aceito pelo esquema e vira o regimento tipado")
    void PT03_sp_yaml_aceito_e_tipado() {
        Regimento sp = RegimentoFixtures.sp();
        assertThat(sp.orgao().codigo()).isEqualTo("CET-SP");
        assertThat(sp.prazos().recurso1a().dias()).isEqualTo(30);
        assertThat(sp.composicao().posicoesPorJunta()).hasSize(6);
        assertThat(sp.resultados()).hasSize(5);
        assertThat(sp.designacao().modo()).isEqualTo(ModoDesignacao.SIGILOSO);
        assertThat(sp.autos().acessoMembrosForaDaSessao())
                .isEqualTo(AcessoAutosForaDaSessao.MEDIANTE_AUTORIZACAO_COORDENADOR);
        assertThat(sp.pecas()).containsKey("RECURSO_1A_INSTANCIA");
        assertThat(sp.pecas().get("JUNTADA_DOCUMENTOS").decisor()).isNull();
    }

    @Test
    @DisplayName("PT-03: curitiba.yaml herda de sp e sobrescreve só o que declara")
    void PT03_curitiba_herda_de_sp_e_sobrescreve() {
        Regimento sp = RegimentoFixtures.sp();
        Regimento curitiba = RegimentoFixtures.curitiba();

        // sobrescrito
        assertThat(curitiba.orgao().codigo()).isEqualTo("SMDT-CURITIBA");
        assertThat(curitiba.orgao().uf()).isEqualTo("PR");
        assertThat(curitiba.identidade().provedoresCidadao()).containsExactly("GOVBR", "IDENTIDADE_PR");
        assertThat(curitiba.composicao().suplentesPorSegmento()).isEqualTo(1);
        // herdado dentro de seção sobrescrita (mescla de objetos)
        assertThat(curitiba.composicao().segmentos()).isEqualTo(sp.composicao().segmentos());
        assertThat(curitiba.composicao().posicoesPorJunta()).isEqualTo(sp.composicao().posicoesPorJunta());
        assertThat(curitiba.identidade().nivelMinimoPorAto()).isEqualTo(sp.identidade().nivelMinimoPorAto());
        // herdado por inteiro
        assertThat(curitiba.prazos()).isEqualTo(sp.prazos());
        assertThat(curitiba.resultados()).isEqualTo(sp.resultados());
        assertThat(curitiba.votacao()).isEqualTo(sp.votacao());
        // hash diferente: o conteúdo resolvido difere
        assertThat(RegimentoFixtures.lido("curitiba").hash()).isNotEqualTo(RegimentoFixtures.lido("sp").hash());
    }

    @Test
    @DisplayName("PT-03: herança em vários níveis; lista do filho substitui a do pai (D-51)")
    void PT03_heranca_em_varios_niveis_e_lista_substituida() {
        Regimento neto = ler(Map.of(
                "filho.yaml", """
                        herda: sp
                        prazos:
                          exigencia: { dias: 12 }
                        resultados:
                          - { codigo: DEFERIDO, alteraPenalidade: true }
                        """,
                "neto.yaml", """
                        herda: filho
                        prazos:
                          informacaoAgente: { dias: 8 }
                        """), "neto");
        assertThat(neto.prazos().exigencia().dias()).isEqualTo(12);
        assertThat(neto.prazos().informacaoAgente().dias()).isEqualTo(8);
        assertThat(neto.prazos().recurso1a().dias()).isEqualTo(30);
        assertThat(neto.resultados()).extracting(Regimento.Resultado::codigo).containsExactly("DEFERIDO");
    }

    @Test
    @DisplayName("PT-03: herança cíclica é reprovada")
    void PT03_heranca_ciclica_reprovada() {
        assertThatThrownBy(() -> ler(Map.of("a.yaml", "herda: b\n", "b.yaml", "herda: a\n"), "a"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("herança cíclica: a -> b -> a");
    }

    @Test
    @DisplayName("PT-03: herança de regimento inexistente é reprovada")
    void PT03_heranca_de_inexistente_reprovada() {
        assertThatThrownBy(() -> ler(Map.of("a.yaml", "herda: nao_existe\n"), "a"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("'nao_existe' não encontrado");
    }

    @Test
    @DisplayName("PT-03: o esquema recusa chave desconhecida (parâmetro novo só pelo doc 06)")
    void PT03_esquema_recusa_chave_desconhecida() {
        assertThatThrownBy(() -> ler(Map.of("t.yaml", """
                herda: sp
                turmas:
                  tamanhoDaSala: 10
                """), "t"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("[esquema]")
                .hasMessageContaining("tamanhoDaSala");
    }

    @Test
    @DisplayName("PT-03: o esquema recusa seção ausente")
    void PT03_esquema_recusa_secao_ausente() {
        assertThatThrownBy(() -> ler(Map.of("t.yaml", """
                orgao: { codigo: X, nome: X, esfera: MUNICIPAL, uf: SP, municipioIbge: 3550308,
                         fusoHorario: America/Sao_Paulo, segundaInstancia: X }
                """), "t"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("[esquema]")
                .hasMessageContaining("resultados");
    }

    @Test
    @DisplayName("PT-03: o esquema recusa valor fora do vocabulário (designacao.modo)")
    void PT03_esquema_recusa_valor_fora_do_enum() {
        assertThatThrownBy(() -> ler(Map.of("t.yaml", """
                herda: sp
                designacao:
                  modo: MISTO
                """), "t"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("[esquema] designacao.modo");
    }

    @Test
    @DisplayName("PT-03: YAML com chave repetida é reprovado")
    void PT03_yaml_com_chave_repetida_reprovado() {
        assertThatThrownBy(() -> ler(Map.of("t.yaml", """
                herda: sp
                retencao:
                  anos: 5
                retencao:
                  anos: 1
                """), "t"))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("chave repetida");
    }
}
