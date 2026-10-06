// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.conforme.secretaria;

import fixturearquitetura.regra1.conforme.distribuicao.DesignacaoConsulta;

/** Conforme: a secretaria só usa a consulta auditada. */
public class PainelDeBacklog {

    private final DesignacaoConsulta consulta;

    public PainelDeBacklog(DesignacaoConsulta consulta) {
        this.consulta = consulta;
    }

    public String situacao(String processoId) {
        return consulta.obter(processoId, "secretaria");
    }
}
