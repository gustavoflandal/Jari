package br.com.sirej.compartilhado;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placa de veículo, normalizada em maiúsculas e sem hífen.
 *
 * <p>Formatos aceitos: antigo ({@code ABC1234}, também escrito {@code ABC-1234}) e Mercosul
 * ({@code ABC1D23}). Não há conversão entre os dois: a equivalência entre placa antiga e Mercosul
 * do mesmo veículo é assunto de quem agrupa conexos (doc 07), que usa também o RENAVAM.
 *
 * <p>{@link #toString()} mascara os quatro últimos caracteres: placa identifica o proprietário e o
 * doc 08 manda mascará-la na transparência; o mesmo cuidado vale para log.
 *
 * @param valor a placa normalizada, sem hífen
 */
public record Placa(String valor) {

    /** Padrão da placa. */
    public enum Formato {
        /** Três letras e quatro algarismos. */
        ANTIGA,
        /** Três letras, um algarismo, uma letra e dois algarismos (padrão Mercosul). */
        MERCOSUL
    }

    private static final Pattern ANTIGA = Pattern.compile("[A-Z]{3}\\d{4}");
    private static final Pattern MERCOSUL = Pattern.compile("[A-Z]{3}\\d[A-Z]\\d{2}");
    private static final Pattern ENTRADA = Pattern.compile("([A-Z]{3})-?(\\d[0-9A-Z]\\d{2})");
    private static final int LETRAS_VISIVEIS = 3;

    public Placa {
        if (valor == null || !(ANTIGA.matcher(valor).matches() || MERCOSUL.matcher(valor).matches())) {
            throw new ValorInvalidoException("placa fora dos padrões antigo e Mercosul");
        }
    }

    /** Lê uma placa em maiúsculas ou minúsculas, com ou sem hífen após as letras. */
    public static Placa de(String entrada) {
        if (entrada == null) {
            throw new ValorInvalidoException("placa ausente");
        }
        Matcher partes = ENTRADA.matcher(entrada.strip().toUpperCase(Locale.ROOT));
        if (!partes.matches()) {
            throw new ValorInvalidoException("placa fora dos padrões antigo e Mercosul");
        }
        return new Placa(partes.group(1) + partes.group(2));
    }

    public Formato formato() {
        return ANTIGA.matcher(valor).matches() ? Formato.ANTIGA : Formato.MERCOSUL;
    }

    @Override
    public String toString() {
        return valor.substring(0, LETRAS_VISIVEIS) + "****";
    }
}
