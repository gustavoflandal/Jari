// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra6.conforme.prazos.dominio;

import java.time.Instant;
import java.time.LocalDate;

import br.com.sirej.compartilhado.Relogio;

/** Conforme: o tempo vem do Relogio injetado; datas fixas por of/parse são permitidas. */
public class CalculoDeVencimento {

    private final Relogio relogio;

    public CalculoDeVencimento(Relogio relogio) {
        this.relogio = relogio;
    }

    public LocalDate vencimento(int dias) {
        return relogio.hoje().plusDays(dias);
    }

    public boolean depoisDoMarco(String marco) {
        return relogio.agora().isAfter(Instant.parse(marco)) && relogio.hoje().isAfter(LocalDate.MIN);
    }
}
