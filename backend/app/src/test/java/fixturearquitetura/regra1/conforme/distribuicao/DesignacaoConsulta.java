// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.conforme.distribuicao;

/** API pública auditada da designação (doc 07, seção 9). */
public interface DesignacaoConsulta {

    String obter(String processoId, String solicitante);
}
