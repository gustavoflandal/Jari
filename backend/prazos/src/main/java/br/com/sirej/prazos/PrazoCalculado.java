package br.com.sirej.prazos;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Resultado da contagem de um prazo em dias corridos.
 *
 * @param termoInicial   data do fato que abre a contagem (ciência, publicação, inclusão); o dia dele não conta
 * @param vencimentoBruto termo inicial mais os dias do prazo, antes de qualquer prorrogação
 * @param vencimento     último dia para praticar o ato; igual ao bruto, ou ao primeiro dia útil seguinte (RN31)
 */
public record PrazoCalculado(LocalDate termoInicial, LocalDate vencimentoBruto, LocalDate vencimento) {

    public PrazoCalculado {
        Objects.requireNonNull(termoInicial, "termoInicial");
        Objects.requireNonNull(vencimentoBruto, "vencimentoBruto");
        Objects.requireNonNull(vencimento, "vencimento");
    }

    /** O vencimento caiu em dia não útil e foi para o próximo dia útil. */
    public boolean prorrogado() {
        return !vencimento.equals(vencimentoBruto);
    }

    /** A peça interposta nesta data está no prazo: no último dia é tempestiva, no dia seguinte não (RN01). */
    public boolean tempestivo(LocalDate dataDaInterposicao) {
        return !dataDaInterposicao.isAfter(vencimento);
    }
}
