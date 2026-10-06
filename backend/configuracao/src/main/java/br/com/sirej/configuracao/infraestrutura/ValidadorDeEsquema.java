package br.com.sirej.configuracao.infraestrutura;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import br.com.sirej.configuracao.RegimentoInvalidoException;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;

/**
 * Valida a árvore resolvida do regimento contra o JSON Schema ({@code config/regimentos/esquema.json}, draft
 * 2020-12). Toda violação sai com a regra "esquema" e o caminho da chave.
 *
 * <p>Implementa o subconjunto de palavras-chave que o esquema do regimento usa: {@code $ref} local
 * ({@code #/$defs/...}), {@code type}, {@code enum}, {@code required}, {@code properties},
 * {@code additionalProperties}, {@code propertyNames}, {@code minProperties}, {@code items}, {@code minItems},
 * {@code uniqueItems}, {@code minimum}, {@code maximum}, {@code minLength} e {@code pattern}; anotações
 * ({@code $schema}, {@code $id}, {@code title}, {@code description}) são ignoradas. Palavra-chave fora dessa lista
 * reprova o próprio esquema ao carregar, para que nenhuma restrição escrita no esquema deixe de ser aplicada em
 * silêncio. Sem biblioteca externa: o módulo não traz Jackson (SCA).
 */
public final class ValidadorDeEsquema {

    /** Nome do arquivo do esquema, ao lado dos regimentos. */
    public static final String ARQUIVO = "esquema.json";

    private static final Set<String> ANOTACOES = Set.of("$schema", "$id", "title", "description", "$defs");
    private static final Set<String> SUPORTADAS = Set.of("$ref", "type", "enum", "required", "properties",
            "additionalProperties", "propertyNames", "minProperties", "items", "minItems", "uniqueItems", "minimum",
            "maximum", "minLength", "pattern");
    private static final String PREFIXO_REF = "#/$defs/";

    private final Map<String, Object> raiz;
    private final Map<String, Object> definicoes;

    @SuppressWarnings("unchecked")
    public ValidadorDeEsquema(Object esquema) {
        if (!(esquema instanceof Map<?, ?> mapa)) {
            throw new RegimentoInvalidoException("esquema do regimento ilegível: a raiz precisa ser um objeto");
        }
        this.raiz = (Map<String, Object>) mapa;
        Object defs = raiz.get("$defs");
        this.definicoes = defs instanceof Map<?, ?> d ? (Map<String, Object>) d : Map.of();
        conferirPalavrasChave(raiz, "#");
    }

    /** Violações da árvore contra o esquema; vazio se ela é válida. */
    public List<Violacao> validar(Map<String, Object> arvore) {
        List<Violacao> violacoes = new ArrayList<>();
        validar(arvore, raiz, "", violacoes);
        return violacoes;
    }

    @SuppressWarnings("unchecked")
    private void validar(Object valor, Map<String, Object> esquema, String caminho, List<Violacao> v) {
        Object ref = esquema.get("$ref");
        if (ref != null) {
            validar(valor, definicao((String) ref), caminho, v);
        }
        if (esquema.containsKey("type") && !tipoAceito(valor, esquema.get("type"))) {
            v.add(violacao(caminho, "tipo inválido: esperado " + esquema.get("type") + ", encontrado " + tipo(valor)));
            return;
        }
        if (esquema.containsKey("enum") && !contemValor((List<Object>) esquema.get("enum"), valor)) {
            v.add(violacao(caminho, "valor " + valor + " fora do vocabulário " + esquema.get("enum")));
        }
        if (valor instanceof Map<?, ?> objeto) {
            validarObjeto((Map<String, Object>) objeto, esquema, caminho, v);
        } else if (valor instanceof List<?> lista) {
            validarLista((List<Object>) lista, esquema, caminho, v);
        } else if (valor instanceof String texto) {
            validarTexto(texto, esquema, caminho, v);
        } else if (inteiro(valor)) {
            validarNumero(new BigDecimal(valor.toString()), esquema, caminho, v);
        }
    }

    @SuppressWarnings("unchecked")
    private void validarObjeto(Map<String, Object> objeto, Map<String, Object> esquema, String caminho,
            List<Violacao> v) {
        for (Object obrigatoria : (List<Object>) esquema.getOrDefault("required", List.of())) {
            if (!objeto.containsKey(obrigatoria)) {
                v.add(violacao(filho(caminho, obrigatoria), "chave obrigatória ausente"));
            }
        }
        if (esquema.containsKey("minProperties") && objeto.size() < numero(esquema.get("minProperties"))) {
            v.add(violacao(caminho, "precisa de ao menos " + esquema.get("minProperties") + " chaves"));
        }
        Map<String, Object> propriedades = (Map<String, Object>) esquema.getOrDefault("properties", Map.of());
        Object adicionais = esquema.get("additionalProperties");
        Map<String, Object> nomes = (Map<String, Object>) esquema.get("propertyNames");
        for (Map.Entry<Object, Object> par : ((Map<Object, Object>) (Map<?, ?>) objeto).entrySet()) {
            Object chave = par.getKey();
            String aqui = filho(caminho, chave);
            if (!(chave instanceof String)) {
                v.add(violacao(aqui, "chave precisa ser texto"));
                continue;
            }
            if (nomes != null) {
                validar(chave, nomes, aqui, v);
            }
            if (propriedades.containsKey(chave)) {
                validar(par.getValue(), (Map<String, Object>) propriedades.get(chave), aqui, v);
            } else if (Boolean.FALSE.equals(adicionais)) {
                v.add(violacao(aqui, "chave não prevista no esquema"));
            } else if (adicionais instanceof Map<?, ?> esquemaAdicional) {
                validar(par.getValue(), (Map<String, Object>) esquemaAdicional, aqui, v);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validarLista(List<Object> lista, Map<String, Object> esquema, String caminho, List<Violacao> v) {
        if (esquema.containsKey("minItems") && lista.size() < numero(esquema.get("minItems"))) {
            v.add(violacao(caminho, "precisa de ao menos " + esquema.get("minItems") + " itens"));
        }
        if (Boolean.TRUE.equals(esquema.get("uniqueItems")) && new HashSet<>(lista).size() != lista.size()) {
            v.add(violacao(caminho, "itens repetidos"));
        }
        if (esquema.get("items") instanceof Map<?, ?> itens) {
            for (int i = 0; i < lista.size(); i++) {
                validar(lista.get(i), (Map<String, Object>) itens, caminho + "[" + i + "]", v);
            }
        }
    }

    private static void validarTexto(String texto, Map<String, Object> esquema, String caminho, List<Violacao> v) {
        if (esquema.containsKey("minLength") && texto.length() < numero(esquema.get("minLength"))) {
            v.add(violacao(caminho, "texto curto demais"));
        }
        if (esquema.containsKey("pattern") && !Pattern.compile((String) esquema.get("pattern")).matcher(texto).find()) {
            v.add(violacao(caminho, "valor " + texto + " fora do formato " + esquema.get("pattern")));
        }
    }

    private static void validarNumero(BigDecimal valor, Map<String, Object> esquema, String caminho,
            List<Violacao> v) {
        if (esquema.containsKey("minimum") && valor.compareTo(new BigDecimal(esquema.get("minimum").toString())) < 0) {
            v.add(violacao(caminho, "valor " + valor + " abaixo do mínimo " + esquema.get("minimum")));
        }
        if (esquema.containsKey("maximum") && valor.compareTo(new BigDecimal(esquema.get("maximum").toString())) > 0) {
            v.add(violacao(caminho, "valor " + valor + " acima do máximo " + esquema.get("maximum")));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> definicao(String ref) {
        if (!ref.startsWith(PREFIXO_REF) || !(definicoes.get(ref.substring(PREFIXO_REF.length())) instanceof Map<?, ?> d)) {
            throw new RegimentoInvalidoException("esquema do regimento com $ref não resolvido: " + ref);
        }
        return (Map<String, Object>) d;
    }

    @SuppressWarnings("unchecked")
    private void conferirPalavrasChave(Map<String, Object> esquema, String onde) {
        for (Map.Entry<String, Object> par : esquema.entrySet()) {
            String palavra = par.getKey();
            if (!SUPORTADAS.contains(palavra) && !ANOTACOES.contains(palavra)) {
                throw new RegimentoInvalidoException("esquema do regimento usa palavra-chave não suportada '"
                        + palavra + "' em " + onde);
            }
            Object valor = par.getValue();
            switch (palavra) {
                case "$ref" -> definicao((String) valor);
                case "properties", "$defs" -> ((Map<String, Object>) valor).forEach((nome, sub) ->
                        conferirPalavrasChave((Map<String, Object>) sub, onde + "/" + palavra + "/" + nome));
                case "items", "propertyNames" -> conferirPalavrasChave((Map<String, Object>) valor, onde + "/" + palavra);
                case "additionalProperties" -> {
                    if (valor instanceof Map<?, ?> sub) {
                        conferirPalavrasChave((Map<String, Object>) sub, onde + "/" + palavra);
                    }
                }
                default -> {
                    // palavra-chave de valor simples
                }
            }
        }
    }

    private static boolean tipoAceito(Object valor, Object tipos) {
        List<?> lista = tipos instanceof List<?> l ? l : List.of(tipos);
        return lista.stream().anyMatch(t -> switch ((String) t) {
            case "object" -> valor instanceof Map;
            case "array" -> valor instanceof List;
            case "string" -> valor instanceof String;
            case "boolean" -> valor instanceof Boolean;
            case "integer" -> inteiro(valor);
            case "number" -> valor instanceof Number;
            case "null" -> valor == null;
            default -> throw new RegimentoInvalidoException("esquema do regimento com tipo desconhecido: " + t);
        });
    }

    private static boolean contemValor(List<Object> vocabulario, Object valor) {
        return vocabulario.stream().anyMatch(item -> item == null ? valor == null : item.equals(valor));
    }

    private static boolean inteiro(Object valor) {
        return valor instanceof Integer || valor instanceof Long || valor instanceof BigInteger
                || valor instanceof Short || valor instanceof Byte;
    }

    private static long numero(Object valor) {
        return ((Number) valor).longValue();
    }

    private static String tipo(Object valor) {
        if (valor == null) {
            return "null";
        }
        if (valor instanceof Map) {
            return "object";
        }
        if (valor instanceof List) {
            return "array";
        }
        if (inteiro(valor)) {
            return "integer";
        }
        return valor instanceof String ? "string" : valor instanceof Boolean ? "boolean" : valor.getClass().getSimpleName();
    }

    private static String filho(String caminho, Object chave) {
        return caminho.isEmpty() ? String.valueOf(chave) : caminho + "." + chave;
    }

    private static Violacao violacao(String caminho, String mensagem) {
        return new Violacao("esquema", caminho.isEmpty() ? "(raiz)" : caminho, mensagem);
    }
}
