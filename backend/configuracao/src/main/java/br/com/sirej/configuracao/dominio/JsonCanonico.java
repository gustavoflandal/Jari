package br.com.sirej.configuracao.dominio;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.com.sirej.compartilhado.Hash;

/**
 * Conteúdo canônico do regimento: JSON sem espaços, com as chaves de cada objeto em ordem crescente (comparação
 * por unidades UTF-16, como na RFC 8785), strings com escape mínimo e números inteiros em decimal. Duas
 * configurações com o mesmo conteúdo, em qualquer ordem de chaves, com ou sem comentários, têm o mesmo texto
 * canônico e, portanto, o mesmo hash (docs/dev/06, "Ciclo de vida", item 2).
 *
 * <p>Aceita a árvore produzida pela leitura do YAML: {@link Map} com chaves {@link String}, {@link List},
 * {@link String}, {@link Boolean}, números inteiros e {@code null}.
 */
public final class JsonCanonico {

    private JsonCanonico() {
    }

    /** Texto canônico da árvore. */
    public static String de(Object arvore) {
        StringBuilder saida = new StringBuilder();
        escrever(arvore, saida);
        return saida.toString();
    }

    /** SHA-256 do texto canônico em UTF-8. */
    public static Hash hash(Object arvore) {
        return Hash.sha256(de(arvore).getBytes(StandardCharsets.UTF_8));
    }

    private static void escrever(Object valor, StringBuilder saida) {
        switch (valor) {
            case null -> saida.append("null");
            case Map<?, ?> mapa -> escreverObjeto(mapa, saida);
            case List<?> lista -> escreverLista(lista, saida);
            case String texto -> escreverTexto(texto, saida);
            case Boolean booleano -> saida.append(booleano);
            case Integer _, Long _, Short _, Byte _, BigInteger _ -> saida.append(valor);
            case BigDecimal decimal -> saida.append(decimal.stripTrailingZeros().toPlainString());
            case Number numero -> saida.append(new BigDecimal(numero.toString()).stripTrailingZeros().toPlainString());
            default -> throw new IllegalArgumentException("tipo sem forma canônica: " + valor.getClass().getName());
        }
    }

    private static void escreverObjeto(Map<?, ?> mapa, StringBuilder saida) {
        TreeMap<String, Object> ordenado = new TreeMap<>();
        mapa.forEach((chave, valor) -> {
            if (!(chave instanceof String texto)) {
                throw new IllegalArgumentException("chave de objeto deve ser texto: " + chave);
            }
            ordenado.put(texto, valor);
        });
        saida.append('{');
        boolean primeiro = true;
        for (Map.Entry<String, Object> par : ordenado.entrySet()) {
            if (!primeiro) {
                saida.append(',');
            }
            primeiro = false;
            escreverTexto(par.getKey(), saida);
            saida.append(':');
            escrever(par.getValue(), saida);
        }
        saida.append('}');
    }

    private static void escreverLista(List<?> lista, StringBuilder saida) {
        saida.append('[');
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                saida.append(',');
            }
            escrever(lista.get(i), saida);
        }
        saida.append(']');
    }

    private static void escreverTexto(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"' -> saida.append("\\\"");
                case '\\' -> saida.append("\\\\");
                case '\b' -> saida.append("\\b");
                case '\f' -> saida.append("\\f");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                case '\t' -> saida.append("\\t");
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
