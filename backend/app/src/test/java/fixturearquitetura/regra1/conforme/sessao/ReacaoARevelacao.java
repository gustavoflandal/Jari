// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.conforme.sessao;

import fixturearquitetura.regra1.conforme.distribuicao.DesignacaoRevelada;

/** Conforme: a sessão reage ao evento de revelação. */
public class ReacaoARevelacao {

    public String junta(DesignacaoRevelada evento) {
        return evento.juntaId();
    }
}
