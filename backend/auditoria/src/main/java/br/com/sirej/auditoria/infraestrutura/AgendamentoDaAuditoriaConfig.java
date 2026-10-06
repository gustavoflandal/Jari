package br.com.sirej.auditoria.infraestrutura;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Liga o agendamento das rotinas diárias da auditoria (ancoragem, verificação, partições). */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(name = "sirej.auditoria.rotinas.habilitadas", havingValue = "true", matchIfMissing = true)
class AgendamentoDaAuditoriaConfig {
}
