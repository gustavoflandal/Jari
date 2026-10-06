package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.compartilhado.ValorInvalidoException;

/** Validação do ato a auditar: autor identificado (RN23), formato e nenhum CPF/CNPJ em claro (D-43). */
class NovoRegistroAuditoriaTest {

    private static final Ator ATOR = new Ator("usuario-1", "SECRETARIA");
    private static final AlvoAuditoria ALVO = new AlvoAuditoria("processo", "P-1");

    @Test
    @DisplayName("PT-04: registro válido normaliza o IP e ordena o detalhe")
    void PT04_registro_valido() {
        NovoRegistroAuditoria registro = NovoRegistroAuditoria.de(ATOR, "::1", "ATO_DE_TESTE", ALVO,
                Map.of("b", "2", "a", "1"));

        assertThat(registro.ip()).isEqualTo("0:0:0:0:0:0:0:1");
        assertThat(registro.detalhe().keySet()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("PT-04: CPF ou CNPJ em claro é recusado no autor, no alvo e no detalhe (D-43)")
    void PT04_cpf_ou_cnpj_em_claro_e_recusado() {
        assertThatThrownBy(() -> new Ator("11144477735", "RECORRENTE")).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> new AlvoAuditoria("pessoa", "111.444.777-35"))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> NovoRegistroAuditoria.de(ATOR, null, "ATO_DE_TESTE", ALVO,
                Map.of("cnpj", "11.222.333/0001-81"))).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    @DisplayName("PT-04: ação, IP, chave e valor fora do formato são recusados")
    void PT04_formato_invalido_e_recusado() {
        assertThatThrownBy(() -> new NovoRegistroAuditoria(ATOR, null, "acao minuscula", ALVO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> new NovoRegistroAuditoria(ATOR, "exemplo.com", "ATO_DE_TESTE", ALVO))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> NovoRegistroAuditoria.de(ATOR, null, "ATO_DE_TESTE", ALVO, Map.of("Chave", "x")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> NovoRegistroAuditoria.de(ATOR, null, "ATO_DE_TESTE", ALVO, Map.of("k", "a\0b")))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> new NovoRegistroAuditoria(ATOR, null, "ATO_DE_TESTE", null))
                .isInstanceOf(ValorInvalidoException.class);
    }
}
