package br.com.sirej.auditoria.dominio;

import java.util.Optional;

/**
 * Resultado de uma verificação da cadeia.
 *
 * @param registrosVerificados quantos registros foram examinados
 * @param ultimoSeqIntegro último registro íntegro, ou {@code null}
 * @param quebra primeiro ponto de quebra, ou {@code null} se a cadeia está íntegra
 */
public record ResultadoVerificacao(long registrosVerificados, Long ultimoSeqIntegro, QuebraDaCadeia quebra) {

    public boolean integra() {
        return quebra == null;
    }

    public Optional<QuebraDaCadeia> pontoDeQuebra() {
        return Optional.ofNullable(quebra);
    }
}
