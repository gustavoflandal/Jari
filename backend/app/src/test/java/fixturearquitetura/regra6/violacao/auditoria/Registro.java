// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.violacao.auditoria;

import java.util.Date;

public class Registro {

    /** Violação: APIs antigas de relógio. */
    public long carimbo() {
        return System.currentTimeMillis() + new Date().getTime();
    }
}
