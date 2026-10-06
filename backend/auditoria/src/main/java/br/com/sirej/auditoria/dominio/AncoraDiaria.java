package br.com.sirej.auditoria.dominio;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import br.com.sirej.auditoria.CarimboTempo;
import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Imutavel;

/**
 * Linha da tabela {@code auditoria.ancora_diaria} (doc 05): o hash final de um dia, carimbado e exportado
 * para fora da produção (doc 08).
 *
 * @param dia dia ancorado, no fuso da instalação
 * @param seqFinal sequência do último registro com instante anterior ao fim do dia (0 se nenhum)
 * @param hashFinal hash desse registro, ou {@link RegistroAuditoria#GENESE}
 * @param carimbo carimbo do tempo sobre {@code hashFinal}
 * @param exportadoEm quando a âncora foi exportada
 * @param destino referência do destino da exportação
 */
@Imutavel
public record AncoraDiaria(LocalDate dia, long seqFinal, Hash hashFinal, CarimboTempo carimbo, Instant exportadoEm,
        String destino) {

    public AncoraDiaria {
        Objects.requireNonNull(dia, "dia");
        Objects.requireNonNull(hashFinal, "hashFinal");
        Objects.requireNonNull(carimbo, "carimbo");
        Objects.requireNonNull(exportadoEm, "exportadoEm");
        Objects.requireNonNull(destino, "destino");
    }
}
