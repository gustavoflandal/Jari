package br.com.sirej.auditoria.dominio;

import java.util.Map;
import java.util.SortedMap;

/**
 * Escritor de JSON canônico mínimo para o encadeamento da trilha (doc 08): objetos com chaves em ordem,
 * sem espaços, valores só texto, número inteiro, nulo ou objeto aninhado do mesmo tipo.
 *
 * <p>Dois registros com o mesmo conteúdo produzem exatamente os mesmos bytes.
 */
public final class JsonCanonico {

    private JsonCanonico() {
    }

    /** Serializa um objeto cujas chaves já estão ordenadas. */
    public static String objeto(SortedMap<String, ?> campos) {
        StringBuilder saida = new StringBuilder();
        escreverObjeto(campos, saida);
        return saida.toString();
    }

    private static void escreverObjeto(Map<String, ?> campos, StringBuilder saida) {
        saida.append('{');
        boolean primeiro = true;
        for (Map.Entry<String, ?> campo : campos.entrySet()) {
            if (!primeiro) {
                saida.append(',');
            }
            primeiro = false;
            escreverTexto(campo.getKey(), saida);
            saida.append(':');
            escreverValor(campo.getValue(), saida);
        }
        saida.append('}');
    }

    private static void escreverValor(Object valor, StringBuilder saida) {
        switch (valor) {
            case null -> saida.append("null");
            case String texto -> escreverTexto(texto, saida);
            case Long numero -> saida.append(numero.longValue());
            case SortedMap<?, ?> mapa -> {
                @SuppressWarnings("unchecked")
                SortedMap<String, ?> aninhado = (SortedMap<String, ?>) mapa;
                escreverObjeto(aninhado, saida);
            }
            default -> throw new IllegalArgumentException("tipo sem forma canônica: " + valor.getClass());
        }
    }

    private static void escreverTexto(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                case '\t' -> saida.append("\\t");
                case '\b' -> saida.append("\\b");
                case '\f' -> saida.append("\\f");
                default -> {
                    if (c < ' ') {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
                }
            }
        }
        saida.append('"');
    }
}
