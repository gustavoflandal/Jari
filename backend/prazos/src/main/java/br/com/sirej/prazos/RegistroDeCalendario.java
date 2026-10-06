package br.com.sirej.prazos;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Registro do calendário de expediente: um dia ou um intervalo de datas (inclusive nas duas pontas) em que não há
 * expediente. Quem decide que o registro existe é o módulo {@code configuracao} (PT-29); este tipo só o descreve.
 */
public record RegistroDeCalendario(TipoDeRegistro tipo, Abrangencia abrangencia, LocalDate inicio, LocalDate fim) {

    public RegistroDeCalendario {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(abrangencia, "abrangencia");
        Objects.requireNonNull(inicio, "inicio");
        Objects.requireNonNull(fim, "fim");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim do registro do calendário é anterior ao início.");
        }
    }

    /** Registro de um único dia. */
    public static RegistroDeCalendario dia(TipoDeRegistro tipo, Abrangencia abrangencia, LocalDate dia) {
        return new RegistroDeCalendario(tipo, abrangencia, dia, dia);
    }

    /** O registro cobre a data informada. */
    public boolean cobre(LocalDate data) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
}
