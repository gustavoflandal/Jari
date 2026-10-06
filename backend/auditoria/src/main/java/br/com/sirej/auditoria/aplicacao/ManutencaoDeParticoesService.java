package br.com.sirej.auditoria.aplicacao;

import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;
import br.com.sirej.auditoria.infraestrutura.RelogioDaAuditoria;

/**
 * Garante as partições mensais da trilha (doc 05: particionamento mensal desde a primeira migração) do mês
 * corrente até {@code sirej.auditoria.particoes.meses-a-frente} meses adiante. Roda na subida e todo dia.
 * Sem partição para o instante, a gravação falha e o ato falha junto (falha fechada).
 */
@Service
public class ManutencaoDeParticoesService {

    private final RegistroAuditoriaRepository registros;
    private final RelogioDaAuditoria relogio;
    private final int mesesAFrente;

    ManutencaoDeParticoesService(RegistroAuditoriaRepository registros, RelogioDaAuditoria relogio,
            @Value("${sirej.auditoria.particoes.meses-a-frente:3}") int mesesAFrente) {
        this.registros = registros;
        this.relogio = relogio;
        this.mesesAFrente = mesesAFrente;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void garantirParticoes() {
        LocalDate mesCorrenteUtc = LocalDate.ofInstant(relogio.relogio().agora(), ZoneOffset.UTC).withDayOfMonth(1);
        for (int i = 0; i <= mesesAFrente; i++) {
            registros.garantirParticao(mesCorrenteUtc.plusMonths(i));
        }
    }
}
