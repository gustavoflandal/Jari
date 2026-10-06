package br.com.sirej.configuracao;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Regimento reprovado pelo esquema ou por regra de consistência do doc 06. Impede a subida da aplicação
 * (docs/dev/06, "Ciclo de vida").
 *
 * <p>A mensagem cita a regra violada ("regra 3 do doc 06", "esquema") e o caminho da chave. Nunca contém dado de
 * processo nem designação: só a configuração, que não é sigilosa.
 */
public class RegimentoInvalidoException extends RuntimeException {

    private final transient List<Violacao> violacoes;

    public RegimentoInvalidoException(String regimento, List<Violacao> violacoes) {
        super(mensagem(regimento, violacoes));
        this.violacoes = List.copyOf(violacoes);
    }

    public RegimentoInvalidoException(String mensagem) {
        super(mensagem);
        this.violacoes = List.of();
    }

    public List<Violacao> violacoes() {
        return violacoes;
    }

    private static String mensagem(String regimento, List<Violacao> violacoes) {
        return "Regimento '" + regimento + "' inválido:" + violacoes.stream()
                .map(v -> "\n - [" + v.regra() + "] " + v.caminho() + ": " + v.mensagem())
                .collect(Collectors.joining());
    }

    /**
     * Uma violação.
     *
     * @param regra "regra N do doc 06" ou "esquema"
     * @param caminho chave do YAML (ex.: {@code turmas.membrosPorTurma})
     * @param mensagem explicação em linguagem de negócio
     */
    public record Violacao(String regra, String caminho, String mensagem) {
        public Violacao {
            Objects.requireNonNull(regra, "regra");
            Objects.requireNonNull(caminho, "caminho");
            Objects.requireNonNull(mensagem, "mensagem");
        }

        /** Violação de regra de consistência do doc 06. */
        public static Violacao daRegra(int numero, String caminho, String mensagem) {
            return new Violacao("regra " + numero + " do doc 06", caminho, mensagem);
        }
    }
}
