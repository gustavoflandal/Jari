// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.violacao.prazos.dominio;

import java.time.LocalDate;

public class PrazoDeRecurso {

    /** Violação: prazo fixo no código. */
    public LocalDate vencimento(LocalDate ciencia) {
        return ciencia.plusDays(30);
    }
}
