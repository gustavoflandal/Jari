// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.violacao.processo.dominio;

import java.time.Instant;

public class Movimentacao {

    /** Violação: carimba o instante pelo relógio do sistema. */
    private final Instant em = Instant.now();

    public Instant em() {
        return em;
    }
}
