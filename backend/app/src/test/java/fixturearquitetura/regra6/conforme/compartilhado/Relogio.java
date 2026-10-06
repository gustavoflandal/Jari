// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.conforme.compartilhado;

import java.time.Clock;
import java.time.Instant;

/** Conforme: o Relogio do compartilhado é o único que lê o relógio do sistema. */
public interface Relogio {

    Instant agora();

    static Relogio doSistema() {
        Clock clock = Clock.systemUTC();
        return clock::instant;
    }
}
