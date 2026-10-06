// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra4.conforme.publicidade.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint público: a abertura é declarada, não omitida. */
@RestController
public class ConsultaPublicaController {

    @PreAuthorize("permitAll()")
    @GetMapping("/publico/estatisticas")
    public String estatisticas() {
        return "{}";
    }
}
