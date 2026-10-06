package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PT-04, critério "tabela imutável no banco" (doc 05 e doc 12, testes de imutabilidade): o usuário da
 * aplicação só tem SELECT e INSERT; o trigger recusa UPDATE, DELETE e TRUNCATE até para o dono.
 */
class ImutabilidadeNoBancoTest extends TesteComBanco {

    private static final String PARTICAO = "auditoria.registro_auditoria_202610";

    @BeforeEach
    void umRegistroEUmaAncora() throws SQLException {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        try (Connection conexao = comoAplicacao(); Statement sql = conexao.createStatement()) {
            sql.execute("""
                    INSERT INTO auditoria.ancora_diaria
                        (dia, seq_final, hash_final, carimbo_tempo, carimbo_emissor, carimbo_em, exportado_em, destino)
                    VALUES ('2026-10-05', 0, repeat('0', 64), 'dG9rZW4=', 'teste', now(), now(), 'teste')
                    """);
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "UPDATE auditoria.registro_auditoria SET acao = 'OUTRA_ACAO'",
            "DELETE FROM auditoria.registro_auditoria",
            "TRUNCATE auditoria.registro_auditoria",
            "UPDATE " + PARTICAO + " SET acao = 'OUTRA_ACAO'",
            "DELETE FROM " + PARTICAO,
            "TRUNCATE " + PARTICAO,
            "UPDATE auditoria.ancora_diaria SET destino = 'outro'",
            "DELETE FROM auditoria.ancora_diaria",
            "TRUNCATE auditoria.ancora_diaria"})
    @DisplayName("PT-04: usuário da aplicação não tem UPDATE, DELETE nem TRUNCATE")
    void PT04_usuario_da_aplicacao_sem_privilegio_de_alterar(String comando) throws SQLException {
        try (Connection conexao = comoAplicacao(); Statement sql = conexao.createStatement()) {
            assertThatThrownBy(() -> sql.execute(comando))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("permission denied");
        }
        assertThat(contarRegistros()).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "UPDATE auditoria.registro_auditoria SET acao = 'OUTRA_ACAO'",
            "DELETE FROM auditoria.registro_auditoria",
            "TRUNCATE auditoria.registro_auditoria",
            "UPDATE " + PARTICAO + " SET acao = 'OUTRA_ACAO'",
            "DELETE FROM " + PARTICAO,
            "TRUNCATE " + PARTICAO,
            "UPDATE auditoria.ancora_diaria SET destino = 'outro'",
            "DELETE FROM auditoria.ancora_diaria",
            "TRUNCATE auditoria.ancora_diaria"})
    @DisplayName("PT-04: o trigger recusa UPDATE, DELETE e TRUNCATE mesmo para o dono das tabelas")
    void PT04_trigger_recusa_alteracao_mesmo_para_o_dono(String comando) throws SQLException {
        try (Connection conexao = comoDono(); Statement sql = conexao.createStatement()) {
            assertThatThrownBy(() -> sql.execute(comando))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("append-only");
        }
        assertThat(contarRegistros()).isEqualTo(1);
        assertThat(jdbcDaAplicacao.queryForObject("SELECT destino FROM auditoria.ancora_diaria", String.class))
                .isEqualTo("teste");
    }

    @Test
    @DisplayName("PT-04: o usuário da aplicação inclui e lê (positivo)")
    void PT04_usuario_da_aplicacao_inclui_e_le() {
        registrarEmTransacao("ATO_DE_TESTE", "P-2");

        assertThat(contarRegistros()).isEqualTo(2);
    }
}
