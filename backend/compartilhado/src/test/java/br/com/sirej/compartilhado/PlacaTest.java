package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PlacaTest {

    @ParameterizedTest
    @CsvSource({
        "ABC1D23, ABC1D23, MERCOSUL",
        "abc1d23, ABC1D23, MERCOSUL",
        "' ABC1D23 ', ABC1D23, MERCOSUL",
        "ABC1234, ABC1234, ANTIGA",
        "ABC-1234, ABC1234, ANTIGA",
        "xyz-0001, XYZ0001, ANTIGA"
    })
    @DisplayName("PT-02: placa nos padrões Mercosul e antigo é aceita e normalizada")
    void PT02_placa_valida_e_aceita(String entrada, String normalizada, Placa.Formato formato) {
        Placa placa = Placa.de(entrada);

        assertThat(placa.valor()).isEqualTo(normalizada);
        assertThat(placa.formato()).isEqualTo(formato);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "AB1234", "ABCD123", "ABC12345", "1BC1234", "ABC1DD3", "ABCD1234", "ABÇ1234",
        "ABC-1D2", "ABC_1234", "ABC1D2E", "ABC 1234", "AB-C1234", "ABC--1234"})
    @DisplayName("PT-02: placa fora dos padrões Mercosul e antigo é recusada")
    void PT02_placa_mal_formada_e_recusada(String entrada) {
        assertThatThrownBy(() -> Placa.de(entrada)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("PT-02: toString da placa é mascarado e a igualdade usa o valor normalizado")
    void PT02_tostring_da_placa_e_mascarado() {
        assertThat(Placa.de("ABC1D23").toString()).isEqualTo("ABC****");
        assertThat(Placa.de("abc-1234")).isEqualTo(Placa.de("ABC1234"));
    }
}
