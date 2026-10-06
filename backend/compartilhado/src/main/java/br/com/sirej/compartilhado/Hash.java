package br.com.sirej.compartilhado;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Resumo SHA-256 (documentos, protocolo, trilha de auditoria, selo), guardado em hexadecimal minúsculo.
 *
 * <p>É o único algoritmo de resumo do produto (docs/dev/02, "Documento"; docs/dev/08, encadeamento da
 * auditoria). O tipo não sabe encadear nem canonizar: isso é de {@code auditoria} e {@code distribuicao}.
 *
 * @param hex os 64 caracteres hexadecimais, em minúsculas
 */
public record Hash(String hex) {

    private static final String ALGORITMO = "SHA-256";
    private static final Pattern HEX_SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final HexFormat HEXADECIMAL = HexFormat.of();

    public Hash {
        if (hex == null || !HEX_SHA256.matcher(hex).matches()) {
            throw new ValorInvalidoException("hash SHA-256 deve ter 64 caracteres hexadecimais minúsculos");
        }
    }

    /** Calcula o SHA-256 do conteúdo. */
    public static Hash sha256(byte[] conteudo) {
        if (conteudo == null) {
            throw new ValorInvalidoException("conteúdo ausente para o hash");
        }
        try {
            return new Hash(HEXADECIMAL.formatHex(MessageDigest.getInstance(ALGORITMO).digest(conteudo)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JDK sem SHA-256", e);
        }
    }

    /** Lê um SHA-256 em hexadecimal, em maiúsculas ou minúsculas. */
    public static Hash deHex(String hex) {
        if (hex == null) {
            throw new ValorInvalidoException("hash ausente");
        }
        return new Hash(hex.toLowerCase(Locale.ROOT));
    }

    /** Os 32 bytes do resumo; cada chamada devolve uma cópia nova. */
    public byte[] bytes() {
        return HEXADECIMAL.parseHex(hex);
    }

    @Override
    public String toString() {
        return hex;
    }
}
