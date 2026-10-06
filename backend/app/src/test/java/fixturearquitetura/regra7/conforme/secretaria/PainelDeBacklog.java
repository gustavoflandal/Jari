// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.conforme.secretaria;

import java.util.List;

/** Conforme: sem literal numérico. */
public class PainelDeBacklog {

    public int pendentes(List<String> processos, int limite) {
        return Math.min(processos.size(), limite);
    }
}
