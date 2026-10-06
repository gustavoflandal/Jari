package br.com.sirej.compartilhado;

import java.util.Random;

/**
 * Gerador mínimo de documentos sintéticos para os testes dos tipos de valor (docs/dev/12, "Dados de
 * teste": somente sintéticos). O cálculo dos dígitos verificadores aqui é independente do código de
 * produção, de propósito: se os dois divergirem, os testes acusam.
 */
final class DadosSinteticos {

    private static final String ALFANUMERICOS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Random aleatorio;

    DadosSinteticos(long semente) {
        this.aleatorio = new Random(semente);
    }

    /** CPF de 11 dígitos, sem máscara, com dígitos verificadores corretos e dígitos não todos iguais. */
    String cpf() {
        while (true) {
            int[] d = new int[11];
            for (int i = 0; i < 9; i++) {
                d[i] = aleatorio.nextInt(10);
            }
            d[9] = dvModulo11(d, 9, 10);
            d[10] = dvModulo11(d, 10, 11);
            StringBuilder cpf = new StringBuilder();
            for (int digito : d) {
                cpf.append(digito);
            }
            if (!cpf.toString().chars().allMatch(c -> c == cpf.charAt(0))) {
                return cpf.toString();
            }
        }
    }

    /** CNPJ numérico de 14 dígitos, sem máscara, com dígitos verificadores corretos. */
    String cnpjNumerico() {
        String cnpj;
        do {
            cnpj = cnpj("0123456789");
        } while (cnpj.chars().allMatch(c -> c == '0'));
        return cnpj;
    }

    /** CNPJ alfanumérico (12 posições em [0-9A-Z] + 2 dígitos verificadores numéricos). */
    String cnpjAlfanumerico() {
        String cnpj;
        do {
            cnpj = cnpj(ALFANUMERICOS);
        } while (cnpj.substring(0, 12).chars().allMatch(Character::isDigit));
        return cnpj;
    }

    private String cnpj(String alfabeto) {
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            base.append(alfabeto.charAt(aleatorio.nextInt(alfabeto.length())));
        }
        base.append(dvCnpj(base.toString(), new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}));
        base.append(dvCnpj(base.toString(), new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}));
        return base.toString();
    }

    private static int dvModulo11(int[] digitos, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digitos[i] * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int dvCnpj(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (base.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    /** Troca o último dígito verificador por outro valor, gerando documento inválido. */
    static String comDigitoVerificadorTrocado(String documento) {
        int ultima = documento.length() - 1;
        char trocado = documento.charAt(ultima) == '9' ? '0' : (char) (documento.charAt(ultima) + 1);
        return documento.substring(0, ultima) + trocado;
    }
}
