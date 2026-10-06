// Fixture do PT-02 (docs/dev/12): exemplo conforme. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra3.conforme.distribuicao.web;

import fixturearquitetura.regra3.conforme.distribuicao.DesignacaoConsulta;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Conforme: distribuicao só expõe leitura. */
@RestController
@RequestMapping("/designacoes")
public class DesignacaoController {

    private final DesignacaoConsulta consulta;

    public DesignacaoController(DesignacaoConsulta consulta) {
        this.consulta = consulta;
    }

    @PreAuthorize("hasRole('MEMBRO')")
    @GetMapping("/{processoId}")
    public String obter(@PathVariable String processoId) {
        return consulta.obter(processoId, "membro");
    }

    @PreAuthorize("hasRole('MEMBRO')")
    @RequestMapping(path = "/{processoId}", method = {RequestMethod.HEAD, RequestMethod.OPTIONS})
    public void cabecalho(@PathVariable String processoId) {
    }
}
