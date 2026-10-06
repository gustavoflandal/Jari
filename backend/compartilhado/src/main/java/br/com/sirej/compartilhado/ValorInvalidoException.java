package br.com.sirej.compartilhado;

/**
 * Erro base para valor que não atende ao formato de um tipo de valor (CPF, CNPJ, placa, hash).
 *
 * <p>A mensagem descreve só o tipo e o motivo; nunca repete o valor recebido, que pode ser dado
 * pessoal (docs/dev/08, "Regras para quem escreve código", e docs/dev/13, "Logs e observabilidade").
 */
public class ValorInvalidoException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
