package br.com.sirej.auditoria;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import br.com.sirej.auditoria.aplicacao.ManutencaoDeParticoesService;
import br.com.sirej.compartilhado.Relogio;

/**
 * Base dos testes do auditoria contra PostgreSQL 17 real (Testcontainers), com três usuários de banco:
 * <ul>
 * <li>superusuário do contêiner: o "DBA", usado só para preparar e para simular adulteração fora da
 * aplicação;</li>
 * <li>{@code sirej_dono}: dono das tabelas, roda as migrações Flyway;</li>
 * <li>{@code sirej_app_teste}: usuário da aplicação (membro de {@code sirej_aplicacao}).</li>
 * </ul>
 */
@ApplicationModuleTest
@Import(TesteComBanco.ConfiguracaoDeTeste.class)
public abstract class TesteComBanco {

    protected static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    protected static final Instant INICIO = Instant.parse("2026-10-06T15:00:00Z");

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withInitScript("banco/papeis.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry propriedades) {
        propriedades.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        propriedades.add("spring.datasource.username", () -> "sirej_app_teste");
        propriedades.add("spring.datasource.password", () -> "app");
        propriedades.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        propriedades.add("spring.flyway.user", () -> "sirej_dono");
        propriedades.add("spring.flyway.password", () -> "dono");
    }

    @Autowired
    protected TrilhaAuditoria trilha;

    @Autowired
    protected JdbcTemplate jdbcDaAplicacao;

    @Autowired
    protected TransactionTemplate transacao;

    @Autowired
    protected RelogioDeTeste relogio;

    @Autowired
    private ManutencaoDeParticoesService particoes;

    /** Cada teste parte de trilha vazia; a limpeza é feita pelo DBA, sem triggers, como num reset. */
    @BeforeEach
    void trilhaVazia() throws SQLException {
        relogio.ajustar(INICIO);
        particoes.garantirParticoes();
        comoSuperusuarioSemTriggers("DELETE FROM auditoria.registro_auditoria",
                "DELETE FROM auditoria.ancora_diaria");
    }

    /** Ato auditado numa transação própria, como faria um módulo de negócio. */
    protected RegistroAuditoriaGravado registrarEmTransacao(String acao, String alvoId) {
        return transacao.execute(status -> trilha.registrar(NovoRegistroAuditoria.de(
                new Ator("0199a1b2-membro-01", "MEMBRO"), "10.0.0.7", acao, new AlvoAuditoria("processo", alvoId),
                Map.of("situacao", "EM_PAUTA"))));
    }

    protected long contarRegistros() {
        Long total = jdbcDaAplicacao.queryForObject("SELECT count(*) FROM auditoria.registro_auditoria", Long.class);
        return total == null ? 0 : total;
    }

    /** Conexão de um usuário de banco qualquer. */
    protected static Connection conectar(String usuario, String senha) throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), usuario, senha);
    }

    protected static Connection comoDono() throws SQLException {
        return conectar("sirej_dono", "dono");
    }

    protected static Connection comoAplicacao() throws SQLException {
        return conectar("sirej_app_teste", "app");
    }

    protected static Connection comoSuperusuario() throws SQLException {
        return conectar(POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    /**
     * Executa SQL como um DBA mal-intencionado faria, fora da aplicação: superusuário com os triggers
     * desligados na sessão ({@code session_replication_role = replica}).
     */
    protected static void comoSuperusuarioSemTriggers(String... comandos) throws SQLException {
        try (Connection conexao = comoSuperusuario(); Statement sql = conexao.createStatement()) {
            sql.execute("SET session_replication_role = replica");
            for (String comando : comandos) {
                sql.execute(comando);
            }
        }
    }

    /** Relógio de teste ajustável. */
    public static final class RelogioDeTeste implements Relogio {

        private final AtomicReference<Instant> agora = new AtomicReference<>(INICIO);

        public void ajustar(Instant instante) {
            agora.set(instante);
        }

        @Override
        public Instant agora() {
            return agora.get();
        }

        @Override
        public ZoneId fuso() {
            return FUSO;
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoDeTeste {

        @Bean
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }
}
