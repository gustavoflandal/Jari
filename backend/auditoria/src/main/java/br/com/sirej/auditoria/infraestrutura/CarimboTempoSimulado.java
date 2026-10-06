package br.com.sirej.auditoria.infraestrutura;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.com.sirej.auditoria.CarimboTempo;
import br.com.sirej.auditoria.CarimboTempoPort;
import br.com.sirej.compartilhado.Hash;

/**
 * Carimbo do tempo simulado (doc 09: toda porta tem um {@code *Simulado}). Sem validade jurídica: só
 * existe com {@code sirej.auditoria.adaptadores-simulados=true}, para desenvolvimento, testes e
 * demonstração. Sem essa propriedade e sem adaptador real, a ancoragem falha fechada.
 */
@Component
@ConditionalOnProperty(name = "sirej.auditoria.adaptadores-simulados", havingValue = "true")
public class CarimboTempoSimulado implements CarimboTempoPort {

    static final String EMISSOR = "ACT-SIMULADA (sem validade jurídica)";

    private final RelogioDaAuditoria relogio;

    CarimboTempoSimulado(RelogioDaAuditoria relogio) {
        this.relogio = relogio;
    }

    @Override
    public CarimboTempo carimbar(Hash resumo) {
        var instante = relogio.relogio().agora();
        Hash token = Hash.sha256((EMISSOR + "|" + resumo.hex() + "|" + instante).getBytes(StandardCharsets.UTF_8));
        return new CarimboTempo(Base64.getEncoder().encodeToString(token.bytes()), EMISSOR, instante);
    }
}
