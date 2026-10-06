package br.com.sirej.auditoria.aplicacao;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.sirej.auditoria.Ator;

/**
 * Rotinas diárias da auditoria: ancoragem dos dias encerrados, verificação da cadeia e manutenção das
 * partições. Horários técnicos em {@code sirej.auditoria.*.cron}, no fuso da instalação.
 *
 * <p>Ligadas por padrão; {@code sirej.auditoria.rotinas.habilitadas=false} só em testes que acionam os
 * serviços diretamente. Cada dia é ancorado na sua própria transação: um dia que falha não impede os
 * anteriores e é tentado de novo na execução seguinte.
 */
@Component
@ConditionalOnProperty(name = "sirej.auditoria.rotinas.habilitadas", havingValue = "true", matchIfMissing = true)
public class RotinasDaAuditoria {

    private static final Logger LOG = LoggerFactory.getLogger(RotinasDaAuditoria.class);

    private final AncoragemDiariaService ancoragem;
    private final VerificacaoDaCadeiaService verificacao;
    private final ManutencaoDeParticoesService particoes;

    public RotinasDaAuditoria(AncoragemDiariaService ancoragem, VerificacaoDaCadeiaService verificacao,
            ManutencaoDeParticoesService particoes) {
        this.ancoragem = ancoragem;
        this.verificacao = verificacao;
        this.particoes = particoes;
    }

    @Scheduled(cron = "${sirej.auditoria.ancoragem.cron:0 15 0 * * *}", zone = "${sirej.fuso-horario:America/Sao_Paulo}")
    public void ancorarDiasEncerrados() {
        for (LocalDate dia : ancoragem.diasPendentes()) {
            try {
                ancoragem.ancorar(dia, Ator.sistema("auditoria.ancoragem"));
            } catch (RuntimeException e) {
                LOG.error("ALERTA: ancoragem diária de {} falhou ({})", dia, e.getClass().getSimpleName());
                return;
            }
        }
    }

    @Scheduled(cron = "${sirej.auditoria.verificacao.cron:0 30 2 * * *}", zone = "${sirej.fuso-horario:America/Sao_Paulo}")
    public void verificarCadeia() {
        verificacao.verificarERegistrar(Ator.sistema("auditoria.verificacao"));
    }

    @Scheduled(cron = "${sirej.auditoria.particoes.cron:0 0 1 * * *}", zone = "${sirej.fuso-horario:America/Sao_Paulo}")
    public void manterParticoes() {
        particoes.garantirParticoes();
    }
}
