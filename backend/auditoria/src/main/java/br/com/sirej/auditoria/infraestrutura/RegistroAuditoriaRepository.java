package br.com.sirej.auditoria.infraestrutura;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.sirej.auditoria.dominio.EnderecoIp;
import br.com.sirej.auditoria.dominio.JsonCanonico;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.compartilhado.Hash;

/**
 * Acesso à tabela {@code auditoria.registro_auditoria}. Só inclui e lê: não há, e não pode haver, método
 * que altere ou remova registro (invariantes 5 e 6; doc 12, regra 5).
 */
@Repository
public class RegistroAuditoriaRepository {

    /** Chave do bloqueio consultivo que serializa o fim da cadeia entre transações e instâncias. */
    private static final long CHAVE_BLOQUEIO_CADEIA = 0x5_1AE7_A0D1L;
    private static final int LOTE_DE_LEITURA = 1000;

    private static final String COLUNAS = """
            seq, em, ator_id, ator_papel, host(ip) AS ip, acao, alvo_tipo, alvo_id, hash_anterior, hash,
            (SELECT coalesce(array_agg(d.key ORDER BY d.key), '{}') FROM jsonb_each_text(detalhe) d) AS chaves,
            (SELECT coalesce(array_agg(d.value ORDER BY d.key), '{}') FROM jsonb_each_text(detalhe) d) AS valores
            """;

    private final JdbcTemplate jdbc;

    RegistroAuditoriaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Bloqueia o fim da cadeia até o fim da transação corrente: duas gravações concorrentes nunca leem o
     * mesmo elo anterior.
     */
    public void travarCadeia() {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(?)::text", String.class, CHAVE_BLOQUEIO_CADEIA);
    }

    /** Próximo número de sequência. */
    public long proximaSequencia() {
        Long seq = jdbc.queryForObject("SELECT nextval('auditoria.registro_auditoria_seq')", Long.class);
        return seq == null ? 0 : seq;
    }

    /** Hash do último elo, ou vazio se a trilha está vazia. */
    public Optional<Hash> ultimoHash() {
        return jdbc.query("SELECT hash FROM auditoria.registro_auditoria ORDER BY seq DESC LIMIT 1",
                (rs, i) -> new Hash(rs.getString("hash"))).stream().findFirst();
    }

    /** Último elo com instante anterior ao limite (exclusivo): o fim de um dia, para a ancoragem. */
    public Optional<RegistroAuditoria> ultimoAntesDe(Instant limite) {
        return jdbc.query("SELECT " + COLUNAS
                + " FROM auditoria.registro_auditoria WHERE em < ? ORDER BY seq DESC LIMIT 1",
                (rs, i) -> mapear(rs), utc(limite)).stream().findFirst();
    }

    /** Instante do primeiro registro, se houver. */
    public Optional<Instant> primeiroInstante() {
        OffsetDateTime primeiro = jdbc.queryForObject("SELECT min(em) FROM auditoria.registro_auditoria",
                OffsetDateTime.class);
        return Optional.ofNullable(primeiro).map(OffsetDateTime::toInstant);
    }

    /** Inclui um elo já encadeado. */
    public void inserir(RegistroAuditoria registro) {
        jdbc.update("""
                INSERT INTO auditoria.registro_auditoria
                    (seq, em, ator_id, ator_papel, ip, acao, alvo_tipo, alvo_id, detalhe, hash_anterior, hash)
                VALUES (?, ?, ?, ?, ?::inet, ?, ?, ?, ?::jsonb, ?, ?)
                """,
                registro.seq(), utc(registro.em()), registro.atorId(), registro.atorPapel(), registro.ip(),
                registro.acao(), registro.alvoTipo(), registro.alvoId(), JsonCanonico.objeto(registro.detalhe()),
                registro.hashAnterior().hex(), registro.hash().hex());
    }

    /**
     * Percorre a trilha inteira em ordem de {@code seq}, em lotes, enquanto o consumidor pedir. Precisa de
     * transação (cursor do servidor).
     */
    public void percorrerEmOrdem(Predicate<RegistroAuditoria> continuar) {
        JdbcTemplate emLotes = new JdbcTemplate(jdbc.getDataSource());
        emLotes.setFetchSize(LOTE_DE_LEITURA);
        try (Stream<RegistroAuditoria> registros = emLotes.queryForStream(
                "SELECT " + COLUNAS + " FROM auditoria.registro_auditoria ORDER BY seq", (rs, i) -> mapear(rs))) {
            registros.takeWhile(continuar).forEach(registro -> { });
        }
    }

    /** Registros de um alvo, em ordem. */
    public List<RegistroAuditoria> porAlvo(String tipo, String id) {
        return jdbc.query("SELECT " + COLUNAS
                + " FROM auditoria.registro_auditoria WHERE alvo_tipo = ? AND alvo_id = ? ORDER BY seq",
                (rs, i) -> mapear(rs), tipo, id);
    }

    /** Garante a partição mensal que contém o dia (função {@code SECURITY DEFINER} da migração). */
    public void garantirParticao(LocalDate dia) {
        jdbc.queryForList("SELECT auditoria.garantir_particao(?)::text", String.class, dia);
    }

    private static RegistroAuditoria mapear(ResultSet rs) throws SQLException {
        String[] chaves = textos(rs.getArray("chaves"));
        String[] valores = textos(rs.getArray("valores"));
        TreeMap<String, String> detalhe = new TreeMap<>();
        for (int i = 0; i < chaves.length; i++) {
            detalhe.put(chaves[i], valores[i]);
        }
        String ip = rs.getString("ip");
        return new RegistroAuditoria(
                rs.getLong("seq"),
                rs.getObject("em", OffsetDateTime.class).toInstant(),
                rs.getString("ator_id"),
                rs.getString("ator_papel"),
                ip == null ? null : EnderecoIp.normalizar(ip),
                rs.getString("acao"),
                rs.getString("alvo_tipo"),
                rs.getString("alvo_id"),
                detalhe,
                new Hash(rs.getString("hash_anterior")),
                new Hash(rs.getString("hash")));
    }

    private static String[] textos(Array array) throws SQLException {
        return array == null ? new String[0] : (String[]) array.getArray();
    }

    private static OffsetDateTime utc(Instant instante) {
        return instante.atOffset(ZoneOffset.UTC);
    }
}
