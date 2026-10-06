package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjTest {

    private static final DadosSinteticos DADOS = new DadosSinteticos(20261007L);

    @RepeatedTest(200)
    @DisplayName("PT-02: CNPJ numérico sintético com dígitos verificadores corretos é aceito")
    void PT02_cnpj_numerico_valido_e_aceito() {
        String numero = DADOS.cnpjNumerico();

        assertThat(Cnpj.de(numero).numero()).isEqualTo(numero);
    }

    @RepeatedTest(200)
    @DisplayName("PT-02: CNPJ alfanumérico (IN RFB 2.229/2024) com dígitos verificadores corretos é aceito")
    void PT02_cnpj_alfanumerico_valido_e_aceito() {
        String numero = DADOS.cnpjAlfanumerico();

        assertThat(Cnpj.de(numero).numero()).isEqualTo(numero);
    }

    @Test
    @DisplayName("PT-02: CNPJ com máscara é normalizado; exemplo alfanumérico da Receita confere")
    void PT02_cnpj_com_mascara_e_normalizado() {
        assertThat(Cnpj.de("12.ABC.345/01DE-35").numero()).isEqualTo("12ABC34501DE35");
        assertThat(Cnpj.de("12.abc.345/01de-35")).isEqualTo(Cnpj.de("12ABC34501DE35"));
    }

    @RepeatedTest(200)
    @DisplayName("PT-02: CNPJ com dígito verificador errado é recusado")
    void PT02_cnpj_com_digito_verificador_errado_e_recusado() {
        String numerico = DadosSinteticos.comDigitoVerificadorTrocado(DADOS.cnpjNumerico());
        String alfanumerico = DadosSinteticos.comDigitoVerificadorTrocado(DADOS.cnpjAlfanumerico());

        assertThatThrownBy(() -> Cnpj.de(numerico)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> Cnpj.de(alfanumerico)).isInstanceOf(ValorInvalidoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "00000000000000", "11111111111111", "AAAAAAAAAAAA00", "1234567800019",
        "123456780001950", "12ABC34501DE3A", "12ABC34501D!35", "12ÁBC34501DE35", "12 ABC 345 01DE 35"})
    @DisplayName("PT-02: CNPJ vazio, repetido, com tamanho ou caractere errado é recusado")
    void PT02_cnpj_mal_formado_e_recusado(String entrada) {
        assertThatThrownBy(() -> Cnpj.de(entrada)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("PT-02: toString do CNPJ é mascarado")
    void PT02_tostring_do_cnpj_e_mascarado() {
        Cnpj cnpj = Cnpj.de("12ABC34501DE35");

        assertThat(cnpj.toString()).isEqualTo("**.ABC.345/****-**").doesNotContain("12ABC34501DE35");
        assertThat(cnpj.mascarado()).isEqualTo(cnpj.toString());
    }

    @Test
    @DisplayName("PT-02: mensagem de erro do CNPJ inválido não repete o valor recebido")
    void PT02_erro_do_cnpj_nao_expoe_o_valor() {
        assertThatThrownBy(() -> Cnpj.de("12ABC34501DE36"))
                .isInstanceOf(ValorInvalidoException.class)
                .message().doesNotContain("12ABC34501DE36").doesNotContain("ABC");
    }
}
