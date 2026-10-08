package br.com.sirej.prazos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.RegimentoFixtures;

/** Critérios de aceite do PT-05 (parte 1) contra o regimento de SP: contagem, ciência, prorrogação e alertas. */
class MotorDePrazosTest {

    private static final Regimento SP = RegimentoFixtures.sp();
    private static final Regimento.SecaoPrazos PRAZOS = SP.prazos();

    // 2026-10-09 é sexta; 10-10 sábado; 10-11 domingo; 10-12 segunda (feriado nacional); 10-13 terça.
    private static final LocalDate SEXTA = LocalDate.of(2026, 10, 9);
    private static final LocalDate SABADO = LocalDate.of(2026, 10, 10);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 10, 11);
    private static final LocalDate SEGUNDA_FERIADO = LocalDate.of(2026, 10, 12);
    private static final LocalDate TERCA = LocalDate.of(2026, 10, 13);

    private static final RegistroDeCalendario APARECIDA = RegistroDeCalendario.dia(TipoDeRegistro.FERIADO,
            Abrangencia.NACIONAL, SEGUNDA_FERIADO);

    private static MotorDePrazos motor(RegistroDeCalendario... registros) {
        return MotorDePrazos.de(SP, List.of(registros));
    }

    @Test
    @DisplayName("RN01_peca_no_ultimo_dia_e_tempestiva")
    void RN01_peca_no_ultimo_dia_e_tempestiva() {
        PrazoCalculado prazo = motor().calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 9));

        assertThat(prazo.vencimento()).isEqualTo(SEXTA);
        assertThat(prazo.prorrogado()).isFalse();
        assertThat(prazo.tempestivo(SEXTA)).isTrue();
    }

    @Test
    @DisplayName("RN01_peca_no_dia_seguinte_ao_vencimento_e_intempestiva")
    void RN01_peca_no_dia_seguinte_ao_vencimento_e_intempestiva() {
        PrazoCalculado prazo = motor().calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 9));

        assertThat(prazo.tempestivo(SEXTA.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("RN01_o_dia_da_ciencia_nao_conta")
    void RN01_o_dia_da_ciencia_nao_conta() {
        LocalDate ciencia = LocalDate.of(2026, 9, 9);

        PrazoCalculado prazo = motor().calcular(PRAZOS.recurso1a(), ciencia);

        assertThat(prazo.vencimentoBruto()).isEqualTo(ciencia.plusDays(PRAZOS.recurso1a().dias()));
    }

    @Test
    @DisplayName("RN02_sem_ciencia_efetiva_a_contagem_parte_da_inclusao_mais_o_prazo_da_ciencia_presumida")
    void RN02_sem_ciencia_efetiva_a_contagem_parte_da_inclusao_mais_o_prazo_da_ciencia_presumida() {
        LocalDate inclusao = LocalDate.of(2026, 9, 9);

        LocalDate ciencia = motor().cienciaConsiderada(Optional.empty(), inclusao,
                PRAZOS.cienciaPresumidaEletronica());

        assertThat(ciencia).isEqualTo(inclusao.plusDays(PRAZOS.cienciaPresumidaEletronica().dias()));
        assertThat(ciencia).isEqualTo(SEXTA);
    }

    @Test
    @DisplayName("RN02_ciencia_efetiva_anterior_a_presumida_prevalece")
    void RN02_ciencia_efetiva_anterior_a_presumida_prevalece() {
        LocalDate efetiva = LocalDate.of(2026, 9, 20);

        LocalDate ciencia = motor().cienciaConsiderada(Optional.of(efetiva), LocalDate.of(2026, 9, 9),
                PRAZOS.cienciaPresumidaEletronica());

        assertThat(ciencia).isEqualTo(efetiva);
    }

    @Test
    @DisplayName("RN02_ciencia_efetiva_posterior_a_presumida_nao_adia_o_prazo")
    void RN02_ciencia_efetiva_posterior_a_presumida_nao_adia_o_prazo() {
        LocalDate ciencia = motor().cienciaConsiderada(Optional.of(LocalDate.of(2026, 10, 15)),
                LocalDate.of(2026, 9, 9), PRAZOS.cienciaPresumidaEletronica());

        assertThat(ciencia).isEqualTo(SEXTA);
    }

    @Test
    @DisplayName("RN02_prazo_de_recurso_conta_da_ciencia_presumida_e_prorroga_o_vencimento")
    void RN02_prazo_de_recurso_conta_da_ciencia_presumida_e_prorroga_o_vencimento() {
        MotorDePrazos motor = motor();
        LocalDate ciencia = motor.cienciaConsiderada(Optional.empty(), LocalDate.of(2026, 9, 9),
                PRAZOS.cienciaPresumidaEletronica());

        PrazoCalculado prazo = motor.calcular(PRAZOS.recurso1a(), ciencia);

        assertThat(prazo.vencimentoBruto()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(prazo.vencimento()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(prazo.prorrogado()).isTrue();
    }

    @Test
    @DisplayName("RN04_indicacao_de_condutor_vence_no_prazo_do_regimento")
    void RN04_indicacao_de_condutor_vence_no_prazo_do_regimento() {
        LocalDate recebimentoDaNa = LocalDate.of(2026, 10, 6);

        PrazoCalculado prazo = motor().calcular(PRAZOS.indicacaoCondutor(), recebimentoDaNa);

        assertThat(prazo.vencimento()).isEqualTo(LocalDate.of(2026, 10, 21));
        assertThat(prazo.tempestivo(LocalDate.of(2026, 10, 21))).isTrue();
        assertThat(prazo.tempestivo(LocalDate.of(2026, 10, 22))).isFalse();
    }

    @Test
    @DisplayName("RN04_prazo_do_regimento_alterado_muda_o_vencimento")
    void RN04_prazo_do_regimento_alterado_muda_o_vencimento() {
        Regimento.Prazo outro = new Regimento.Prazo(PRAZOS.indicacaoCondutor().dias() + 1, null, null, null, null);

        PrazoCalculado prazo = motor().calcular(outro, LocalDate.of(2026, 10, 6));

        assertThat(prazo.vencimento()).isEqualTo(LocalDate.of(2026, 10, 22));
    }

    @Test
    @DisplayName("RN10_recurso_de_segunda_instancia_vence_no_prazo_do_regimento")
    void RN10_recurso_de_segunda_instancia_vence_no_prazo_do_regimento() {
        PrazoCalculado prazo = motor().calcular(PRAZOS.recurso2a(), LocalDate.of(2026, 9, 9));

        assertThat(prazo.vencimento()).isEqualTo(SEXTA);
        assertThat(prazo.tempestivo(SEXTA.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("RN31_vencimento_em_feriado_nacional_prorroga_para_o_proximo_dia_util")
    void RN31_vencimento_em_feriado_nacional_prorroga_para_o_proximo_dia_util() {
        PrazoCalculado prazo = motor(APARECIDA).calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 12));

        assertThat(prazo.vencimentoBruto()).isEqualTo(SEGUNDA_FERIADO);
        assertThat(prazo.vencimento()).isEqualTo(TERCA);
        assertThat(prazo.prorrogado()).isTrue();
    }

    @Test
    @DisplayName("RN31_vencimento_em_sabado_prorroga_para_segunda")
    void RN31_vencimento_em_sabado_prorroga_para_segunda() {
        PrazoCalculado prazo = motor().calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 10));

        assertThat(prazo.vencimentoBruto()).isEqualTo(SABADO);
        assertThat(prazo.vencimento()).isEqualTo(SEGUNDA_FERIADO);
        assertThat(prazo.tempestivo(SEGUNDA_FERIADO)).isTrue();
    }

    @Test
    @DisplayName("RN31_vencimento_em_domingo_antes_de_feriado_municipal_prorroga_para_depois_do_feriado")
    void RN31_vencimento_em_domingo_antes_de_feriado_municipal_prorroga_para_depois_do_feriado() {
        RegistroDeCalendario municipal = RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.MUNICIPAL,
                SEGUNDA_FERIADO);

        PrazoCalculado prazo = motor(municipal).calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 11));

        assertThat(prazo.vencimentoBruto()).isEqualTo(DOMINGO);
        assertThat(prazo.vencimento()).isEqualTo(TERCA);
    }

    @Test
    @DisplayName("RN31_feriado_de_abrangencia_fora_do_regimento_nao_prorroga")
    void RN31_feriado_de_abrangencia_fora_do_regimento_nao_prorroga() {
        Regimento soNacional = SP.comCalendario(new Regimento.SecaoCalendario(List.of("NACIONAL"), true));
        RegistroDeCalendario municipal = RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.MUNICIPAL,
                SEXTA);

        PrazoCalculado prazo = MotorDePrazos.de(soNacional, List.of(municipal)).calcular(PRAZOS.recurso1a(),
                LocalDate.of(2026, 9, 9));

        assertThat(prazo.vencimento()).isEqualTo(SEXTA);
        assertThat(prazo.prorrogado()).isFalse();
    }

    @Test
    @DisplayName("RN31_com_prorrogacao_desligada_no_regimento_o_vencimento_em_dia_nao_util_fica")
    void RN31_com_prorrogacao_desligada_no_regimento_o_vencimento_em_dia_nao_util_fica() {
        Regimento semProrrogacao = SP.comCalendario(
                new Regimento.SecaoCalendario(SP.calendario().feriados(), false));

        PrazoCalculado prazo = MotorDePrazos.de(semProrrogacao, List.of()).calcular(PRAZOS.recurso1a(),
                LocalDate.of(2026, 9, 10));

        assertThat(prazo.vencimento()).isEqualTo(SABADO);
        assertThat(prazo.prorrogado()).isFalse();
    }

    @Test
    @DisplayName("RN31_suspensao_de_expediente_no_ultimo_dia_prorroga_o_vencimento")
    void RN31_suspensao_de_expediente_no_ultimo_dia_prorroga_o_vencimento() {
        RegistroDeCalendario suspensao = RegistroDeCalendario.dia(TipoDeRegistro.SUSPENSAO_EXPEDIENTE,
                Abrangencia.ORGAO, SEXTA);

        PrazoCalculado prazo = motor(suspensao).calcular(PRAZOS.recurso1a(), LocalDate.of(2026, 9, 9));

        assertThat(prazo.vencimentoBruto()).isEqualTo(SEXTA);
        assertThat(prazo.vencimento()).isEqualTo(SEGUNDA_FERIADO);
    }

    @Test
    @DisplayName("PT05_prazo_em_sessoes_nao_se_conta_em_dias")
    void PT05_prazo_em_sessoes_nao_se_conta_em_dias() {
        assertThatThrownBy(() -> motor().calcular(PRAZOS.relatoria(), SEXTA))
                .isInstanceOf(PrazoNaoCalculavelException.class);
    }

    @Test
    @DisplayName("PT05_prazo_impresso_na_notificacao_nao_se_conta_em_dias")
    void PT05_prazo_impresso_na_notificacao_nao_se_conta_em_dias() {
        assertThatThrownBy(() -> motor().calcular(PRAZOS.defesaAutuacao(), SEXTA))
                .isInstanceOf(PrazoNaoCalculavelException.class);
    }

    @Test
    @DisplayName("PT05_meta_sem_prazo_legal_nao_se_conta_em_dias")
    void PT05_meta_sem_prazo_legal_nao_se_conta_em_dias() {
        assertThatThrownBy(() -> motor().calcular(PRAZOS.metaDefesaAutuacao(), SEXTA))
                .isInstanceOf(PrazoNaoCalculavelException.class);
    }

    @Test
    @DisplayName("PT05_prazo_com_zero_dias_nao_se_conta")
    void PT05_prazo_com_zero_dias_nao_se_conta() {
        assertThatThrownBy(() -> motor().calcular(new Regimento.Prazo(0, null, null, null, null), SEXTA))
                .isInstanceOf(PrazoNaoCalculavelException.class);
    }

    @Test
    @DisplayName("PT05_ciencia_presumida_sem_prazo_em_dias_e_recusada")
    void PT05_ciencia_presumida_sem_prazo_em_dias_e_recusada() {
        assertThatThrownBy(() -> motor().cienciaConsiderada(Optional.empty(), SEXTA, PRAZOS.relatoria()))
                .isInstanceOf(PrazoNaoCalculavelException.class);
    }

    @Test
    @DisplayName("RN37_alertas_do_regimento_caem_nos_dias_certos_antes_do_vencimento")
    void RN37_alertas_do_regimento_caem_nos_dias_certos_antes_do_vencimento() {
        LocalDate vencimento = LocalDate.of(2026, 10, 21);

        List<LocalDate> datas = MotorDePrazos.datasDeAlerta(vencimento, PRAZOS.alertas());

        assertThat(datas).containsExactly(LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 16),
                LocalDate.of(2026, 10, 20));
    }

    @Test
    @DisplayName("RN37_so_gera_alerta_no_dia_da_antecedencia")
    void RN37_so_gera_alerta_no_dia_da_antecedencia() {
        LocalDate vencimento = LocalDate.of(2026, 10, 21);
        Regimento.Alertas alertas = PRAZOS.alertas();

        assertThat(MotorDePrazos.alertasDoDia(LocalDate.of(2026, 10, 11), vencimento, alertas)).containsExactly(10);
        assertThat(MotorDePrazos.alertasDoDia(LocalDate.of(2026, 10, 16), vencimento, alertas)).containsExactly(5);
        assertThat(MotorDePrazos.alertasDoDia(LocalDate.of(2026, 10, 20), vencimento, alertas)).containsExactly(1);
        assertThat(MotorDePrazos.alertasDoDia(LocalDate.of(2026, 10, 12), vencimento, alertas)).isEmpty();
        assertThat(MotorDePrazos.alertasDoDia(vencimento, vencimento, alertas)).isEmpty();
    }

    @Test
    @DisplayName("RN37_antecedencias_vem_do_regimento_e_nao_do_codigo")
    void RN37_antecedencias_vem_do_regimento_e_nao_do_codigo() {
        LocalDate vencimento = LocalDate.of(2026, 10, 21);
        Regimento.Alertas outros = new Regimento.Alertas(List.of(3));

        assertThat(MotorDePrazos.datasDeAlerta(vencimento, outros)).containsExactly(LocalDate.of(2026, 10, 18));
        assertThat(MotorDePrazos.alertasDoDia(LocalDate.of(2026, 10, 11), vencimento, outros)).isEmpty();
    }

    @Test
    @DisplayName("PT05_calendario_de_teste_usa_todas_as_abrangencias_do_regimento_de_sp")
    void PT05_calendario_de_teste_usa_todas_as_abrangencias_do_regimento_de_sp() {
        assertThat(SP.calendario().feriados()).containsExactlyInAnyOrderElementsOf(
                EnumSet.allOf(Abrangencia.class).stream().map(Enum::name).toList());
    }
}
