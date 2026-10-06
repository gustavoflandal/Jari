package br.com.sirej.auditoria;

import br.com.sirej.compartilhado.Cnpj;
import br.com.sirej.compartilhado.Cpf;
import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Recusa CPF ou CNPJ em claro nos campos da trilha (D-43): a trilha é lida por auditores e exportada,
 * e o doc 08 manda guardar CPF/CNPJ só cifrado. Quem precisa referir uma pessoa usa o identificador
 * técnico dela.
 */
final class ValidacaoDeDadoPessoal {

    private ValidacaoDeDadoPessoal() {
    }

    static void recusarSeDocumentoPessoal(String valor) {
        if (ehCpf(valor) || ehCnpj(valor)) {
            throw new ValorInvalidoException("a trilha de auditoria não aceita CPF ou CNPJ em claro");
        }
    }

    private static boolean ehCpf(String valor) {
        try {
            Cpf.de(valor);
            return true;
        } catch (ValorInvalidoException e) {
            return false;
        }
    }

    private static boolean ehCnpj(String valor) {
        try {
            Cnpj.de(valor);
            return true;
        } catch (ValorInvalidoException e) {
            return false;
        }
    }
}
