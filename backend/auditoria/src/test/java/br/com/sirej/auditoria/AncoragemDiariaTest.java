package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.sirej.auditoria.aplicacao.AncoragemDiariaService;
import br.com.sirej.auditoria.aplicacao.AncoragemRecusadaException;
import br.com.sirej.auditoria.aplicacao.RotinasDaAuditoria;
import br.com.sirej.auditoria.dominio.AncoraDiaria;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.infraestrutura.AncoraDiariaRepository;
import br.com.sirej.auditoria.infraestrutura.ArmazenamentoAncoraSimulado;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;

/** PT-04, critério "ancoragem diária com carimbo simulado" (doc 08). */
class AncoragemDiariaTest extends TesteComBanco {

    private static final LocalDate DIA = LocalDate.ofInstant(INICIO, FUSO);
    private static final long UM_DIA = 86_400;

    @Autowired
    private AncoragemDiariaService ancoragem;

    @Autowired
    private AncoraDiariaRepository ancoras;

    @Autowired
    private RegistroAuditoriaRepository registros;

    @Autowired
    private ArmazenamentoAncoraSimulado armazenamento;

    @Test
    @DisplayName("PT-04: a âncora do dia guarda o hash do último registro do dia, carimbado e exportado")
    void PT04_ancora_do_dia_carimbada_e_exportada() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        RegistroAuditoriaGravado ultimoDoDia = registrarEmTransacao("ATO_DE_TESTE", "P-2");
        relogio.ajustar(INICIO.plusSeconds(UM_DIA));
        registrarEmTransacao("ATO_DE_TESTE", "P-3");

        AncoraDiaria ancora = ancoragem.ancorar(DIA, Ator.sistema("auditoria.ancoragem"));

        assertThat(ancora.seqFinal()).isEqualTo(ultimoDoDia.seq());
        assertThat(ancora.hashFinal()).isEqualTo(ultimoDoDia.hash());
        assertThat(ancora.carimbo().emissor()).contains("SIMULADA");
        assertThat(ancora.destino()).isEqualTo("simulado://ancoras/" + DIA);
        assertThat(ancoras.doDia(DIA)).contains(ancora);
        assertThat(armazenamento.exportadas()).anySatisfy(exportada -> {
            assertThat(exportada.dia()).isEqualTo(DIA);
            assertThat(exportada.hashFinal()).isEqualTo(ultimoDoDia.hash());
        });
        assertThat(registros.porAlvo("ancora_diaria", DIA.toString())).singleElement()
                .extracting(RegistroAuditoria::acao).isEqualTo(AncoragemDiariaService.ACAO);
    }

    @Test
    @DisplayName("PT-04: ancorar de novo o mesmo dia devolve a âncora existente")
    void PT04_ancoragem_e_idempotente() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        relogio.ajustar(INICIO.plusSeconds(UM_DIA));

        AncoraDiaria primeira = ancoragem.ancorar(DIA, Ator.sistema("auditoria.ancoragem"));
        AncoraDiaria segunda = ancoragem.ancorar(DIA, Ator.sistema("auditoria.ancoragem"));

        assertThat(segunda).isEqualTo(primeira);
        assertThat(ancoras.todas()).hasSize(1);
    }

    @Test
    @DisplayName("PT-04: o dia corrente não é ancorado")
    void PT04_dia_corrente_nao_e_ancorado() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");

        assertThatThrownBy(() -> ancoragem.ancorar(DIA, Ator.sistema("auditoria.ancoragem")))
                .isInstanceOf(AncoragemRecusadaException.class);
        assertThat(ancoras.todas()).isEmpty();
    }

    @Test
    @DisplayName("PT-04: cadeia rompida não é ancorada (falha fechada)")
    void PT04_cadeia_rompida_nao_e_ancorada() throws SQLException {
        RegistroAuditoriaGravado gravado = registrarEmTransacao("ATO_DE_TESTE", "P-1");
        comoSuperusuarioSemTriggers("UPDATE auditoria.registro_auditoria SET acao = 'OUTRA_ACAO' WHERE seq = "
                + gravado.seq());
        relogio.ajustar(INICIO.plusSeconds(UM_DIA));

        assertThatThrownBy(() -> ancoragem.ancorar(DIA, Ator.sistema("auditoria.ancoragem")))
                .isInstanceOf(AncoragemRecusadaException.class);
        assertThat(ancoras.todas()).isEmpty();
    }

    @Test
    @DisplayName("PT-04: a rotina ancora todos os dias encerrados pendentes")
    void PT04_rotina_ancora_dias_pendentes() {
        registrarEmTransacao("ATO_DE_TESTE", "P-1");
        relogio.ajustar(INICIO.plusSeconds(3 * UM_DIA));

        assertThat(ancoragem.diasPendentes()).containsExactly(DIA, DIA.plusDays(1), DIA.plusDays(2));
        new RotinasDeTeste(ancoragem).ancorarDiasEncerrados();

        List<AncoraDiaria> todas = ancoras.todas();
        assertThat(todas).extracting(AncoraDiaria::dia).containsExactly(DIA, DIA.plusDays(1), DIA.plusDays(2));
        assertThat(ancoragem.diasPendentes()).isEmpty();
    }

    /** Aciona a rotina agendada sem depender do agendador (desligado nos testes). */
    private record RotinasDeTeste(AncoragemDiariaService ancoragem) {

        void ancorarDiasEncerrados() {
            new RotinasDaAuditoria(ancoragem, null, null).ancorarDiasEncerrados();
        }
    }
}
