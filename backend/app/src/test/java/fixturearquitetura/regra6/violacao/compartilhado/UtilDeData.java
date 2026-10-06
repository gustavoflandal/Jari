// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.violacao.compartilhado;

import java.time.Clock;
import java.time.Instant;

/** Violação: no compartilhado, só o Relogio pode ler o relógio do sistema. */
public class UtilDeData {

    public Instant agora() {
        return Clock.systemUTC().instant();
    }
}
