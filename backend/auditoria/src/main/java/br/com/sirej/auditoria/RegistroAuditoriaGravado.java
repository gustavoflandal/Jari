package br.com.sirej.auditoria;

import java.time.Instant;

import br.com.sirej.compartilhado.Hash;

/**
 * Comprovante de gravação na trilha: serve para o chamador citar o registro (por exemplo, o compromisso
 * do selo da distribuição, doc 07).
 *
 * @param seq número de sequência na cadeia
 * @param em instante do registro (precisão de microssegundos, a do banco)
 * @param hash hash encadeado do registro
 */
public record RegistroAuditoriaGravado(long seq, Instant em, Hash hash) {
}
