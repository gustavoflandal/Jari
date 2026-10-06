// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra2.conforme.processo;

import fixturearquitetura.regra2.conforme.processo.api.SituacaoDoProcesso;

/** API pública do módulo processo (pacote raiz). */
public interface ProcessoConsulta {

    SituacaoDoProcesso situacao(String processoId);
}
