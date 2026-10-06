package br.com.sirej.auditoria;

import java.time.Instant;
import java.util.Objects;

import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Carimbo do tempo emitido sobre um resumo.
 *
 * @param token o carimbo, em Base64 (no caso real, o {@code TimeStampToken} RFC 3161)
 * @param emissor identificação da autoridade de carimbo
 * @param instante instante atestado pelo carimbo
 */
public record CarimboTempo(String token, String emissor, Instant instante) {

    public CarimboTempo {
        if (token == null || token.isBlank() || emissor == null || emissor.isBlank()) {
            throw new ValorInvalidoException("carimbo do tempo incompleto");
        }
        Objects.requireNonNull(instante, "instante");
    }
}
