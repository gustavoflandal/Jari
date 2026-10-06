// Fixture do PT-02 (docs/dev/12): violação plantada. Código de teste; nunca vai para src/main.
package fixturearquitetura.regra3.violacao.secretaria.web;

import fixturearquitetura.regra3.violacao.distribuicao.DistribuicaoSemanal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** Violação: controller de outro módulo dispara o lote de distribuição (RN24). */
@RestController
public class SuporteController {

    private final DistribuicaoSemanal distribuicao = new DistribuicaoSemanal();

    @PreAuthorize("hasRole('SUPORTE')")
    @PostMapping("/suporte/redistribuir")
    public void redistribuir() {
        distribuicao.executar();
    }
}
