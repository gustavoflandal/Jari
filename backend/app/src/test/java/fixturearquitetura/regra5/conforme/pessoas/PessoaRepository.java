// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.pessoas;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

/** Conforme: save e delete são permitidos em tipo que não é @Imutavel. */
public interface PessoaRepository extends CrudRepository<Pessoa, UUID> {
}
