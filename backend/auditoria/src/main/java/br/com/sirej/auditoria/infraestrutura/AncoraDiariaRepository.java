package br.com.sirej.auditoria.infraestrutura;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.sirej.auditoria.CarimboTempo;
import br.com.sirej.auditoria.dominio.AncoraDiaria;
import br.com.sirej.compartilhado.Hash;

/** Acesso à tabela {@code auditoria.ancora_diaria}. Só inclui e lê (invariantes 5 e 6). */
@Repository
public class AncoraDiariaRepository {

    private static final String COLUNAS =
            "dia, seq_final, hash_final, carimbo_tempo, carimbo_emissor, carimbo_em, exportado_em, destino";

    private final JdbcTemplate jdbc;

    AncoraDiariaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void inserir(AncoraDiaria ancora) {
        jdbc.update("INSERT INTO auditoria.ancora_diaria (" + COLUNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                ancora.dia(), ancora.seqFinal(), ancora.hashFinal().hex(), ancora.carimbo().token(),
                ancora.carimbo().emissor(), ancora.carimbo().instante().atOffset(ZoneOffset.UTC),
                ancora.exportadoEm().atOffset(ZoneOffset.UTC), ancora.destino());
    }

    public Optional<AncoraDiaria> doDia(LocalDate dia) {
        return jdbc.query("SELECT " + COLUNAS + " FROM auditoria.ancora_diaria WHERE dia = ?",
                (rs, i) -> mapear(rs), dia).stream().findFirst();
    }

    public Optional<AncoraDiaria> ultima() {
        return jdbc.query("SELECT " + COLUNAS + " FROM auditoria.ancora_diaria ORDER BY dia DESC LIMIT 1",
                (rs, i) -> mapear(rs)).stream().findFirst();
    }

    public List<AncoraDiaria> todas() {
        return jdbc.query("SELECT " + COLUNAS + " FROM auditoria.ancora_diaria ORDER BY dia", (rs, i) -> mapear(rs));
    }

    private static AncoraDiaria mapear(ResultSet rs) throws SQLException {
        return new AncoraDiaria(
                rs.getObject("dia", LocalDate.class),
                rs.getLong("seq_final"),
                new Hash(rs.getString("hash_final")),
                new CarimboTempo(rs.getString("carimbo_tempo"), rs.getString("carimbo_emissor"),
                        rs.getObject("carimbo_em", OffsetDateTime.class).toInstant()),
                rs.getObject("exportado_em", OffsetDateTime.class).toInstant(),
                rs.getString("destino"));
    }
}
