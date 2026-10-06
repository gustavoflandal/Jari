package br.com.sirej.configuracao;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação mínima com o módulo configuracao e o auditoria, do qual ele depende (doc 03, regra 3), para os testes
 * de módulo com banco (docs/dev/12, "Módulo" e "Integração"). Os beans de apoio (banco, cofre simulado, ouvinte de
 * eventos, auditoria que falha) ficam em {@code apoioteste}, fora do pacote varrido, e entram só quando o teste os
 * pede.
 */
@SpringBootApplication(scanBasePackages = {"br.com.sirej.configuracao", "br.com.sirej.auditoria"})
public class AplicacaoDeTeste {
}
