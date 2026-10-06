// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.violacao.auditoria;

import org.springframework.data.repository.Repository;

/** Violação: declara delete sobre tipo @Imutavel. */
public interface RegistroAuditoriaRepository extends Repository<RegistroAuditoria, Long> {

    void deleteById(Long seq);
}
