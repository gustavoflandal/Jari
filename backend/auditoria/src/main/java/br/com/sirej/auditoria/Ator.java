package br.com.sirej.auditoria;

import java.util.regex.Pattern;

import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Autor identificado de um ato auditado (RN23: atos e decisões têm autor nominal).
 *
 * <p>{@code id} é o identificador estável do usuário no SIREJ, nunca CPF, nome ou e-mail: o registro não
 * guarda dado pessoal em claro. Rotinas automáticas usam {@link #sistema(String)} e só registram atos
 * técnicos, nunca decisão (invariante 10).
 *
 * @param id identificador do usuário ou da rotina
 * @param papel papel com que o ato foi praticado (doc 08), ou {@code SISTEMA}
 */
public record Ator(String id, String papel) {

    /** Papel das rotinas automáticas do próprio sistema. */
    public static final String PAPEL_SISTEMA = "SISTEMA";

    private static final Pattern ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._:-]{0,99}");
    private static final Pattern PAPEL = Pattern.compile("[A-Z][A-Z0-9_]{0,59}");

    public Ator {
        if (id == null || !ID.matcher(id).matches()) {
            throw new ValorInvalidoException("ato auditado exige autor identificado");
        }
        if (papel == null || !PAPEL.matcher(papel).matches()) {
            throw new ValorInvalidoException("ato auditado exige o papel do autor");
        }
        ValidacaoDeDadoPessoal.recusarSeDocumentoPessoal(id);
    }

    /** Rotina automática do sistema, identificada pelo nome (ex.: {@code auditoria.ancoragem}). */
    public static Ator sistema(String rotina) {
        return new Ator("sistema:" + rotina, PAPEL_SISTEMA);
    }
}
