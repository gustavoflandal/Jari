// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.compartilhado.conforme.compartilhado;

import java.util.Objects;

/** Conforme: só JDK. */
public record Nome(String valor) {

    public Nome {
        Objects.requireNonNull(valor, "valor");
    }
}
