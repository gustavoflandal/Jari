// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra1.violacao.sessao;

/** Violação: tipo de semente declarado fora de distribuicao. */
public record SementeDaSessao(byte[] valor) {
}
