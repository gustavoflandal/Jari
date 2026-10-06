// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.violacao.auditoria;

import br.com.sirej.compartilhado.Imutavel;

@Imutavel
public record RegistroAuditoria(Long seq, String acao) {
}
