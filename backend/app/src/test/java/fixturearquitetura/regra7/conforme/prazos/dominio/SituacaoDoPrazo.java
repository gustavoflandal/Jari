// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra7.conforme.prazos.dominio;

/** Conforme: os ordinais que o compilador gera para as constantes não contam como literal. */
public enum SituacaoDoPrazo {
    NAO_INICIADO,
    EM_CURSO,
    SUSPENSO,
    PRORROGADO,
    CUMPRIDO,
    VENCIDO,
    CANCELADO,
    ENCERRADO,
    ESPECIAL {
        @Override
        public boolean especial() {
            return true;
        }
    };

    public boolean especial() {
        return false;
    }
}
