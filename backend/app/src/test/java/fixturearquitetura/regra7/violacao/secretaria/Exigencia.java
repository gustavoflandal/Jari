// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.violacao.secretaria;

import java.time.LocalDate;
import java.util.function.UnaryOperator;

public class Exigencia {

    /** Violação: constante de prazo (o compilador também a copia para quem a usa). */
    static final int PRAZO_EXIGENCIA = 15;

    /** Violação: literal dentro de lambda. */
    private final UnaryOperator<LocalDate> maisDez = data -> data.plusDays(10);

    public LocalDate vencimento(LocalDate inicio) {
        return maisDez.apply(inicio.plusDays(PRAZO_EXIGENCIA));
    }

    /** Violação: literal long. */
    public LocalDate vencimentoLongo(LocalDate inicio) {
        return inicio.plusDays(60L);
    }
}
