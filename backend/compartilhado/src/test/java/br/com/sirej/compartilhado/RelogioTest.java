package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RelogioTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    @Test
    @DisplayName("PT-02: relógio fixo devolve sempre o mesmo instante (injetável em teste)")
    void PT02_relogio_fixo_e_deterministico() {
        Instant instante = Instant.parse("2026-10-06T12:00:00Z");
        Relogio relogio = Relogio.fixo(instante, SAO_PAULO);

        assertThat(relogio.agora()).isEqualTo(instante);
        assertThat(relogio.agora()).isEqualTo(relogio.agora());
        assertThat(relogio.fuso()).isEqualTo(SAO_PAULO);
    }

    @Test
    @DisplayName("PT-02: hoje() usa o fuso da instalação, não o UTC")
    void PT02_hoje_respeita_o_fuso_da_instalacao() {
        // 01h UTC de 07/10 ainda é 06/10 em São Paulo (UTC-3).
        Instant instante = Instant.parse("2026-10-07T01:00:00Z");

        assertThat(Relogio.fixo(instante, SAO_PAULO).hoje()).isEqualTo(LocalDate.of(2026, 10, 6));
        assertThat(Relogio.fixo(instante, ZoneId.of("UTC")).hoje()).isEqualTo(LocalDate.of(2026, 10, 7));
    }

    @Test
    @DisplayName("PT-02: relógio sobre um Clock qualquer usa o instante e o fuso desse Clock")
    void PT02_relogio_sobre_clock() {
        Instant instante = Instant.parse("2026-10-06T12:00:00Z");
        Relogio relogio = Relogio.de(Clock.fixed(instante, SAO_PAULO));

        assertThat(relogio.agora()).isEqualTo(instante);
        assertThat(relogio.fuso()).isEqualTo(SAO_PAULO);
    }

    @Test
    @DisplayName("PT-02: relógio do sistema acompanha o tempo real")
    void PT02_relogio_do_sistema_acompanha_o_tempo() {
        Relogio relogio = Relogio.doSistema(SAO_PAULO);

        assertThat(relogio.agora()).isCloseTo(Clock.systemUTC().instant(), within(5, ChronoUnit.SECONDS));
        assertThat(relogio.fuso()).isEqualTo(SAO_PAULO);
    }

    @Test
    @DisplayName("PT-02: relógio sem fuso, sem instante ou sem Clock é recusado")
    void PT02_relogio_incompleto_e_recusado() {
        assertThatThrownBy(() -> Relogio.doSistema(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Relogio.fixo(null, SAO_PAULO)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Relogio.fixo(Instant.EPOCH, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Relogio.de(null)).isInstanceOf(NullPointerException.class);
    }
}
