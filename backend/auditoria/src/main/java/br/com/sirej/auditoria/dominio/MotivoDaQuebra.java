package br.com.sirej.auditoria.dominio;

/** Por que a cadeia deixou de ser íntegra num ponto. */
public enum MotivoDaQuebra {
    /** O registro não aponta para o elo anterior: houve remoção, inserção ou reordenação antes dele. */
    ENCADEAMENTO_ROMPIDO,
    /** O conteúdo gravado não produz o hash gravado: o registro foi alterado. */
    HASH_DIVERGENTE,
    /** O registro em que um dia ancorado terminou tem hash diferente da âncora exportada. */
    ANCORA_DIVERGENTE,
    /** O registro em que um dia ancorado terminou não existe mais. */
    ANCORA_SEM_REGISTRO
}
