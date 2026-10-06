package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.sirej.auditoria.aplicacao.ConsultaDaTrilhaRecusadaException;
import br.com.sirej.auditoria.aplicacao.ConsultaDaTrilhaService;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;
import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * RN23 (doc 04): todo ato de distribuição e toda decisão ficam nos autos, com autor nominal. O lado da
 * auditoria: o ato fica na trilha com autor identificado, ato sem autor é recusado e a trilha é consultável
 * pelo auditor, com a própria consulta registrada.
 */
class RN23AtosNaTrilhaTest extends TesteComBanco {

    @Autowired
    private ConsultaDaTrilhaService consulta;

    @Autowired
    private RegistroAuditoriaRepository registros;

    @Test
    @DisplayName("RN23: o ato fica na trilha com autor e papel identificados")
    void RN23_ato_fica_na_trilha_com_autor_identificado() {
        transacao.executeWithoutResult(status -> trilha.registrar(NovoRegistroAuditoria.de(
                new Ator("membro-0042", "MEMBRO"), "10.1.2.3", "VOTO_REGISTRADO",
                new AlvoAuditoria("processo", "2026-000123"), Map.of("resultado", "PROVIDO"))));

        List<RegistroAuditoria> encontrados = consulta.porAlvo(new Ator("auditor-01", "AUDITOR"), "10.9.9.9",
                new AlvoAuditoria("processo", "2026-000123"));

        assertThat(encontrados).singleElement().satisfies(registro -> {
            assertThat(registro.atorId()).isEqualTo("membro-0042");
            assertThat(registro.atorPapel()).isEqualTo("MEMBRO");
            assertThat(registro.acao()).isEqualTo("VOTO_REGISTRADO");
            assertThat(registro.ip()).isEqualTo("10.1.2.3");
            assertThat(registro.detalhe()).containsEntry("resultado", "PROVIDO");
        });
        assertThat(registros.porAlvo("registro_auditoria", "trilha")).singleElement()
                .satisfies(r -> assertThat(r.acao()).isEqualTo(ConsultaDaTrilhaService.ACAO_CONSULTA));
    }

    @Test
    @DisplayName("RN23: ato sem autor identificado é recusado e nada é gravado")
    void RN23_ato_sem_autor_identificado_e_recusado() {
        assertThatThrownBy(() -> new Ator(" ", "MEMBRO")).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> new Ator("membro-0042", null)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> trilha.registrar(
                new NovoRegistroAuditoria(null, null, "VOTO_REGISTRADO", new AlvoAuditoria("processo", "P-1")))))
                .isInstanceOf(ValorInvalidoException.class);

        assertThat(contarRegistros()).isZero();
    }

    @Test
    @DisplayName("RN23: consulta à trilha sem papel AUDITOR é recusada, e a tentativa fica registrada")
    void RN23_consulta_sem_papel_de_auditor_e_recusada_e_registrada() {
        registrarEmTransacao("VOTO_REGISTRADO", "P-1");

        assertThatThrownBy(() -> consulta.porAlvo(new Ator("secretaria-01", "SECRETARIA"), null,
                new AlvoAuditoria("processo", "P-1"))).isInstanceOf(ConsultaDaTrilhaRecusadaException.class);

        assertThat(registros.porAlvo("registro_auditoria", "trilha")).singleElement()
                .satisfies(r -> {
                    assertThat(r.acao()).isEqualTo(ConsultaDaTrilhaService.ACAO_RECUSA);
                    assertThat(r.atorId()).isEqualTo("secretaria-01");
                });
    }
}
