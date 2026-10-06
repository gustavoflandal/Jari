package br.com.sirej.prazos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.RegimentoFixtures;

/** RN31 (PT-05): dias úteis, feriado por abrangência, ponto facultativo e suspensão de expediente. */
class CalendarioDeExpedienteTest {

    private static final LocalDate TERCA = LocalDate.of(2026, 10, 6);
    private static final LocalDate SEXTA = LocalDate.of(2026, 10, 9);
    private static final LocalDate SABADO = LocalDate.of(2026, 10, 10);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 10, 11);
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 10, 12);

    private static final Set<Abrangencia> TODAS = EnumSet.allOf(Abrangencia.class);

    @Test
    @DisplayName("RN31_dia_de_semana_sem_registro_e_dia_util")
    void RN31_dia_de_semana_sem_registro_e_dia_util() {
        CalendarioDeExpediente calendario = new CalendarioDeExpediente(List.of(), TODAS);

        assertThat(calendario.ehDiaUtil(TERCA)).isTrue();
        assertThat(calendario.ehDiaUtil(SEXTA)).isTrue();
    }

    @Test
    @DisplayName("RN31_sabado_e_domingo_nao_sao_dias_uteis")
    void RN31_sabado_e_domingo_nao_sao_dias_uteis() {
        CalendarioDeExpediente calendario = new CalendarioDeExpediente(List.of(), TODAS);

        assertThat(calendario.ehDiaUtil(SABADO)).isFalse();
        assertThat(calendario.ehDiaUtil(DOMINGO)).isFalse();
    }

    @Test
    @DisplayName("RN31_feriado_de_cada_abrangencia_considerada_nao_e_dia_util")
    void RN31_feriado_de_cada_abrangencia_considerada_nao_e_dia_util() {
        for (Abrangencia abrangencia : Abrangencia.values()) {
            CalendarioDeExpediente calendario = new CalendarioDeExpediente(
                    List.of(RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, abrangencia, SEGUNDA)), TODAS);

            assertThat(calendario.ehDiaUtil(SEGUNDA)).as("feriado %s", abrangencia).isFalse();
        }
    }

    @Test
    @DisplayName("RN31_feriado_de_abrangencia_fora_da_lista_do_regimento_e_ignorado")
    void RN31_feriado_de_abrangencia_fora_da_lista_do_regimento_e_ignorado() {
        List<RegistroDeCalendario> registros = List
                .of(RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.MUNICIPAL, SEGUNDA));

        CalendarioDeExpediente semMunicipal = new CalendarioDeExpediente(registros,
                EnumSet.of(Abrangencia.NACIONAL, Abrangencia.ESTADUAL, Abrangencia.ORGAO));
        CalendarioDeExpediente comMunicipal = new CalendarioDeExpediente(registros, TODAS);

        assertThat(semMunicipal.ehDiaUtil(SEGUNDA)).isTrue();
        assertThat(comMunicipal.ehDiaUtil(SEGUNDA)).isFalse();
    }

    @Test
    @DisplayName("RN31_ponto_facultativo_do_orgao_nao_e_dia_util")
    void RN31_ponto_facultativo_do_orgao_nao_e_dia_util() {
        CalendarioDeExpediente calendario = new CalendarioDeExpediente(
                List.of(RegistroDeCalendario.dia(TipoDeRegistro.PONTO_FACULTATIVO, Abrangencia.ORGAO, SEXTA)), TODAS);

        assertThat(calendario.ehDiaUtil(SEXTA)).isFalse();
    }

    @Test
    @DisplayName("RN31_suspensao_de_expediente_cobre_o_intervalo_e_vale_em_qualquer_abrangencia")
    void RN31_suspensao_de_expediente_cobre_o_intervalo_e_vale_em_qualquer_abrangencia() {
        RegistroDeCalendario suspensao = new RegistroDeCalendario(TipoDeRegistro.SUSPENSAO_EXPEDIENTE,
                Abrangencia.MUNICIPAL, TERCA, TERCA.plusDays(2));

        CalendarioDeExpediente calendario = new CalendarioDeExpediente(List.of(suspensao),
                EnumSet.noneOf(Abrangencia.class));

        assertThat(calendario.ehDiaUtil(TERCA)).isFalse();
        assertThat(calendario.ehDiaUtil(TERCA.plusDays(1))).isFalse();
        assertThat(calendario.ehDiaUtil(TERCA.plusDays(2))).isFalse();
        assertThat(calendario.ehDiaUtil(SEXTA)).isTrue();
        assertThat(calendario.ehDiaUtil(TERCA.minusDays(1))).isTrue();
    }

    @Test
    @DisplayName("RN31_proximo_dia_util_de_dia_util_e_o_proprio_dia")
    void RN31_proximo_dia_util_de_dia_util_e_o_proprio_dia() {
        CalendarioDeExpediente calendario = new CalendarioDeExpediente(List.of(), TODAS);

        assertThat(calendario.proximoDiaUtil(TERCA)).isEqualTo(TERCA);
    }

    @Test
    @DisplayName("RN31_proximo_dia_util_pula_fim_de_semana_e_feriado_seguido")
    void RN31_proximo_dia_util_pula_fim_de_semana_e_feriado_seguido() {
        CalendarioDeExpediente calendario = new CalendarioDeExpediente(
                List.of(RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.NACIONAL, SEGUNDA),
                        RegistroDeCalendario.dia(TipoDeRegistro.PONTO_FACULTATIVO, Abrangencia.ORGAO,
                                SEGUNDA.plusDays(1))),
                TODAS);

        assertThat(calendario.proximoDiaUtil(SABADO)).isEqualTo(SEGUNDA.plusDays(2));
    }

    @Test
    @DisplayName("RN31_calendario_do_regimento_considera_as_abrangencias_de_calendario_feriados")
    void RN31_calendario_do_regimento_considera_as_abrangencias_de_calendario_feriados() {
        Regimento regimento = RegimentoFixtures.sp().comCalendario(new Regimento.SecaoCalendario(List.of("NACIONAL"), true));
        List<RegistroDeCalendario> registros = List.of(
                RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.NACIONAL, SEGUNDA),
                RegistroDeCalendario.dia(TipoDeRegistro.FERIADO, Abrangencia.MUNICIPAL, SEXTA));

        CalendarioDeExpediente calendario = CalendarioDeExpediente.de(regimento.calendario(), registros);

        assertThat(calendario.ehDiaUtil(SEGUNDA)).isFalse();
        assertThat(calendario.ehDiaUtil(SEXTA)).isTrue();
    }

    @Test
    @DisplayName("RN31_registro_com_fim_antes_do_inicio_e_recusado")
    void RN31_registro_com_fim_antes_do_inicio_e_recusado() {
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException().isThrownBy(
                () -> new RegistroDeCalendario(TipoDeRegistro.FERIADO, Abrangencia.NACIONAL, SEGUNDA, TERCA));
    }

    @Test
    @DisplayName("RN31_registro_sem_tipo_e_recusado")
    void RN31_registro_sem_tipo_e_recusado() {
        assertThatNullPointerException().isThrownBy(
                () -> RegistroDeCalendario.dia(null, Abrangencia.NACIONAL, SEGUNDA));
    }
}
