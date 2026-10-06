// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.violacao.secretaria;

import fixturearquitetura.regra1.violacao.distribuicao.dominio.DesignacaoSemanal;

/** Violação: a secretaria lê a designação direto do domínio de distribuicao. */
public class PainelDeBacklog {

    public String relatorDe(DesignacaoSemanal designacao) {
        return designacao.posicao();
    }
}
