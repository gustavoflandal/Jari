// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.processo.infraestrutura;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fixturearquitetura.regra5.conforme.processo.dominio.Movimentacao;
import org.springframework.data.repository.Repository;

/** Conforme: tipo @Imutavel só com consultas. */
public interface MovimentacaoRepository extends Repository<Movimentacao, UUID> {

    Optional<Movimentacao> findById(UUID id);

    List<Movimentacao> findByProcessoId(UUID processoId);
}
