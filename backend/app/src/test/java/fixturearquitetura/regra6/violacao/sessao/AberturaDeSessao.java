// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.violacao.sessao;

import java.time.Instant;
import java.util.function.Supplier;

public class AberturaDeSessao {

    /** Violação: referência de método também lê o relógio do sistema. */
    private final Supplier<Instant> agora = Instant::now;

    public Instant agora() {
        return agora.get();
    }
}
