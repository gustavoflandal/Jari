package br.com.sirej.auditoria.dominio;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Imutavel;

/**
 * Linha da tabela {@code auditoria.registro_auditoria} (doc 05), elo da cadeia.
 *
 * <p>{@code hash = SHA-256(hash_anterior ‖ JSON_canonico(registro_sem_hash))} (doc 08), em que
 * {@code hash_anterior} entra como os 64 caracteres hexadecimais e o JSON em UTF-8. O registro sem hash
 * inclui {@code seq} e {@code hash_anterior}: reordenar ou trocar o elo anterior muda o hash.
 *
 * @param seq número de sequência
 * @param em instante, truncado em microssegundos (precisão do {@code timestamptz})
 * @param atorId autor
 * @param atorPapel papel do autor
 * @param ip IP na forma canônica de {@link EnderecoIp}, ou {@code null}
 * @param acao código do ato
 * @param alvoTipo tipo do alvo
 * @param alvoId identificador do alvo
 * @param detalhe pares adicionais, ordenados pela chave
 * @param hashAnterior hash do elo anterior ({@link #GENESE} no primeiro)
 * @param hash hash deste elo
 */
@Imutavel
public record RegistroAuditoria(long seq, Instant em, String atorId, String atorPapel, String ip, String acao,
        String alvoTipo, String alvoId, SortedMap<String, String> detalhe, Hash hashAnterior, Hash hash) {

    /** Elo anterior do primeiro registro da cadeia. */
    public static final Hash GENESE = new Hash("0".repeat(64));

    public RegistroAuditoria {
        Objects.requireNonNull(em, "em");
        Objects.requireNonNull(atorId, "atorId");
        Objects.requireNonNull(atorPapel, "atorPapel");
        Objects.requireNonNull(acao, "acao");
        Objects.requireNonNull(alvoTipo, "alvoTipo");
        Objects.requireNonNull(alvoId, "alvoId");
        Objects.requireNonNull(hashAnterior, "hashAnterior");
        Objects.requireNonNull(hash, "hash");
        detalhe = Collections.unmodifiableSortedMap(new TreeMap<>(Objects.requireNonNull(detalhe, "detalhe")));
    }

    /** Cria o próximo elo da cadeia, calculando o hash. */
    public static RegistroAuditoria encadear(long seq, Instant em, String atorId, String atorPapel, String ip,
            String acao, String alvoTipo, String alvoId, SortedMap<String, String> detalhe, Hash hashAnterior) {
        Instant emMicros = em.truncatedTo(ChronoUnit.MICROS);
        Hash hash = calcularHash(seq, emMicros, atorId, atorPapel, ip, acao, alvoTipo, alvoId, detalhe,
                hashAnterior);
        return new RegistroAuditoria(seq, emMicros, atorId, atorPapel, ip, acao, alvoTipo, alvoId, detalhe,
                hashAnterior, hash);
    }

    /** Recalcula o hash a partir do conteúdo gravado; diferente de {@link #hash()} indica adulteração. */
    public Hash hashRecalculado() {
        return calcularHash(seq, em, atorId, atorPapel, ip, acao, alvoTipo, alvoId, detalhe, hashAnterior);
    }

    /** Forma canônica do registro sem o hash (doc 08). */
    public String canonicoSemHash() {
        return canonico(seq, em, atorId, atorPapel, ip, acao, alvoTipo, alvoId, detalhe, hashAnterior);
    }

    private static Hash calcularHash(long seq, Instant em, String atorId, String atorPapel, String ip, String acao,
            String alvoTipo, String alvoId, SortedMap<String, String> detalhe, Hash hashAnterior) {
        String conteudo = hashAnterior.hex()
                + canonico(seq, em, atorId, atorPapel, ip, acao, alvoTipo, alvoId, detalhe, hashAnterior);
        return Hash.sha256(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    private static String canonico(long seq, Instant em, String atorId, String atorPapel, String ip, String acao,
            String alvoTipo, String alvoId, SortedMap<String, String> detalhe, Hash hashAnterior) {
        TreeMap<String, Object> campos = new TreeMap<>();
        campos.put("acao", acao);
        campos.put("alvo_id", alvoId);
        campos.put("alvo_tipo", alvoTipo);
        campos.put("ator_id", atorId);
        campos.put("ator_papel", atorPapel);
        campos.put("detalhe", new TreeMap<>(detalhe));
        campos.put("em", DateTimeFormatter.ISO_INSTANT.format(em));
        campos.put("hash_anterior", hashAnterior.hex());
        campos.put("ip", ip);
        campos.put("seq", seq);
        return JsonCanonico.objeto(campos);
    }
}
