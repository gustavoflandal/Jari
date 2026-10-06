// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.auditoria;

import br.com.sirej.compartilhado.Imutavel;

@Imutavel
public record RegistroAuditoria(Long seq, String acao) {
}
