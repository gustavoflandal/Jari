package br.com.sirej.auditoria;

/**
 * Porta de entrada da trilha de auditoria para todos os módulos (docs/dev/03 e docs/dev/08).
 *
 * <p>A gravação faz parte da transação do ato auditado: chamar fora de uma transação ativa é recusado,
 * o rollback do chamador desfaz o registro, e uma falha ao gravar o registro propaga a exceção e desfaz
 * o ato ("se a auditoria falhar, o ato falha").
 *
 * <p>Nada aqui altera ou remove registro (invariantes 5 e 6): a trilha só cresce.
 */
public interface TrilhaAuditoria {

    /**
     * Acrescenta um registro ao fim da cadeia, na transação corrente do chamador.
     *
     * @param registro o ato, com autor identificado
     * @return número de sequência, instante e hash do registro gravado
     * @throws org.springframework.transaction.IllegalTransactionStateException se não houver transação ativa
     */
    RegistroAuditoriaGravado registrar(NovoRegistroAuditoria registro);
}
