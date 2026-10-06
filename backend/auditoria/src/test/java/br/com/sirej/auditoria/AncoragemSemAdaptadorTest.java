package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import br.com.sirej.auditoria.aplicacao.AncoragemDiariaService;
import br.com.sirej.auditoria.aplicacao.AncoragemRecusadaException;
import br.com.sirej.auditoria.infraestrutura.AncoraDiariaRepository;

/** PT-04: sem adaptador de carimbo (real ou simulado ligado), a ancoragem falha fechada. */
@TestPropertySource(properties = "sirej.auditoria.adaptadores-simulados=false")
class AncoragemSemAdaptadorTest extends TesteComBanco {

    @Autowired
    private AncoragemDiariaService ancoragem;

    @Autowired
    private AncoraDiariaRepository ancoras;

    @Test
    @DisplayName("PT-04: sem adaptador de carimbo do tempo, nada é ancorado")
    void PT04_sem_adaptador_de_carimbo_nada_e_ancorado() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        relogio.ajustar(INICIO.plusSeconds(86_400));

        assertThatThrownBy(() -> ancoragem.ancorar(LocalDate.ofInstant(INICIO, FUSO),
                Ator.sistema("auditoria.ancoragem"))).isInstanceOf(AncoragemRecusadaException.class);
        assertThat(ancoras.todas()).isEmpty();
    }
}
