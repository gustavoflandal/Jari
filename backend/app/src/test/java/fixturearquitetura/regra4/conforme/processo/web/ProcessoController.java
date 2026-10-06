// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra4.conforme.processo.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProcessoController {

    @PreAuthorize("hasRole('SECRETARIA') and @escopo.junta(#id)")
    @GetMapping("/processos/{id}")
    public boolean consultar(String id) {
        // Fixture: não ecoa a entrada; o método privado prova que só os públicos precisam de autorização.
        return normalizar(id).isEmpty();
    }

    @SomenteSecretaria
    @GetMapping("/processos")
    public List<String> listar() {
        return List.of();
    }

    private String normalizar(String id) {
        return id.strip();
    }
}
