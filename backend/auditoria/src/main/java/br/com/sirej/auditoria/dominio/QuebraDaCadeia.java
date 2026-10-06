package br.com.sirej.auditoria.dominio;

import java.util.Objects;

/**
 * Ponto exato em que a cadeia deixou de ser íntegra.
 *
 * @param seq sequência do registro em que a quebra foi detectada
 * @param seqAnteriorIntegro sequência do último registro íntegro antes dele, ou {@code null} se nenhum
 * @param motivo o tipo de quebra
 */
public record QuebraDaCadeia(long seq, Long seqAnteriorIntegro, MotivoDaQuebra motivo) {

    public QuebraDaCadeia {
        Objects.requireNonNull(motivo, "motivo");
    }
}
