// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.violacao.prazos;

/** Violação: prazos fixos como argumento de constante de enum. */
public enum TipoDePrazo {
    RECURSO(30),
    DEFESA(15);

    private final int dias;

    TipoDePrazo(int dias) {
        this.dias = dias;
    }

    public int dias() {
        return dias;
    }
}
