package br.com.sirej.auditoria;

import java.time.LocalDate;

import br.com.sirej.compartilhado.Hash;

/**
 * Conteúdo exportado da âncora diária: o hash do último registro do dia, já carimbado.
 *
 * @param dia dia ancorado, no fuso da instalação
 * @param seqFinal sequência do último registro até o fim do dia (0 se a trilha ainda estiver vazia)
 * @param hashFinal hash desse registro (ou o hash de gênese)
 * @param carimbo carimbo do tempo sobre {@code hashFinal}
 */
public record AncoraExportavel(LocalDate dia, long seqFinal, Hash hashFinal, CarimboTempo carimbo) {
}
