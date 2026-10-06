package br.com.sirej.configuracao;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação mínima com só o módulo configuracao, para os testes de módulo com banco (docs/dev/12, "Módulo" e
 * "Integração"). Os beans de apoio (banco, cofre simulado, ouvinte de eventos) ficam em {@code apoioteste}, fora
 * do pacote varrido, e entram só quando o teste os pede.
 */
@SpringBootApplication
public class AplicacaoDeTeste {
}
