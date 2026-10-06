package br.com.sirej.auditoria.aplicacao;

/** A ancoragem de um dia não pôde ser feita; nada foi gravado (falha fechada). */
public class AncoragemRecusadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AncoragemRecusadaException(String mensagem) {
        super(mensagem);
    }
}
