package br.com.sirej.auditoria.dominio;

import java.net.Inet6Address;
import java.net.InetAddress;

import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Forma única do endereço IP gravado na trilha.
 *
 * <p>O banco guarda o IP como {@code inet}, que reescreve o texto (ex.: {@code ::1}); a forma canônica que
 * entra no hash é sempre a do JDK, aplicada tanto na gravação quanto na verificação. Só literais: nunca
 * resolve nome (sem DNS).
 */
public final class EnderecoIp {

    private EnderecoIp() {
    }

    /** Valida o literal IPv4 ou IPv6 e devolve a forma canônica. */
    public static String normalizar(String literal) {
        InetAddress endereco;
        try {
            endereco = InetAddress.ofLiteral(literal.strip());
        } catch (IllegalArgumentException e) {
            throw new ValorInvalidoException("endereço IP inválido");
        }
        if (endereco instanceof Inet6Address v6 && v6.getScopeId() != 0) {
            throw new ValorInvalidoException("endereço IP com escopo não é aceito");
        }
        return endereco.getHostAddress();
    }
}
