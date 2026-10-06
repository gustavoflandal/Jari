// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.conforme.distribuicao.dominio;

import java.util.List;

public record LoteDeDistribuicao(List<DesignacaoSemanal> designacoes, SeloDistribuicao selo, SementeDoLote semente) {
}
