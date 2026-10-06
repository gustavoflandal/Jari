// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.violacao.processo.infraestrutura;

import java.util.UUID;

import fixturearquitetura.regra5.violacao.processo.dominio.Movimentacao;
import org.springframework.data.repository.CrudRepository;

/** Violação: herda save e delete do CrudRepository sobre tipo @Imutavel. */
public interface MovimentacaoRepository extends CrudRepository<Movimentacao, UUID> {
}
