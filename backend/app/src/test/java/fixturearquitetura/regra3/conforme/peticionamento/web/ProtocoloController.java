// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra3.conforme.peticionamento.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Conforme: POST fora de distribuicao que não toca a distribuição. */
@RestController
public class ProtocoloController {

    @PreAuthorize("hasRole('CIDADAO')")
    @PostMapping("/portal/rascunhos/{id}/protocolo")
    public String protocolar(String id) {
        return id;
    }
}
