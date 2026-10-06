package br.com.sirej.configuracao.infraestrutura;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte a árvore do YAML (já validada pelo esquema) no {@code Regimento} tipado e de volta, pelos componentes
 * dos {@code record}s: o nome da chave é o nome do componente. Tipos aceitos: {@code record}, {@code enum},
 * {@link String}, {@code int}/{@link Integer}, {@code boolean}/{@link Boolean}, {@link List} e {@link Map} com
 * chave texto. Sem biblioteca externa.
 */
public final class ConversorDeRegistros {

    private ConversorDeRegistros() {
    }

    /** Monta o {@code record} a partir de um objeto da árvore. */
    @SuppressWarnings("unchecked")
    public static <T extends Record> T paraRegistro(Map<String, Object> arvore, Class<T> tipo) {
        return (T) converter(arvore, tipo, tipo.getSimpleName());
    }

    /** Árvore (mapas, listas e valores simples) de um {@code record}, na ordem dos componentes. */
    public static Map<String, Object> paraArvore(Record registro) {
        Map<String, Object> arvore = new LinkedHashMap<>();
        for (RecordComponent componente : registro.getClass().getRecordComponents()) {
            arvore.put(componente.getName(), paraValor(ler(componente, registro)));
        }
        return arvore;
    }

    private static Object paraValor(Object valor) {
        return switch (valor) {
            case null -> null;
            case Record registro -> paraArvore(registro);
            case Enum<?> constante -> constante.name();
            case List<?> lista -> lista.stream().map(ConversorDeRegistros::paraValor).toList();
            case Map<?, ?> mapa -> {
                Map<String, Object> copia = new LinkedHashMap<>();
                mapa.forEach((chave, item) -> copia.put((String) chave, paraValor(item)));
                yield copia;
            }
            default -> valor;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object converter(Object valor, Type tipo, String caminho) {
        Class<?> classe = classe(tipo);
        if (valor == null) {
            if (classe.isPrimitive()) {
                throw new IllegalArgumentException(caminho + ": valor obrigatório ausente");
            }
            return null;
        }
        if (classe.isRecord()) {
            return registro((Map<String, Object>) valor, classe, caminho);
        }
        if (classe.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) classe, (String) valor);
        }
        if (classe == List.class) {
            Type item = argumento(tipo, 0);
            List<Object> lista = new ArrayList<>();
            List<?> origem = (List<?>) valor;
            for (int i = 0; i < origem.size(); i++) {
                lista.add(converter(origem.get(i), item, caminho + "[" + i + "]"));
            }
            return lista;
        }
        if (classe == Map.class) {
            Type item = argumento(tipo, 1);
            Map<String, Object> mapa = new LinkedHashMap<>();
            ((Map<String, Object>) valor).forEach((chave, v) -> mapa.put(chave, converter(v, item, caminho + "." + chave)));
            return mapa;
        }
        if (classe == int.class || classe == Integer.class) {
            return ((Number) valor).intValue();
        }
        if (classe == boolean.class || classe == Boolean.class || classe == String.class) {
            return classe == String.class ? (String) valor : (Boolean) valor;
        }
        throw new IllegalArgumentException(caminho + ": tipo sem conversão " + tipo);
    }

    private static Object registro(Map<String, Object> mapa, Class<?> classe, String caminho) {
        RecordComponent[] componentes = classe.getRecordComponents();
        Object[] argumentos = new Object[componentes.length];
        for (int i = 0; i < componentes.length; i++) {
            RecordComponent c = componentes[i];
            argumentos[i] = converter(mapa.get(c.getName()), c.getGenericType(), caminho + "." + c.getName());
        }
        try {
            Constructor<?> construtor = classe.getDeclaredConstructor(
                    Arrays.stream(componentes).map(RecordComponent::getType).toArray(Class<?>[]::new));
            return construtor.newInstance(argumentos);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(caminho + ": " + e.getCause().getMessage(), e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(caminho + ": falha ao montar " + classe.getName(), e);
        }
    }

    private static Object ler(RecordComponent componente, Record registro) {
        try {
            return componente.getAccessor().invoke(registro);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("falha ao ler " + componente.getName(), e);
        }
    }

    private static Class<?> classe(Type tipo) {
        return tipo instanceof ParameterizedType p ? (Class<?>) p.getRawType() : (Class<?>) tipo;
    }

    private static Type argumento(Type tipo, int indice) {
        return ((ParameterizedType) tipo).getActualTypeArguments()[indice];
    }
}
