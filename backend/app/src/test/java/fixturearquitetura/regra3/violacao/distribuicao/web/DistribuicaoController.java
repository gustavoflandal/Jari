// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra3.violacao.distribuicao.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Violação: rotas de distribuição manual (RN24). */
@RestController
@RequestMapping("/distribuicao")
public class DistribuicaoController {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lote")
    public void executarLote() {
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/processos/{id}/posicao")
    public void escolherPosicao(@PathVariable String id, String posicao) {
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping("/reprocessar")
    public void reprocessar() {
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/situacao")
    public String situacao() {
        return "OK";
    }
}
