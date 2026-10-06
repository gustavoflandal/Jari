package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;

/**
 * PT-03: as 10 regras de consistência do doc 06, cada uma com teste de falha (a mensagem cita a regra) e teste
 * positivo. As variações herdam de {@code sp} e sobrescrevem só o que a regra exercita.
 */
class RegrasDeConsistenciaTest {

    static Regimento ler(String yaml, boolean cofre) {
        return RegimentoFixtures.leitor(Map.of("teste.yaml", yaml)).ler("teste", cofre).regimento();
    }

    static Regimento ler(String yaml) {
        return ler(yaml, true);
    }

    /** Lê esperando reprovação e devolve as violações; a mensagem precisa citar a regra. */
    static List<Violacao> reprovar(String yaml, boolean cofre, int regra) {
        String citacao = "regra " + regra + " do doc 06";
        RegimentoInvalidoException erro = (RegimentoInvalidoException) org.assertj.core.api.Assertions
                .catchThrowable(() -> ler(yaml, cofre));
        assertThat(erro).as("regimento deveria ser reprovado pela " + citacao).isNotNull();
        assertThat(erro.getMessage()).contains(citacao);
        assertThat(erro.violacoes()).anyMatch(v -> v.regra().equals(citacao));
        return erro.violacoes();
    }

    static List<Violacao> reprovar(String yaml, int regra) {
        return reprovar(yaml, true, regra);
    }

    @Test
    @DisplayName("PT-03: sp.yaml passa por todas as regras de consistência")
    void PT03_sp_passa_por_todas_as_regras() {
        assertThat(RegimentoFixtures.violacoes(RegimentoFixtures.sp())).isEmpty();
        assertThat(RegimentoFixtures.violacoes(RegimentoFixtures.curitiba())).isEmpty();
    }

    @Nested
    @DisplayName("Regra 1: distribuicao.falhaFechada só true (invariante 2, RN24)")
    class Regra1 {

        @Test
        void RN24_regra1_falha_fechada_false_reprovada() {
            reprovar("""
                    herda: sp
                    distribuicao:
                      falhaFechada: false
                    """, 1);
        }

        @Test
        void RN24_regra1_falha_fechada_true_aceita() {
            assertThat(ler("""
                    herda: sp
                    distribuicao:
                      falhaFechada: true
                    """).distribuicao().falhaFechada()).isTrue();
        }
    }

    @Nested
    @DisplayName("Regra 2: resultados não vazio, códigos únicos, ao menos um altera a penalidade (RN26)")
    class Regra2 {

        @Test
        void RN26_regra2_rol_vazio_reprovado() {
            reprovar("""
                    herda: sp
                    resultados: []
                    """, 2);
        }

        @Test
        void RN26_regra2_codigo_repetido_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    resultados:
                      - { codigo: MANTIDO, alteraPenalidade: false }
                      - { codigo: MANTIDO, alteraPenalidade: true }
                    """, 2)).anyMatch(v -> v.mensagem().contains("repetido"));
        }

        @Test
        void RN26_regra2_nenhum_resultado_altera_penalidade_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    resultados:
                      - { codigo: MANTIDO, alteraPenalidade: false }
                    """, 2)).anyMatch(v -> v.mensagem().contains("alteraPenalidade"));
        }

        @Test
        void RN26_regra2_outro_rol_fechado_aceito() {
            Regimento r = ler("""
                    herda: sp
                    resultados:
                      - { codigo: DEFERIDO, alteraPenalidade: true }
                      - { codigo: INDEFERIDO, alteraPenalidade: false }
                    """);
            assertThat(r.resultados()).extracting(Regimento.Resultado::codigo).containsExactly("DEFERIDO", "INDEFERIDO");
            assertThat(r.resultado("CANCELAMENTO_PENALIDADE")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Regra 3: turma ímpar e um por segmento (RN21)")
    class Regra3 {

        @Test
        void RN21_regra3_turma_par_reprovada() {
            assertThat(reprovar("""
                    herda: sp
                    turmas:
                      membrosPorTurma: 4
                      umPorSegmento: false
                    votacao:
                      votosMinimos: 3
                    """, 3)).anyMatch(v -> v.mensagem().contains("ímpar"));
        }

        @Test
        void RN21_regra3_um_por_segmento_com_tamanho_diferente_dos_segmentos_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    turmas:
                      membrosPorTurma: 5
                      umPorSegmento: true
                    """, 3)).anyMatch(v -> v.mensagem().contains("segmento"));
        }

        @Test
        void RN21_regra3_turma_de_cinco_sem_um_por_segmento_aceita() {
            Regimento r = ler("""
                    herda: sp
                    turmas:
                      membrosPorTurma: 5
                      umPorSegmento: false
                    """);
            assertThat(r.turmas().membrosPorTurma()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Regra 4: votos mínimos ≤ membros por turma; exceção de maioria simples ≥ 2 (RN07, RN14)")
    class Regra4 {

        @Test
        void RN14_regra4_votos_minimos_acima_da_turma_reprovado() {
            reprovar("""
                    herda: sp
                    votacao:
                      votosMinimos: 4
                    """, 4);
        }

        @Test
        void RN14_regra4_excecao_com_minimo_1_reprovada() {
            reprovar("""
                    herda: sp
                    votacao:
                      excecaoMaioriaSimples: { permitida: true, minimo: 1, exigePresidenteOuVice: true }
                    """, 4);
        }

        @Test
        void RN14_regra4_votos_minimos_e_excecao_no_limite_aceitos() {
            Regimento r = ler("""
                    herda: sp
                    votacao:
                      votosMinimos: 2
                      excecaoMaioriaSimples: { permitida: false, minimo: 2, exigePresidenteOuVice: false }
                    """);
            assertThat(r.votacao().votosMinimos()).isEqualTo(2);
            assertThat(r.votacao().excecaoMaioriaSimples().permitida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Regra 5: posições ≥ membros por turma, segmento declarado, letra única (RN21)")
    class Regra5 {

        @Test
        void RN21_regra5_menos_posicoes_que_membros_reprovado() {
            reprovar("""
                    herda: sp
                    composicao:
                      posicoesPorJunta:
                        - { letra: A, segmento: COMUNIDADE }
                        - { letra: B, segmento: SOCIEDADE_CIVIL }
                    """, 5);
        }

        @Test
        void RN21_regra5_segmento_nao_declarado_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    composicao:
                      posicoesPorJunta:
                        - { letra: A, segmento: COMUNIDADE }
                        - { letra: B, segmento: ENTIDADE_EXECUTIVA }
                        - { letra: C, segmento: OUTRO_SEGMENTO }
                    """, 5)).anyMatch(v -> v.mensagem().contains("OUTRO_SEGMENTO"));
        }

        @Test
        void RN21_regra5_letra_repetida_reprovada() {
            assertThat(reprovar("""
                    herda: sp
                    composicao:
                      posicoesPorJunta:
                        - { letra: A, segmento: COMUNIDADE }
                        - { letra: A, segmento: ENTIDADE_EXECUTIVA }
                        - { letra: C, segmento: SOCIEDADE_CIVIL }
                    """, 5)).anyMatch(v -> v.mensagem().contains("repetida"));
        }

        @Test
        void RN21_regra5_tres_posicoes_uma_por_segmento_aceitas() {
            Regimento r = ler("""
                    herda: sp
                    composicao:
                      posicoesPorJunta:
                        - { letra: X, segmento: COMUNIDADE }
                        - { letra: Y, segmento: ENTIDADE_EXECUTIVA }
                        - { letra: Z, segmento: SOCIEDADE_CIVIL }
                    """);
            assertThat(r.composicao().posicoesPorJunta()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Regra 6: prazo com dias > 0 ou origem explícita; null só para metas (RN01, RN02)")
    class Regra6 {

        @Test
        void RN01_regra6_prazo_com_zero_dias_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    prazos:
                      recurso1a: { dias: 0, conta: CIENCIA_NP }
                    """, 6)).anyMatch(v -> v.caminho().equals("prazos.recurso1a.dias"));
        }

        @Test
        void RN01_regra6_prazo_legal_com_dias_null_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    prazos:
                      exigencia: { dias: null }
                    """, 6)).anyMatch(v -> v.caminho().equals("prazos.exigencia"));
        }

        @Test
        void RN01_regra6_prazo_em_sessoes_zero_reprovado() {
            reprovar("""
                    herda: sp
                    prazos:
                      relatoria: { sessoes: 0 }
                    """, 6);
        }

        @Test
        void RN01_regra6_alerta_nao_positivo_reprovado() {
            reprovar("""
                    herda: sp
                    prazos:
                      alertas: { diasAntes: [5, 0] }
                    """, 6);
        }

        @Test
        void RN01_regra6_meta_sem_prazo_origem_explicita_e_outros_dias_aceitos() {
            Regimento r = ler("""
                    herda: sp
                    prazos:
                      recurso1a: { dias: 20, conta: CIENCIA_NP }
                      exigencia: { dias: 7 }
                    """);
            assertThat(r.prazos().recurso1a().dias()).isEqualTo(20);
            assertThat(r.prazos().exigencia().dias()).isEqualTo(7);
            assertThat(r.prazos().metaDefesaAutuacao().dias()).isNull();
            assertThat(r.prazos().defesaAutuacao().origem()).isEqualTo("IMPRESSO_NA");
        }
    }

    @Nested
    @DisplayName("Regra 7: modo SIGILOSO exige cofre de chaves na instalação (RN20)")
    class Regra7 {

        @Test
        void RN20_regra7_sigiloso_sem_cofre_reprovado() {
            reprovar("""
                    herda: sp
                    designacao:
                      modo: SIGILOSO
                    """, false, 7);
        }

        @Test
        void RN20_regra7_sigiloso_com_cofre_aceito() {
            assertThat(ler("herda: sp\n", true).designacao().modo()).isEqualTo(ModoDesignacao.SIGILOSO);
        }

        @Test
        void RN20_regra7_aberto_sem_cofre_aceito() {
            assertThat(ler("""
                    herda: sp
                    designacao:
                      modo: ABERTO
                    """, false).designacao().modo()).isEqualTo(ModoDesignacao.ABERTO);
        }
    }

    @Nested
    @DisplayName("Regra 8: nenhuma chave de voto de qualidade, peso ou desempate (invariante 3, RN14)")
    class Regra8 {

        @Test
        void RN14_regra8_voto_de_qualidade_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    votacao:
                      votoDeQualidade: PRESIDENTE
                    """, 8)).anyMatch(v -> v.caminho().equals("votacao.votoDeQualidade"));
        }

        @Test
        void RN14_regra8_peso_de_voto_em_outra_secao_reprovado() {
            reprovar("""
                    herda: sp
                    turmas:
                      pesoDoVotoDoPresidente: 2
                    """, 8);
        }

        @Test
        void RN14_regra8_desempate_dentro_de_lista_reprovado() {
            assertThat(reprovar("""
                    herda: sp
                    resultados:
                      - { codigo: MANTIDO, alteraPenalidade: false }
                      - { codigo: CANCELADO, alteraPenalidade: true, criterioDeDesempate: MINERVA }
                    """, 8)).anyMatch(v -> v.caminho().equals("resultados[1].criterioDeDesempate"));
        }

        @Test
        void RN14_regra8_votacao_sem_chave_proibida_aceita() {
            assertThat(ler("herda: sp\n").votacao().ordem()).isEqualTo("SEQUENCIAL");
        }
    }

    @Nested
    @DisplayName("Regra 9: todo tipo tem aprovador; ADMIN não aprova REGIMENTO (RN38)")
    class Regra9 {

        @Test
        void RN38_regra9_admin_aprovando_regimento_reprovado() {
            reprovar("""
                    herda: sp
                    administracao:
                      aprovadores:
                        REGIMENTO: [ADMIN]
                    """, 9);
        }

        @Test
        void RN38_regra9_tipo_sem_aprovador_reprovado() {
            reprovar("""
                    herda: sp
                    administracao:
                      aprovadores:
                        CALENDARIO: []
                    """, 9);
        }

        @Test
        void RN38_regra9_admin_aprovando_importacao_reprovado_D50() {
            reprovar("""
                    herda: sp
                    administracao:
                      aprovadores:
                        IMPORTACAO: [COORDENADOR, ADMIN]
                    """, 9);
        }

        @Test
        void RN38_regra9_outros_aprovadores_aceitos() {
            Regimento r = ler("""
                    herda: sp
                    administracao:
                      aprovadores:
                        CALENDARIO: [COORDENADOR, SECRETARIA]
                    """);
            assertThat(r.administracao().aprovadores().get("CALENDARIO")).containsExactly("COORDENADOR", "SECRETARIA");
            assertThat(r.administracao().aprovadores().get("REGIMENTO")).containsExactly("COORDENADOR");
        }
    }

    @Nested
    @DisplayName("Regra 10: guarda total ≥ retenção; eliminação física só false (RN44, D-19)")
    class Regra10 {

        @Test
        void RN44_regra10_guarda_menor_que_retencao_reprovada() {
            reprovar("""
                    herda: sp
                    temporalidade:
                      classes:
                        - { codigo: PROCESSO_RECURSO, correnteAnos: 1, intermediariaAnos: 3, destinacao: ELIMINACAO }
                    """, 10);
        }

        @Test
        void RN44_regra10_eliminacao_fisica_reprovada() {
            reprovar("""
                    herda: sp
                    temporalidade:
                      eliminacaoFisica: true
                    """, 10);
        }

        @Test
        void RN44_regra10_retencao_menor_com_guarda_suficiente_aceita() {
            Regimento r = ler("""
                    herda: sp
                    retencao:
                      anos: 3
                    temporalidade:
                      classes:
                        - { codigo: PROCESSO_RECURSO, correnteAnos: 1, intermediariaAnos: 2, destinacao: ELIMINACAO }
                    """);
            assertThat(r.temporalidade().classes()).singleElement()
                    .extracting(Regimento.ClasseTemporalidade::guardaTotalAnos).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("PT-03: a mensagem de reprovação traz caminho e explicação, sem dado de processo")
    void PT03_mensagem_de_reprovacao_cita_regra_e_caminho() {
        assertThatThrownBy(() -> ler("""
                herda: sp
                turmas:
                  membrosPorTurma: 4
                """))
                .isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("Regimento 'teste' inválido")
                .hasMessageContaining("[regra 3 do doc 06] turmas.membrosPorTurma");
    }
}
