// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra5.conforme.auditoria;

/** Conforme: inclusão explícita, sem save, update ou delete. */
public class RegistroAuditoriaRepository {

    public RegistroAuditoria inserir(RegistroAuditoria registro) {
        return registro;
    }
}
