// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra2.conforme.secretaria;

import fixturearquitetura.regra2.conforme.processo.ProcessoConsulta;
import fixturearquitetura.regra2.conforme.processo.api.SituacaoDoProcesso;
import fixturearquitetura.regra2.conforme.secretaria.internal.FilaDeTriagem;

/** Conforme: usa só a API pública de processo e o próprio pacote interno. */
public class Triagem {

    private final FilaDeTriagem fila = new FilaDeTriagem();

    public SituacaoDoProcesso avaliar(ProcessoConsulta consulta, String processoId) {
        return fila.vazia() ? consulta.situacao(processoId) : null;
    }
}
