// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra2.violacao.secretaria;

import fixturearquitetura.regra2.violacao.processo.dominio.Movimentacao;
import fixturearquitetura.regra2.violacao.processo.internal.ProjecaoDeSituacao;

/** Violação: a secretaria usa pacote interno e domínio do módulo processo. */
public class Triagem {

    public String avaliar(ProjecaoDeSituacao projecao, Movimentacao movimentacao) {
        return projecao.situacao() + movimentacao.ato();
    }
}
