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

import br.com.sirej.configuracao.RegimentoFixtures;
import br.com.sirej.configuracao.RegimentoVigente;
import br.com.sirej.configuracao.VerificadorCofreChaves;
import br.com.sirej.configuracao.VerificadorCofreChavesSimulado;

@SpringBootTest(properties = "sirej.regimento=sp")
class SirejApplicationTest {

    /** PostgreSQL 17 descartável e cofre de chaves simulado (regra de consistência 7 do doc 06; D-49). */
    @TestConfiguration(proxyBeanMethods = false)
    static class Infraestrutura {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:17-alpine");
        }

        @Bean
        VerificadorCofreChaves verificadorCofreChaves() {
            return VerificadorCofreChavesSimulado.comCofre();
        }
    }

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("PT-01: o contexto da aplicação sobe")
    void PT01_contexto_da_aplicacao_sobe() {
        assertThat(contexto.getBean(SirejApplication.class)).isNotNull();
    }

    @Test
    @DisplayName("PT-03: a aplicação sobe com o regimento de referência (sp) vigente")
    void PT03_aplicacao_sobe_com_regimento_sp_vigente() {
        assertThat(contexto.getBean(RegimentoVigente.class).regimento()).isEqualTo(RegimentoFixtures.sp());
    }
}
