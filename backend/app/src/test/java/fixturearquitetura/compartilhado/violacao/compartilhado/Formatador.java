// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.compartilhado.violacao.compartilhado;

import org.springframework.util.StringUtils;

/** Violação: tipo do compartilhado dependendo de Spring. */
public class Formatador {

    public boolean preenchido(String texto) {
        return StringUtils.hasText(texto);
    }
}
