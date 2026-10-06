package br.com.sirej.configuracao.infraestrutura;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import br.com.sirej.compartilhado.Relogio;

/**
 * Relógio da instalação, se ninguém declarou outro (testes declaram {@code Relogio.fixo}). O fuso é propriedade
 * técnica da instalação ({@code sirej.fuso-horario}, variável {@code SIREJ_FUSO_HORARIO}); o padrão é o mesmo
 * usado pelo módulo {@code auditoria}.
 */
@AutoConfiguration
public class RelogioAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Relogio.class)
    Relogio relogio(@Value("${sirej.fuso-horario:America/Sao_Paulo}") String fuso) {
        return Relogio.doSistema(ZoneId.of(fuso));
    }
}
