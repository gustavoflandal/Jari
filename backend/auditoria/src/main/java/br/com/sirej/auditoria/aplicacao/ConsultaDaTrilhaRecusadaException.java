package br.com.sirej.auditoria.aplicacao;

/** Consulta à trilha por quem não tem o papel {@code AUDITOR}; a tentativa fica registrada. */
public class ConsultaDaTrilhaRecusadaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConsultaDaTrilhaRecusadaException() {
        super("consulta à trilha de auditoria não permitida");
    }
}
