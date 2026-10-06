package br.com.sirej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
class SirejApplicationTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("PT-01: o contexto da aplicação sobe")
    void PT01_contexto_da_aplicacao_sobe() {
        assertThat(contexto.getBean(SirejApplication.class)).isNotNull();
    }

    /** PostgreSQL 17 descartável: os módulos com tabelas (PT-04 em diante) rodam as migrações na subida. */
    @TestConfiguration(proxyBeanMethods = false)
    static class BancoDeTeste {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:17-alpine");
        }
    }
}
