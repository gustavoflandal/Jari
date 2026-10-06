package br.com.sirej.compartilhado;

import java.util.regex.Pattern;

/**
 * CPF validado, guardado só com os 11 dígitos.
 *
 * <p>Aceita a forma sem máscara ({@code 11144477735}) ou com máscara ({@code 111.444.777-35}).
 * Recusa dígitos verificadores errados e sequências de um só dígito repetido.
 *
 * <p>{@link #toString()} devolve a forma mascarada: o doc 13 proíbe CPF completo em log. Use
 * {@link #numero()} só onde o número completo é necessário (persistência cifrada, documento oficial).
 *
 * @param numero os 11 dígitos, sem máscara
 */
public record Cpf(String numero) {

    private static final Pattern SEM_MASCARA = Pattern.compile("\\d{11}");
    private static final Pattern COM_MASCARA = Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");
    private static final int TAMANHO = 11;
    private static final int BASE = 9;

    public Cpf {
        if (numero == null || !SEM_MASCARA.matcher(numero).matches()) {
            throw new ValorInvalidoException("CPF deve ter 11 dígitos");
        }
        if (numero.chars().allMatch(c -> c == numero.charAt(0))) {
            throw new ValorInvalidoException("CPF com todos os dígitos iguais");
        }
        if (digitoVerificador(numero, BASE) != numero.charAt(BASE) - '0'
                || digitoVerificador(numero, BASE + 1) != numero.charAt(BASE + 1) - '0') {
            throw new ValorInvalidoException("CPF com dígito verificador inválido");
        }
    }

    /** Lê um CPF com ou sem máscara; espaços nas pontas são ignorados. */
    public static Cpf de(String entrada) {
        if (entrada == null) {
            throw new ValorInvalidoException("CPF ausente");
        }
        String texto = entrada.strip();
        if (COM_MASCARA.matcher(texto).matches()) {
            texto = texto.replace(".", "").replace("-", "");
        }
        return new Cpf(texto);
    }

    /** Forma mascarada, segura para log e tela: {@code ***.444.777-**}. */
    public String mascarado() {
        return "***." + numero.substring(3, 6) + "." + numero.substring(6, BASE) + "-**";
    }

    @Override
    public String toString() {
        return mascarado();
    }

    /** Módulo 11 sobre os {@code quantidade} primeiros dígitos, com pesos decrescentes até 2. */
    private static int digitoVerificador(String digitos, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (digitos.charAt(i) - '0') * (quantidade + 1 - i);
        }
        int resto = soma % TAMANHO;
        return resto < 2 ? 0 : TAMANHO - resto;
    }
}
