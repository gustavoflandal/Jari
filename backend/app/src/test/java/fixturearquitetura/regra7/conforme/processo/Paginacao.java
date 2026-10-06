// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.conforme.processo;

/** Fora de prazos e secretaria: a regra 7 não se aplica. */
public class Paginacao {

    public static final int TAMANHO_PADRAO = 50;

    public int tamanho() {
        return TAMANHO_PADRAO;
    }
}
