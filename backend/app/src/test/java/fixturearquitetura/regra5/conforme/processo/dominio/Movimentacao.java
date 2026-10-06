// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.processo.dominio;

import br.com.sirej.compartilhado.Imutavel;

@Imutavel
public record Movimentacao(java.util.UUID id, java.util.UUID processoId, String ato) {
}
