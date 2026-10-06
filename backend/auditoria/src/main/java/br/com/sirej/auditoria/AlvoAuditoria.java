package br.com.sirej.auditoria;

import java.util.regex.Pattern;

import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Objeto sobre o qual o ato recaiu (processo, documento, regimento, lote...).
 *
 * @param tipo nome do tipo, em minúsculas e sem acento (ex.: {@code processo})
 * @param id identificador técnico do objeto (UUID, número do processo); nunca CPF ou CNPJ
 */
public record AlvoAuditoria(String tipo, String id) {

    private static final Pattern TIPO = Pattern.compile("[a-z][a-z0-9_]{0,59}");
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:/-]{0,119}");

    public AlvoAuditoria {
        if (tipo == null || !TIPO.matcher(tipo).matches()) {
            throw new ValorInvalidoException("tipo do alvo da auditoria inválido");
        }
        if (id == null || !ID.matcher(id).matches()) {
            throw new ValorInvalidoException("identificador do alvo da auditoria inválido");
        }
        ValidacaoDeDadoPessoal.recusarSeDocumentoPessoal(id);
    }
}
