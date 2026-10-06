package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

import apoioteste.configuracao.ApoioDeTeste;

/**
 * PT-03, docs/dev/06 ("Ciclo de vida"): falha de validação impede a subida; variação sintética sobe. Cada caso usa
 * um banco novo no mesmo PostgreSQL 17.
 */
class SubidaDaAplicacaoTest {

    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(ApoioDeTeste.IMAGEM_POSTGRES);

    @BeforeAll
    static void iniciar() {
        POSTGRES.start();
    }

    @AfterAll
    static void parar() {
        POSTGRES.stop();
    }

    private static String bancoNovo() {
        String nome = "t" + UUID.randomUUID().toString().replace("-", "");
        JdbcClient.create(new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
                POSTGRES.getPassword()))
                .sql("CREATE DATABASE " + nome).update();
        return POSTGRES.getJdbcUrl().replaceFirst("/[^/?]+(\\?|$)", "/" + nome + "$1");
    }

    private static ConfigurableApplicationContext subir(String url, boolean cofre, String... propriedades) {
        return subir(url, cofre, List.of(), propriedades);
    }

    private static ConfigurableApplicationContext subir(String url, boolean cofre, List<Class<?>> extras,
            String... propriedades) {
        List<Class<?>> fontes = new ArrayList<>(List.of(AplicacaoDeTeste.class));
        if (cofre) {
            fontes.add(ApoioDeTeste.CofrePresente.class);
        }
        fontes.addAll(extras);
        List<String> props = new ArrayList<>(List.of("spring.datasource.url=" + url,
                "spring.datasource.username=" + POSTGRES.getUsername(),
                "spring.datasource.password=" + POSTGRES.getPassword(), "spring.main.banner-mode=off"));
        props.addAll(List.of(propriedades));
        return new SpringApplicationBuilder(fontes.toArray(Class<?>[]::new)).properties(props.toArray(String[]::new))
                .run();
    }

    private static Throwable causaRaiz(Throwable erro) {
        Throwable atual = erro;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
    }

    @Test
    @DisplayName("RN24: regimento que viola a regra 1 (falhaFechada: false) impede a subida")
    void RN24_regimento_invalido_impede_a_subida() {
        Throwable erro = catchThrowable(() -> subir(bancoNovo(), true, "sirej.regimento=falha-aberta",
                "sirej.regimentos.local=classpath:regimentos-teste/"));

        assertThat(causaRaiz(erro)).isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("regra 1 do doc 06");
    }

    @Test
    @DisplayName("RN20: regimento sigiloso sem cofre de chaves impede a subida (regra 7)")
    void RN20_sigiloso_sem_cofre_impede_a_subida() {
        Throwable erro = catchThrowable(() -> subir(bancoNovo(), false, "sirej.regimento=sp"));

        assertThat(causaRaiz(erro)).isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("regra 7 do doc 06");
    }

    @Test
    @DisplayName("PT-03: sem sirej.regimento (SIREJ_REGIMENTO) a aplicação não sobe")
    void PT03_sem_regimento_definido_nao_sobe() {
        Throwable erro = catchThrowable(() -> subir(bancoNovo(), true));

        assertThat(causaRaiz(erro)).isInstanceOf(RegimentoInvalidoException.class)
                .hasMessageContaining("SIREJ_REGIMENTO");
    }

    @Test
    @DisplayName("PT-03: a aplicação sobe com o regimento sintético e o expõe como vigente")
    void PT03_sobe_com_regimento_sintetico() {
        try (ConfigurableApplicationContext contexto = subir(bancoNovo(), false, "sirej.regimento=sintetico",
                "sirej.regimentos.local=classpath:regimentos-teste/")) {
            Regimento r = contexto.getBean(RegimentoVigente.class).regimento();

            assertThat(r.orgao().codigo()).isEqualTo("ORGAO-SINTETICO");
            assertThat(r.resultados()).extracting(Regimento.Resultado::codigo)
                    .containsExactly("DEFERIMENTO", "INDEFERIMENTO", "NAO_CONHECIMENTO");
            assertThat(r.prazos().recurso1a().dias()).isEqualTo(45);
        }
    }

    @Test
    @DisplayName("D-48: se a auditoria falhar, a versão do regimento não fica gravada e a aplicação não sobe")
    void D48_falha_da_auditoria_desfaz_a_gravacao_da_versao() {
        String url = bancoNovo();

        Throwable erro = catchThrowable(() -> subir(url, true, List.of(ApoioDeTeste.AuditoriaQueFalha.class),
                "sirej.regimento=sp"));

        assertThat(causaRaiz(erro)).hasMessageContaining(ApoioDeTeste.AuditoriaQueFalha.MENSAGEM);
        JdbcClient banco = JdbcClient.create(new DriverManagerDataSource(url, POSTGRES.getUsername(),
                POSTGRES.getPassword()));
        assertThat(banco.sql("SELECT to_regclass('configuracao.regimento_versao') IS NOT NULL").query(Boolean.class)
                .single()).as("migrações rodaram").isTrue();
        assertThat(banco.sql("SELECT count(*) FROM configuracao.regimento_versao").query(Long.class).single())
                .isZero();
    }

    @Test
    @DisplayName("PT-03: versão gravada com hash que não confere (adulteração) impede a subida")
    void PT03_versao_adulterada_impede_a_subida() {
        String url = bancoNovo();
        try (ConfigurableApplicationContext contexto = subir(url, true, "sirej.regimento=sp")) {
            contexto.getBean(JdbcClient.class).sql("""
                    INSERT INTO configuracao.regimento_versao
                        (id, conteudo, hash, vigente_desde, aplicado_por, criado_em, criado_por)
                    SELECT gen_random_uuid(), jsonb_set(conteudo, '{retencao,anos}', '9'), hash, now(), 'X', now(), 'X'
                      FROM configuracao.regimento_versao
                    """).update();
        }

        Throwable erro = catchThrowable(() -> subir(url, true, "sirej.regimento=sp"));

        assertThat(causaRaiz(erro)).isInstanceOf(RegimentoInvalidoException.class).hasMessageContaining("adulterada");
    }
}
