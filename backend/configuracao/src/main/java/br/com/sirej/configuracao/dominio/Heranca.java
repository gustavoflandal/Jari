package br.com.sirej.configuracao.dominio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import br.com.sirej.configuracao.RegimentoInvalidoException;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;

/**
 * Resolve a chave {@code herda} de um regimento (docs/dev/06, "Arquivos": {@code curitiba.yaml} herda de
 * {@code sp} e sobrescreve).
 *
 * <p>Semântica (D-51): objetos são mesclados chave a chave, recursivamente; listas e valores simples do filho
 * substituem os do pai por inteiro (uma lista nunca é concatenada); um {@code null} explícito no filho também
 * substitui. A herança pode ter vários níveis; ciclo ou pai inexistente reprova o regimento.
 */
public final class Heranca {

    /** Nome da chave de herança no YAML. */
    public static final String CHAVE = "herda";

    private static final String REGRA = "herança";

    private Heranca() {
    }

    /**
     * Devolve a árvore resolvida, já sem a chave {@code herda}.
     *
     * @param nome regimento a resolver (ex.: {@code curitiba})
     * @param leitor lê a árvore crua de um regimento pelo nome; vazio se não existir
     */
    public static Map<String, Object> resolver(String nome, Function<String, Optional<Map<String, Object>>> leitor) {
        return resolver(nome, leitor, new LinkedHashSet<>());
    }

    private static Map<String, Object> resolver(String nome, Function<String, Optional<Map<String, Object>>> leitor,
            Set<String> visitados) {
        if (!visitados.add(nome)) {
            throw new RegimentoInvalidoException(nome, List.of(new Violacao(REGRA, CHAVE,
                    "herança cíclica: " + String.join(" -> ", visitados) + " -> " + nome)));
        }
        Map<String, Object> arvore = leitor.apply(nome).orElseThrow(() -> new RegimentoInvalidoException(nome,
                List.of(new Violacao(REGRA, CHAVE, "regimento '" + nome + "' não encontrado"))));
        Map<String, Object> proprio = new LinkedHashMap<>(arvore);
        Object pai = proprio.remove(CHAVE);
        if (pai == null) {
            return proprio;
        }
        if (!(pai instanceof String nomePai) || nomePai.isBlank()) {
            throw new RegimentoInvalidoException(nome, List.of(new Violacao(REGRA, CHAVE,
                    "herda deve ser o nome de outro regimento")));
        }
        return mesclar(resolver(nomePai, leitor, visitados), proprio);
    }

    /** Mescla {@code filho} sobre {@code pai}: objetos recursivamente; o resto, o filho substitui. */
    @SuppressWarnings("unchecked")
    static Map<String, Object> mesclar(Map<String, Object> pai, Map<String, Object> filho) {
        Map<String, Object> resultado = new LinkedHashMap<>(pai);
        filho.forEach((chave, valorFilho) -> {
            Object valorPai = resultado.get(chave);
            if (valorPai instanceof Map<?, ?> mapaPai && valorFilho instanceof Map<?, ?> mapaFilho) {
                resultado.put(chave, mesclar((Map<String, Object>) mapaPai, (Map<String, Object>) mapaFilho));
            } else {
                resultado.put(chave, copia(valorFilho));
            }
        });
        return resultado;
    }

    @SuppressWarnings("unchecked")
    private static Object copia(Object valor) {
        if (valor instanceof Map<?, ?> mapa) {
            return mesclar(Map.of(), (Map<String, Object>) mapa);
        }
        if (valor instanceof List<?> lista) {
            List<Object> nova = new ArrayList<>(lista.size());
            lista.forEach(item -> nova.add(copia(item)));
            return nova;
        }
        return valor;
    }
}
