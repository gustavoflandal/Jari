package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    private static final DadosSinteticos DADOS = new DadosSinteticos(20261006L);

    @RepeatedTest(200)
    @DisplayName("PT-02: CPF sintético com dígitos verificadores corretos é aceito")
    void PT02_cpf_valido_e_aceito() {
        String numero = DADOS.cpf();

        assertThat(Cpf.de(numero).numero()).isEqualTo(numero);
    }

    @Test
    @DisplayName("PT-02: CPF com máscara é normalizado para 11 dígitos")
    void PT02_cpf_com_mascara_e_normalizado() {
        assertThat(Cpf.de("111.444.777-35").numero()).isEqualTo("11144477735");
        assertThat(Cpf.de(" 111.444.777-35 ")).isEqualTo(Cpf.de("11144477735"));
    }

    @RepeatedTest(200)
    @DisplayName("PT-02: CPF com dígito verificador errado é recusado")
    void PT02_cpf_com_digito_verificador_errado_e_recusado() {
        String invalido = DadosSinteticos.comDigitoVerificadorTrocado(DADOS.cpf());

        assertThatThrownBy(() -> Cpf.de(invalido)).isInstanceOf(ValorInvalidoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "00000000000", "11111111111", "99999999999", "1114447773", "111444777355",
        "1114447773A", "111 444 777 35", "111.444.777/35", "111..444.777-35x"})
    @DisplayName("PT-02: CPF vazio, repetido, com tamanho ou caractere errado é recusado")
    void PT02_cpf_mal_formado_e_recusado(String entrada) {
        assertThatThrownBy(() -> Cpf.de(entrada)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("PT-02: toString do CPF é mascarado (doc 13 proíbe CPF completo em log)")
    void PT02_tostring_do_cpf_e_mascarado() {
        Cpf cpf = Cpf.de("11144477735");

        assertThat(cpf.toString()).isEqualTo("***.444.777-**").doesNotContain("11144477735");
        assertThat(cpf.mascarado()).isEqualTo(cpf.toString());
    }

    @Test
    @DisplayName("PT-02: mensagem de erro do CPF inválido não repete o valor recebido")
    void PT02_erro_do_cpf_nao_expoe_o_valor() {
        assertThatThrownBy(() -> Cpf.de("11144477736"))
                .isInstanceOf(ValorInvalidoException.class)
                .message().doesNotContain("11144477736").doesNotContain("444");
    }
}
