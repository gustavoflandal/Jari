package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** PT-03: {@link RegimentoFixtures} para os testes dos outros módulos (docs/dev/06 e 12). */
class RegimentoFixturesTest {

    @Test
    @DisplayName("PT-03: RegimentoFixtures.sp() e variações com*; violações denunciam variação inconsistente")
    void PT03_fixtures_sp_e_variacoes() {
        Regimento sp = RegimentoFixtures.sp();
        Regimento.SecaoVotacao votacao = new Regimento.SecaoVotacao(2, sp.votacao().excecaoMaioriaSimples(),
                "PARALELA", true, true);

        Regimento variacao = RegimentoFixtures.sp().comVotacao(votacao);

        assertThat(variacao.votacao().ordem()).isEqualTo("PARALELA");
        assertThat(variacao.prazos()).isEqualTo(sp.prazos());
        assertThat(sp.votacao().ordem()).isEqualTo("SEQUENCIAL");
        assertThat(RegimentoFixtures.violacoes(variacao)).isEmpty();

        Regimento inconsistente = sp.comTurmas(new Regimento.SecaoTurmas(4, true, 2, "SEM_REPETICAO_ATE_ESGOTAR", false));
        assertThat(RegimentoFixtures.violacoes(inconsistente)).anyMatch(v -> v.regra().equals("regra 3 do doc 06"));
    }

    @Test
    @DisplayName("PT-03: RegimentoFixtures.vigente() entrega RegimentoVigente com versão e hash da variação")
    void PT03_fixtures_vigente() {
        Regimento variacao = RegimentoFixtures.sp().comRetencao(new Regimento.SecaoRetencao(7));
        RegimentoVigente vigente = RegimentoFixtures.vigente(variacao);

        assertThat(vigente.regimento().retencao().anos()).isEqualTo(7);
        assertThat(vigente.versao(vigente.versao().id())).contains(vigente.versao());
        assertThat(vigente.versao().hash()).isNotEqualTo(RegimentoFixtures.vigente(RegimentoFixtures.sp()).versao().hash());
    }
}
