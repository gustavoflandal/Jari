// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra4.violacao.sessao.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Violação: autorização só na classe; o doc 12 exige no método. */
@RestController
@PreAuthorize("hasRole('PRESIDENTE')")
public class SessaoController {

    @PostMapping("/sessoes/{id}/abertura")
    public void abrir(String id) {
    }
}
