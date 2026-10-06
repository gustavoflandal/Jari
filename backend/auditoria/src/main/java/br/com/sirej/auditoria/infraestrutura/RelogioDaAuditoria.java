package br.com.sirej.auditoria.infraestrutura;

import java.time.ZoneId;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.sirej.compartilhado.Relogio;

/**
 * Relógio usado pela auditoria: o bean {@link Relogio} da aplicação, se houver; senão, o relógio do
 * sistema no fuso técnico da instalação ({@code sirej.fuso-horario}).
 *
 * <p>Não registra bean {@link Relogio}, para não disputar com o módulo que o define.
 */
@Component
public class RelogioDaAuditoria {

    private final Relogio relogio;

    RelogioDaAuditoria(ObjectProvider<Relogio> relogios,
            @Value("${sirej.fuso-horario:America/Sao_Paulo}") String fuso) {
        this.relogio = relogios.getIfAvailable(() -> Relogio.doSistema(ZoneId.of(fuso)));
    }

    public Relogio relogio() {
        return relogio;
    }
}
