// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.violacao.prazos.dominio;

import java.time.LocalDate;

public class CalculoDeVencimento {

    private final int dias;

    public CalculoDeVencimento(int dias) {
        this.dias = dias;
    }

    /** Violação: lê a data do sistema. */
    public LocalDate vencimento() {
        return LocalDate.now().plusDays(dias);
    }
}
