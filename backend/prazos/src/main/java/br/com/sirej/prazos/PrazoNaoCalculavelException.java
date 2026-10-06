package br.com.sirej.prazos;

/**
 * O prazo do regimento não se calcula em dias corridos: vem em sessões, tem origem impressa na notificação
 * ({@code IMPRESSO_NA}) ou é meta sem prazo legal. O chamador trata cada caso com o dado que o define.
 */
public class PrazoNaoCalculavelException extends RuntimeException {

    public PrazoNaoCalculavelException(String mensagem) {
        super(mensagem);
    }
}
