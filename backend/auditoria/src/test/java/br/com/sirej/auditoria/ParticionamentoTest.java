package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.sirej.auditoria.aplicacao.ManutencaoDeParticoesService;

/** PT-04 / doc 05: {@code registro_auditoria} é particionada por mês desde a primeira migração. */
class ParticionamentoTest extends TesteComBanco {

    @Autowired
    private ManutencaoDeParticoesService particoes;

    @Test
    @DisplayName("PT-04: registro_auditoria é particionada por intervalo de 'em'")
    void PT04_tabela_particionada_por_intervalo() {
        String estrategia = jdbcDaAplicacao.queryForObject("""
                SELECT p.partstrat::text FROM pg_partitioned_table p
                JOIN pg_class c ON c.oid = p.partrelid JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = 'auditoria' AND c.relname = 'registro_auditoria'
                """, String.class);

        assertThat(estrategia).isEqualTo("r");
    }

    @Test
    @DisplayName("PT-04: cada registro cai na partição do seu mês")
    void PT04_registro_cai_na_particao_do_mes() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        relogio.ajustar(Instant.parse("2026-11-03T12:00:00Z"));
        registrarEmTransacao("ATO_DE_TESTE", "P-2");

        assertThat(jdbcDaAplicacao.queryForList(
                "SELECT tableoid::regclass::text FROM auditoria.registro_auditoria ORDER BY seq", String.class))
                .containsExactly("auditoria.registro_auditoria_202610", "auditoria.registro_auditoria_202611");
    }

    @Test
    @DisplayName("PT-04: a manutenção cria as partições dos meses seguintes")
    void PT04_manutencao_cria_particoes_futuras() {
        relogio.ajustar(Instant.parse("2031-05-20T12:00:00Z"));

        particoes.garantirParticoes();
        relogio.ajustar(Instant.parse("2031-08-31T23:00:00Z"));
        registrarEmTransacao("ATO_DE_TESTE", "P-1");

        assertThat(jdbcDaAplicacao.queryForObject(
                "SELECT to_regclass('auditoria.registro_auditoria_203108') IS NOT NULL", Boolean.class)).isTrue();
        assertThat(contarRegistros()).isEqualTo(1);
    }
}
