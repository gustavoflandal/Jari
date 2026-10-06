package br.com.sirej.compartilhado;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * CNPJ validado, guardado com as 14 posições sem máscara, em maiúsculas.
 *
 * <p>Aceita o formato numérico e o alfanumérico da IN RFB 2.229/2024 (12 posições em {@code [0-9A-Z]}
 * seguidas de 2 dígitos verificadores numéricos). O dígito verificador usa módulo 11 com o valor de
 * cada caractere igual ao seu código ASCII menos 48, o que mantém o cálculo antigo para dígitos.
 *
 * <p>{@link #toString()} devolve a forma mascarada, como no {@link Cpf}.
 *
 * @param numero as 14 posições, sem máscara, em maiúsculas
 */
public record Cnpj(String numero) {

    private static final Pattern SEM_MASCARA = Pattern.compile("[0-9A-Z]{12}\\d{2}");
    private static final Pattern COM_MASCARA =
            Pattern.compile("[0-9A-Z]{2}\\.[0-9A-Z]{3}\\.[0-9A-Z]{3}/[0-9A-Z]{4}-\\d{2}");
    private static final int[] PESOS_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int MODULO = 11;

    public Cnpj {
        if (numero == null || !SEM_MASCARA.matcher(numero).matches()) {
            throw new ValorInvalidoException("CNPJ deve ter 12 posições alfanuméricas e 2 dígitos verificadores");
        }
        if (numero.chars().allMatch(c -> c == numero.charAt(0))) {
            throw new ValorInvalidoException("CNPJ com todas as posições iguais");
        }
        if (digitoVerificador(numero, PESOS_DV1) != numero.charAt(PESOS_DV1.length) - '0'
                || digitoVerificador(numero, PESOS_DV2) != numero.charAt(PESOS_DV2.length) - '0') {
            throw new ValorInvalidoException("CNPJ com dígito verificador inválido");
        }
    }

    /** Lê um CNPJ com ou sem máscara, em maiúsculas ou minúsculas; espaços nas pontas são ignorados. */
    public static Cnpj de(String entrada) {
        if (entrada == null) {
            throw new ValorInvalidoException("CNPJ ausente");
        }
        String texto = entrada.strip().toUpperCase(Locale.ROOT);
        if (COM_MASCARA.matcher(texto).matches()) {
            texto = texto.replace(".", "").replace("/", "").replace("-", "");
        }
        return new Cnpj(texto);
    }

    /** Forma mascarada, segura para log e tela: {@code **.ABC.345/****-**}. */
    public String mascarado() {
        return "**." + numero.substring(2, 5) + "." + numero.substring(5, 8) + "/****-**";
    }

    @Override
    public String toString() {
        return mascarado();
    }

    private static int digitoVerificador(String posicoes, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (posicoes.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % MODULO;
        return resto < 2 ? 0 : MODULO - resto;
    }
}
