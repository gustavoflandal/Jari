package br.com.sirej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class SirejApplicationTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("PT-01: o contexto da aplicação sobe")
    void PT01_contexto_da_aplicacao_sobe() {
        assertThat(contexto.getBean(SirejApplication.class)).isNotNull();
    }
}
