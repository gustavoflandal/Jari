package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import br.com.sirej.auditoria.aplicacao.AncoragemDiariaService;
import br.com.sirej.auditoria.aplicacao.VerificacaoDaCadeiaService;

/**
 * PT-04: nada de dado pessoal no log da aplicação (doc 08, regra 1; doc 13). Com o módulo em DEBUG, o log
 * da gravação, da verificação com quebra e da ancoragem não traz autor, IP, alvo nem detalhe.
 */
@ExtendWith(OutputCaptureExtension.class)
class LogSemDadoPessoalTest extends TesteComBanco {

    private static final String AUTOR = "usuario-joao-silva";
    private static final String IP = "203.0.113.45";
    private static final String ALVO = "NP-77801234";
    private static final String DETALHE = "Rua das Palmeiras 100";

    @Autowired
    private VerificacaoDaCadeiaService verificacao;

    @Autowired
    private AncoragemDiariaService ancoragem;

    @Test
    @DisplayName("PT-04: o log da trilha não contém autor, IP, alvo nem detalhe")
    void PT04_log_sem_dado_pessoal(CapturedOutput saida) throws SQLException {
        RegistroAuditoriaGravado gravado = transacao.execute(status -> trilha.registrar(NovoRegistroAuditoria.de(
                new Ator(AUTOR, "RECORRENTE"), IP, "PECA_PROTOCOLADA", new AlvoAuditoria("notificacao", ALVO),
                Map.of("endereco", DETALHE))));
        relogio.ajustar(INICIO.plusSeconds(86_400));
        ancoragem.ancorar(LocalDate.ofInstant(INICIO, FUSO), Ator.sistema("auditoria.ancoragem"));
        comoSuperusuarioSemTriggers("UPDATE auditoria.registro_auditoria SET ip = '198.51.100.9' WHERE seq = "
                + gravado.seq());
        verificacao.verificarERegistrar(Ator.sistema("auditoria.verificacao"));

        assertThat(saida.getAll()).contains("seq=" + gravado.seq()).contains("ALERTA")
                .doesNotContain(AUTOR, IP, ALVO, DETALHE, "198.51.100.9");
    }
}
