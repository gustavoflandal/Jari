// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra2.conforme.processo.internal;

import fixturearquitetura.regra2.conforme.processo.ProcessoConsulta;
import fixturearquitetura.regra2.conforme.processo.api.SituacaoDoProcesso;
import fixturearquitetura.regra2.conforme.processo.dominio.Movimentacao;

/** Conforme: o próprio módulo usa seus pacotes internos. */
public class ProjecaoDeSituacao implements ProcessoConsulta {

    private Movimentacao ultima;

    @Override
    public SituacaoDoProcesso situacao(String processoId) {
        return new SituacaoDoProcesso(ultima == null ? "SEM_MOVIMENTACAO" : ultima.ato());
    }
}
