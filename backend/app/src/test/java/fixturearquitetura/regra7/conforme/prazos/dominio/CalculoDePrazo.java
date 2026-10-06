// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.conforme.prazos.dominio;

import java.time.LocalDate;

/** Conforme: a quantidade de dias vem de fora (RegimentoVigente); só -1, 0 e 1 como literal. */
public class CalculoDePrazo {

    public LocalDate vencimento(LocalDate inicio, int dias) {
        return inicio.plusDays(dias);
    }

    public LocalDate vespera(LocalDate data) {
        return data.minusDays(1);
    }

    public int comparar(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? -1 : (a.isEqual(b) ? 0 : 1);
    }
}
