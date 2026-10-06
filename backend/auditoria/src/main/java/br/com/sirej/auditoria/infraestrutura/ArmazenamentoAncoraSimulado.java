package br.com.sirej.auditoria.infraestrutura;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import br.com.sirej.auditoria.AncoraExportavel;
import br.com.sirej.auditoria.ArmazenamentoAncoraPort;

/**
 * Armazenamento WORM simulado, em memória (doc 09: toda porta tem um {@code *Simulado}). Só existe com
 * {@code sirej.auditoria.adaptadores-simulados=true}.
 */
@Component
@ConditionalOnProperty(name = "sirej.auditoria.adaptadores-simulados", havingValue = "true")
public class ArmazenamentoAncoraSimulado implements ArmazenamentoAncoraPort {

    private final List<AncoraExportavel> exportadas = new CopyOnWriteArrayList<>();

    @Override
    public String exportar(AncoraExportavel ancora) {
        exportadas.add(ancora);
        return "simulado://ancoras/" + ancora.dia();
    }

    /** Âncoras exportadas até agora (cópia). */
    public List<AncoraExportavel> exportadas() {
        return List.copyOf(exportadas);
    }
}
