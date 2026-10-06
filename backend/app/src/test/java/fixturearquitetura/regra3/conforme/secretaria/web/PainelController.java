// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra3.conforme.secretaria.web;

import fixturearquitetura.regra3.conforme.distribuicao.DesignacaoConsulta;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Conforme: controller de outro módulo usa só a consulta auditada. */
@RestController
public class PainelController {

    private final DesignacaoConsulta consulta;

    public PainelController(DesignacaoConsulta consulta) {
        this.consulta = consulta;
    }

    @PreAuthorize("hasRole('SECRETARIA')")
    @GetMapping("/painel/{processoId}")
    public String situacao(String processoId) {
        return consulta.obter(processoId, "secretaria");
    }
}
