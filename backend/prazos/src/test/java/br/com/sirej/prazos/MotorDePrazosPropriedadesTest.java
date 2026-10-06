package br.com.sirej.prazos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.RegimentoFixtures;

/**
 * Propriedades do calendário e da contagem (PT-05; docs/dev/12, "Propriedade"). Exploram todas as datas de iniciais
 * de três anos contra calendários sorteados com semente fixa, para o resultado ser reproduzível. A biblioteca de
 * propriedades do projeto (jqwik) ainda não está no build (D-54); estes testes passam a jqwik quando ela entrar.
 */
class MotorDePrazosPropriedadesTest {

    private static final Regimento SP = RegimentoFixtures.sp();
    private static final LocalDate PRIMEIRO = LocalDate.of(2026, 1, 1);
    private static final LocalDate ULTIMO = LocalDate.of(2028, 12, 31);
    private static final long SEMENTE = 20261006L;
    private static final int CALENDARIOS = 25;
    private static final int REGISTROS_POR_CALENDARIO = 60;
    private static final int DIAS_DE_SORTEIO = 1100;
    private static final int DIAS_MAXIMOS_DE_SUSPENSAO = 5;

    private static List<CalendarioDeExpediente> calendariosSorteados() {
        Random sorteio = new Random(SEMENTE);
        List<CalendarioDeExpediente> calendarios = new ArrayList<>();
        calendarios.add(new CalendarioDeExpediente(List.of(), EnumSet.allOf(Abrangencia.class)));
        for (int c = 0; c < CALENDARIOS; c++) {
            List<RegistroDeCalendario> registros = new ArrayList<>();
            for (int r = 0; r < REGISTROS_POR_CALENDARIO; r++) {
                LocalDate inicio = PRIMEIRO.plusDays(sorteio.nextInt(DIAS_DE_SORTEIO));
                LocalDate fim = inicio.plusDays(sorteio.nextInt(DIAS_MAXIMOS_DE_SUSPENSAO));
                TipoDeRegistro tipo = TipoDeRegistro.values()[sorteio.nextInt(TipoDeRegistro.values().length)];
                Abrangencia abrangencia = Abrangencia.values()[sorteio.nextInt(Abrangencia.values().length)];
                registros.add(new RegistroDeCalendario(tipo, abrangencia, inicio, fim));
            }
            calendarios.add(new CalendarioDeExpediente(registros, EnumSet.allOf(Abrangencia.class)));
        }
        return calendarios;
    }

    @Test
    @DisplayName("RN31_propriedade_proximo_dia_util_e_dia_util_nao_anterior_e_idempotente")
    void RN31_propriedade_proximo_dia_util_e_dia_util_nao_anterior_e_idempotente() {
        for (CalendarioDeExpediente calendario : calendariosSorteados()) {
            for (LocalDate data = PRIMEIRO; !data.isAfter(ULTIMO); data = data.plusDays(1)) {
                LocalDate proximo = calendario.proximoDiaUtil(data);

                assertThat(calendario.ehDiaUtil(proximo)).as("%s", data).isTrue();
                assertThat(proximo).as("%s", data).isAfterOrEqualTo(data);
                assertThat(calendario.proximoDiaUtil(proximo)).as("%s", data).isEqualTo(proximo);
                assertThat(calendario.ehDiaUtil(data)).as("%s", data).isEqualTo(proximo.equals(data));
            }
        }
    }

    @Test
    @DisplayName("RN31_propriedade_nenhum_dia_util_e_pulado_entre_o_vencimento_bruto_e_o_vencimento")
    void RN31_propriedade_nenhum_dia_util_e_pulado_entre_o_vencimento_bruto_e_o_vencimento() {
        for (CalendarioDeExpediente calendario : calendariosSorteados()) {
            for (LocalDate data = PRIMEIRO; !data.isAfter(ULTIMO); data = data.plusDays(1)) {
                LocalDate proximo = calendario.proximoDiaUtil(data);
                for (LocalDate d = data; d.isBefore(proximo); d = d.plusDays(1)) {
                    assertThat(calendario.ehDiaUtil(d)).as("%s antes de %s", d, proximo).isFalse();
                }
            }
        }
    }

    @Test
    @DisplayName("RN01_RN31_propriedade_vencimento_e_dia_util_nao_anterior_ao_bruto_e_bruto_e_inicio_mais_dias")
    void RN01_RN31_propriedade_vencimento_e_dia_util_nao_anterior_ao_bruto_e_bruto_e_inicio_mais_dias() {
        List<Regimento.Prazo> regras = List.of(SP.prazos().indicacaoCondutor(), SP.prazos().recurso1a(),
                SP.prazos().recurso2a(), SP.prazos().cienciaPresumidaEletronica(), SP.prazos().julgamento(),
                SP.prazos().exigencia(), SP.prazos().informacaoAgente());

        for (CalendarioDeExpediente calendario : calendariosSorteados()) {
            MotorDePrazos motor = new MotorDePrazos(calendario, true);
            for (Regimento.Prazo regra : regras) {
                for (LocalDate inicio = PRIMEIRO; !inicio.isAfter(ULTIMO); inicio = inicio.plusDays(1)) {
                    PrazoCalculado prazo = motor.calcular(regra, inicio);

                    assertThat(prazo.vencimentoBruto()).isEqualTo(inicio.plusDays(regra.dias()));
                    assertThat(prazo.vencimento()).isAfterOrEqualTo(prazo.vencimentoBruto());
                    assertThat(calendario.ehDiaUtil(prazo.vencimento())).isTrue();
                    assertThat(prazo.prorrogado()).isEqualTo(!calendario.ehDiaUtil(prazo.vencimentoBruto()));
                    assertThat(prazo.tempestivo(prazo.vencimento())).isTrue();
                    assertThat(prazo.tempestivo(prazo.vencimento().plusDays(1))).isFalse();
                }
            }
        }
    }

    @Test
    @DisplayName("RN01_propriedade_termo_inicial_posterior_nunca_vence_antes")
    void RN01_propriedade_termo_inicial_posterior_nunca_vence_antes() {
        Regimento.Prazo regra = SP.prazos().recurso1a();

        for (CalendarioDeExpediente calendario : calendariosSorteados()) {
            MotorDePrazos motor = new MotorDePrazos(calendario, true);
            LocalDate anterior = motor.calcular(regra, PRIMEIRO).vencimento();
            for (LocalDate inicio = PRIMEIRO.plusDays(1); !inicio.isAfter(ULTIMO); inicio = inicio.plusDays(1)) {
                LocalDate atual = motor.calcular(regra, inicio).vencimento();

                assertThat(atual).as("início %s", inicio).isAfterOrEqualTo(anterior);
                anterior = atual;
            }
        }
    }

    @Test
    @DisplayName("RN31_propriedade_sem_prorrogacao_o_vencimento_e_sempre_o_bruto")
    void RN31_propriedade_sem_prorrogacao_o_vencimento_e_sempre_o_bruto() {
        Regimento.Prazo regra = SP.prazos().recurso1a();

        for (CalendarioDeExpediente calendario : calendariosSorteados()) {
            MotorDePrazos motor = new MotorDePrazos(calendario, false);
            for (LocalDate inicio = PRIMEIRO; !inicio.isAfter(ULTIMO); inicio = inicio.plusDays(1)) {
                PrazoCalculado prazo = motor.calcular(regra, inicio);

                assertThat(prazo.vencimento()).isEqualTo(prazo.vencimentoBruto());
                assertThat(prazo.prorrogado()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("RN37_propriedade_cada_alerta_cai_antes_do_vencimento_e_so_no_seu_dia")
    void RN37_propriedade_cada_alerta_cai_antes_do_vencimento_e_so_no_seu_dia() {
        Regimento.Alertas alertas = SP.prazos().alertas();

        for (LocalDate dia = PRIMEIRO; !dia.isAfter(ULTIMO); dia = dia.plusDays(1)) {
            LocalDate vencimento = dia;
            List<LocalDate> datas = MotorDePrazos.datasDeAlerta(vencimento, alertas);

            assertThat(datas).hasSize(alertas.diasAntes().size()).isSorted().allMatch(d -> d.isBefore(vencimento));
            for (LocalDate data : datas) {
                List<Integer> doDia = MotorDePrazos.alertasDoDia(data, vencimento, alertas);
                assertThat(doDia).hasSize(1);
                assertThat(data.plusDays(doDia.get(0))).isEqualTo(vencimento);
            }
        }
    }
}
