package br.com.sirej.compartilhado;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Fonte única de tempo do SIREJ (docs/dev/13: "Tempo sempre via {@code Relogio} injetável").
 *
 * <p>Nenhuma classe chama {@code Instant.now()}, {@code LocalDate.now()} ou equivalentes: recebe um
 * {@code Relogio}. A regra 6 do doc 12 verifica isso no build. Em teste, use {@link #fixo}.
 *
 * <p>O fuso é o da instalação (prazos, semana de distribuição e sessões são contados nele). Ele vem da
 * configuração técnica da instalação; este tipo não escolhe fuso por conta própria.
 */
public interface Relogio {

    /** Instante atual. */
    Instant agora();

    /** Fuso da instalação. */
    ZoneId fuso();

    /** Data atual no fuso da instalação. */
    default LocalDate hoje() {
        return LocalDate.ofInstant(agora(), fuso());
    }

    /** Relógio real do sistema no fuso da instalação. */
    static Relogio doSistema(ZoneId fuso) {
        return new RelogioSobreClock(Clock.system(Objects.requireNonNull(fuso, "fuso")));
    }

    /** Relógio parado num instante, para testes e reprocessamentos determinísticos. */
    static Relogio fixo(Instant instante, ZoneId fuso) {
        return new RelogioSobreClock(Clock.fixed(Objects.requireNonNull(instante, "instante"),
                Objects.requireNonNull(fuso, "fuso")));
    }

    /** Relógio sobre um {@link Clock} qualquer (por exemplo, um relógio de teste que avança). */
    static Relogio de(Clock clock) {
        return new RelogioSobreClock(Objects.requireNonNull(clock, "clock"));
    }
}

/** Implementação sobre {@link Clock}; visível só neste pacote. */
record RelogioSobreClock(Clock clock) implements Relogio {

    @Override
    public Instant agora() {
        return clock.instant();
    }

    @Override
    public ZoneId fuso() {
        return clock.getZone();
    }
}
