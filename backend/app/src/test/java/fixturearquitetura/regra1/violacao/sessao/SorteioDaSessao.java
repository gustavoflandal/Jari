// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.violacao.sessao;

import fixturearquitetura.regra1.violacao.distribuicao.dominio.SeloDistribuicao;

/** Violação: a sessão manipula o selo da distribuição. */
public class SorteioDaSessao {

    private SeloDistribuicao selo;

    public SeloDistribuicao selo() {
        return selo;
    }
}
