// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.pessoas;

/** Tabela comum (não imutável). */
public record Pessoa(java.util.UUID id, String email) {
}
