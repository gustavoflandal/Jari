package br.com.sirej.auditoria.dominio;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import br.com.sirej.compartilhado.Hash;

/**
 * Percorre a trilha em ordem de {@code seq} e aponta o primeiro ponto em que ela deixa de ser íntegra.
 *
 * <p>Detecta: conteúdo alterado ({@link MotivoDaQuebra#HASH_DIVERGENTE}); registro removido, inserido ou
 * reordenado ({@link MotivoDaQuebra#ENCADEAMENTO_ROMPIDO}); e, contra as âncoras diárias já exportadas,
 * reescrita da cadeia inteira ({@link MotivoDaQuebra#ANCORA_DIVERGENTE}) ou remoção do fim
 * ({@link MotivoDaQuebra#ANCORA_SEM_REGISTRO}).
 *
 * <p>Uso: {@link #examinar} para cada registro, em ordem, e {@link #concluir} no fim.
 */
public final class VerificadorDeCadeia {

    private final Map<Long, AncoraDiaria> ancorasPorSeq = new HashMap<>();
    private Hash hashAnterior = RegistroAuditoria.GENESE;
    private Long seqAnterior;
    private long verificados;
    private QuebraDaCadeia quebra;

    /**
     * @param ancoras âncoras diárias já gravadas; cada uma fixa o hash do registro em que o dia terminou
     */
    public VerificadorDeCadeia(Iterable<AncoraDiaria> ancoras) {
        for (AncoraDiaria ancora : ancoras) {
            if (ancora.seqFinal() > 0) {
                ancorasPorSeq.merge(ancora.seqFinal(), ancora, (a, b) -> a.dia().isBefore(b.dia()) ? a : b);
            }
        }
    }

    /** Examina o próximo registro; devolve {@code false} quando a quebra já foi encontrada. */
    public boolean examinar(RegistroAuditoria registro) {
        if (quebra != null) {
            return false;
        }
        verificados++;
        if (!registro.hashAnterior().equals(hashAnterior)
                || (seqAnterior != null && registro.seq() <= seqAnterior)) {
            quebra = new QuebraDaCadeia(registro.seq(), seqAnterior, MotivoDaQuebra.ENCADEAMENTO_ROMPIDO);
            return false;
        }
        if (!registro.hashRecalculado().equals(registro.hash())) {
            quebra = new QuebraDaCadeia(registro.seq(), seqAnterior, MotivoDaQuebra.HASH_DIVERGENTE);
            return false;
        }
        AncoraDiaria ancora = ancorasPorSeq.remove(registro.seq());
        if (ancora != null && !ancora.hashFinal().equals(registro.hash())) {
            quebra = new QuebraDaCadeia(registro.seq(), seqAnterior, MotivoDaQuebra.ANCORA_DIVERGENTE);
            return false;
        }
        hashAnterior = registro.hash();
        seqAnterior = registro.seq();
        return true;
    }

    /** Resultado final; âncora cujo registro não apareceu indica remoção. */
    public ResultadoVerificacao concluir() {
        if (quebra == null && !ancorasPorSeq.isEmpty()) {
            long seqAncorado = new TreeMap<>(ancorasPorSeq).firstKey();
            quebra = new QuebraDaCadeia(seqAncorado, seqAnterior, MotivoDaQuebra.ANCORA_SEM_REGISTRO);
        }
        return new ResultadoVerificacao(verificados, seqAnterior, quebra);
    }
}
