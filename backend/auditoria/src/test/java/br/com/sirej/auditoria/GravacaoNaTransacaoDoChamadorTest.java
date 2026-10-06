package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.IllegalTransactionStateException;

/**
 * PT-04, critério "gravação na mesma transação do chamador" (doc 08: se a auditoria falhar, o ato falha).
 */
class GravacaoNaTransacaoDoChamadorTest extends TesteComBanco {

    @BeforeEach
    void tabelaDoAto() throws SQLException {
        try (Connection conexao = comoSuperusuario(); Statement sql = conexao.createStatement()) {
            sql.execute("CREATE TABLE IF NOT EXISTS public.ato_de_teste (id text PRIMARY KEY)");
            sql.execute("GRANT SELECT, INSERT ON public.ato_de_teste TO sirej_aplicacao");
            sql.execute("TRUNCATE public.ato_de_teste");
        }
    }

    @Test
    @DisplayName("PT-04: ato e registro de auditoria são gravados juntos na transação do chamador")
    void PT04_ato_e_auditoria_gravados_juntos() {
        transacao.executeWithoutResult(status -> {
            jdbcDaAplicacao.update("INSERT INTO public.ato_de_teste (id) VALUES ('ato-1')");
            trilha.registrar(new NovoRegistroAuditoria(new Ator("usuario-1", "SECRETARIA"), null, "ATO_DE_TESTE",
                    new AlvoAuditoria("processo", "P-1")));
        });

        assertThat(atos()).isEqualTo(1);
        assertThat(contarRegistros()).isEqualTo(1);
    }

    @Test
    @DisplayName("PT-04: rollback do chamador desfaz o registro de auditoria")
    void PT04_rollback_do_chamador_desfaz_o_registro() {
        transacao.executeWithoutResult(status -> {
            jdbcDaAplicacao.update("INSERT INTO public.ato_de_teste (id) VALUES ('ato-2')");
            trilha.registrar(new NovoRegistroAuditoria(new Ator("usuario-1", "SECRETARIA"), null, "ATO_DE_TESTE",
                    new AlvoAuditoria("processo", "P-2")));
            status.setRollbackOnly();
        });

        assertThat(atos()).isZero();
        assertThat(contarRegistros()).isZero();
    }

    @Test
    @DisplayName("PT-04: falha ao gravar a auditoria desfaz o ato")
    void PT04_falha_ao_auditar_desfaz_o_ato() {
        // Instante sem partição: o INSERT na trilha falha no banco, depois de o ato já ter sido gravado.
        relogio.ajustar(Instant.parse("2099-01-15T12:00:00Z"));

        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> {
            jdbcDaAplicacao.update("INSERT INTO public.ato_de_teste (id) VALUES ('ato-3')");
            trilha.registrar(new NovoRegistroAuditoria(new Ator("usuario-1", "SECRETARIA"), null, "ATO_DE_TESTE",
                    new AlvoAuditoria("processo", "P-3")));
        })).isInstanceOf(DataAccessException.class);

        assertThat(atos()).isZero();
        assertThat(contarRegistros()).isZero();
    }

    @Test
    @DisplayName("PT-04: gravar auditoria fora de transação é recusado")
    void PT04_gravar_fora_de_transacao_e_recusado() {
        assertThatThrownBy(() -> trilha.registrar(new NovoRegistroAuditoria(new Ator("usuario-1", "SECRETARIA"),
                null, "ATO_DE_TESTE", new AlvoAuditoria("processo", "P-4"))))
                .isInstanceOf(IllegalTransactionStateException.class);

        assertThat(contarRegistros()).isZero();
    }

    private long atos() {
        Long total = jdbcDaAplicacao.queryForObject("SELECT count(*) FROM public.ato_de_teste", Long.class);
        return total == null ? 0 : total;
    }
}
