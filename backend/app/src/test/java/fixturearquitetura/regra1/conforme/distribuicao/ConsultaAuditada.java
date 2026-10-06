// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.conforme.distribuicao;

import fixturearquitetura.regra1.conforme.distribuicao.dominio.DesignacaoSemanal;

class ConsultaAuditada implements DesignacaoConsulta {

    private DesignacaoSemanal designacao;

    @Override
    public String obter(String processoId, String solicitante) {
        return designacao == null ? "NAO_REVELADA" : designacao.posicao();
    }
}
