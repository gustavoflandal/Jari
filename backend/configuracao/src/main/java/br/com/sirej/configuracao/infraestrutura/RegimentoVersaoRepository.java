package br.com.sirej.configuracao.infraestrutura;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Imutavel;

/**
 * Acesso à tabela imutável {@code configuracao.regimento_versao}. Só inclui e lê: não há método de atualização nem
 * de exclusão (docs/dev/12, regra 5; D-41). A imutabilidade também é garantida por trigger no banco.
 */
@Repository
public class RegimentoVersaoRepository {

    private final JdbcClient jdbc;

    public RegimentoVersaoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Inclui uma versão nova. */
    public void inserir(LinhaRegimentoVersao linha) {
        jdbc.sql("""
                INSERT INTO configuracao.regimento_versao
                    (id, conteudo, hash, vigente_desde, aplicado_por, proposta_id, criado_em, criado_por)
                VALUES (:id, CAST(:conteudo AS jsonb), :hash, :vigenteDesde, :aplicadoPor, :propostaId, :criadoEm, :criadoPor)
                """)
                .param("id", linha.id())
                .param("conteudo", linha.conteudo())
                .param("hash", linha.hash().hex())
                .param("vigenteDesde", OffsetDateTime.ofInstant(linha.vigenteDesde(), ZoneOffset.UTC))
                .param("aplicadoPor", linha.aplicadoPor())
                .param("propostaId", linha.propostaId())
                .param("criadoEm", OffsetDateTime.ofInstant(linha.criadoEm(), ZoneOffset.UTC))
                .param("criadoPor", linha.criadoPor())
                .update();
    }

    /** Todas as versões, da vigência mais antiga para a mais recente (desempate pela gravação). */
    public List<LinhaRegimentoVersao> todas() {
        return jdbc.sql("""
                SELECT id, conteudo::text AS conteudo, hash, vigente_desde, aplicado_por, proposta_id, criado_em, criado_por
                  FROM configuracao.regimento_versao
                 ORDER BY vigente_desde, criado_em, id
                """)
                .query((rs, n) -> new LinhaRegimentoVersao(
                        rs.getObject("id", UUID.class),
                        rs.getString("conteudo"),
                        Hash.deHex(rs.getString("hash")),
                        rs.getObject("vigente_desde", OffsetDateTime.class).toInstant(),
                        rs.getString("aplicado_por"),
                        rs.getObject("proposta_id", UUID.class),
                        rs.getObject("criado_em", OffsetDateTime.class).toInstant(),
                        rs.getString("criado_por")))
                .list();
    }

    /** Trava de transação que serializa a gravação de versões entre instâncias que sobem juntas. */
    public void travarGravacao() {
        jdbc.sql("SELECT pg_advisory_xact_lock(hashtext('configuracao.regimento_versao'))").query().singleRow();
    }

    /** Uma linha de {@code regimento_versao}; {@code conteudo} é o JSON gravado. */
    @Imutavel
    public record LinhaRegimentoVersao(UUID id, String conteudo, Hash hash, Instant vigenteDesde, String aplicadoPor,
            UUID propostaId, Instant criadoEm, String criadoPor) {
    }
}
