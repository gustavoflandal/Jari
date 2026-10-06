// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra4.violacao.processo.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessoController {

    /** Violação: endpoint sem autorização declarada. */
    @GetMapping("/processos/{id}")
    public String consultar(String id) {
        return id;
    }

    @PreAuthorize("hasRole('SECRETARIA')")
    @GetMapping("/processos")
    public List<String> listar() {
        return List.of();
    }
}
