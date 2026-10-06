// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.conforme.compartilhado;

import java.time.Instant;

public class RelogioDoSistema implements Relogio {

    @Override
    public Instant agora() {
        return Instant.now();
    }
}
