// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.violacao.prazos;

public class PrazoAnotado {

    /** Violação: prazo fixo em valor de anotação. */
    @DiasUteis(5)
    public void prazo() {
    }
}
