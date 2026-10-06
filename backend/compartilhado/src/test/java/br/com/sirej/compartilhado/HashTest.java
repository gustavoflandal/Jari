package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class HashTest {

    /** Vetores de teste do FIPS 180-2 (SHA-256). */
    private static final String SHA256_VAZIO = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final String SHA256_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

    @Test
    @DisplayName("PT-02: SHA-256 confere com os vetores de teste do FIPS 180-2")
    void PT02_sha256_confere_com_vetores_conhecidos() {
        assertThat(Hash.sha256(new byte[0]).hex()).isEqualTo(SHA256_VAZIO);
        assertThat(Hash.sha256("abc".getBytes(StandardCharsets.US_ASCII)).hex()).isEqualTo(SHA256_ABC);
    }

    @Test
    @DisplayName("PT-02: hash lido de hexadecimal é normalizado em minúsculas e devolve 32 bytes")
    void PT02_hash_de_hexadecimal_valido_e_aceito() {
        Hash hash = Hash.deHex(SHA256_ABC.toUpperCase(Locale.ROOT));

        assertThat(hash).isEqualTo(Hash.sha256("abc".getBytes(StandardCharsets.US_ASCII)));
        assertThat(hash.hex()).isEqualTo(SHA256_ABC);
        assertThat(hash.bytes()).hasSize(32);
        assertThat(hash.toString()).isEqualTo(SHA256_ABC);
    }

    @Test
    @DisplayName("PT-02: bytes() devolve cópia; alterar a cópia não altera o hash")
    void PT02_hash_e_imutavel() {
        Hash hash = Hash.sha256(new byte[] {1, 2, 3});
        byte[] copia = hash.bytes();
        copia[0] = (byte) ~copia[0];

        assertThat(hash.bytes()).isNotEqualTo(copia);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b85",
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b8555",
        "g3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"})
    @DisplayName("PT-02: hexadecimal que não é SHA-256 é recusado")
    void PT02_hash_mal_formado_e_recusado(String entrada) {
        assertThatThrownBy(() -> Hash.deHex(entrada)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("PT-02: SHA-256 de conteúdo nulo é recusado")
    void PT02_hash_de_conteudo_nulo_e_recusado() {
        assertThatThrownBy(() -> Hash.sha256(null)).isInstanceOf(ValorInvalidoException.class);
    }
}
