package br.com.sirej.auditoria.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.auditoria.CarimboTempo;
import br.com.sirej.compartilhado.Hash;

/** Encadeamento e verificação, sem banco (doc 08: {@code hash = SHA-256(hash_anterior ‖ JSON_canonico)}). */
class VerificadorDeCadeiaTest {

    private static final Instant EM = Instant.parse("2026-10-06T15:00:00.123456789Z");

    @Test
    @DisplayName("PT-04: o hash é determinístico e o instante é truncado em microssegundos")
    void PT04_hash_deterministico() {
        RegistroAuditoria a = registro(1, RegistroAuditoria.GENESE, "x");
        RegistroAuditoria b = registro(1, RegistroAuditoria.GENESE, "x");

        assertThat(a.hash()).isEqualTo(b.hash());
        assertThat(a.em()).isEqualTo(Instant.parse("2026-10-06T15:00:00.123456Z"));
        assertThat(a.hashRecalculado()).isEqualTo(a.hash());
        assertThat(a.canonicoSemHash()).startsWith("{\"acao\":\"ATO\",").contains("\"seq\":1}");
    }

    @Test
    @DisplayName("PT-04: qualquer campo diferente muda o hash")
    void PT04_qualquer_campo_muda_o_hash() {
        RegistroAuditoria base = registro(1, RegistroAuditoria.GENESE, "x");

        assertThat(registro(2, RegistroAuditoria.GENESE, "x").hash()).isNotEqualTo(base.hash());
        assertThat(registro(1, RegistroAuditoria.GENESE, "y").hash()).isNotEqualTo(base.hash());
        assertThat(registro(1, Hash.sha256(new byte[] {1}), "x").hash()).isNotEqualTo(base.hash());
    }

    @Test
    @DisplayName("PT-04: cadeia íntegra passa; alteração, remoção e âncora divergente são apontadas")
    void PT04_verificador_aponta_quebras() {
        List<RegistroAuditoria> cadeia = cadeia(4);
        assertThat(verificar(cadeia, List.of()).integra()).isTrue();

        List<RegistroAuditoria> alterada = new ArrayList<>(cadeia);
        RegistroAuditoria original = alterada.get(1);
        alterada.set(1, new RegistroAuditoria(original.seq(), original.em(), original.atorId(), original.atorPapel(),
                original.ip(), "OUTRA", original.alvoTipo(), original.alvoId(), original.detalhe(),
                original.hashAnterior(), original.hash()));
        assertThat(verificar(alterada, List.of()).quebra())
                .isEqualTo(new QuebraDaCadeia(2, 1L, MotivoDaQuebra.HASH_DIVERGENTE));

        List<RegistroAuditoria> semUm = new ArrayList<>(cadeia);
        semUm.remove(1);
        assertThat(verificar(semUm, List.of()).quebra())
                .isEqualTo(new QuebraDaCadeia(3, 1L, MotivoDaQuebra.ENCADEAMENTO_ROMPIDO));

        AncoraDiaria ancoraErrada = ancora(3, Hash.sha256(new byte[] {9}));
        assertThat(verificar(cadeia, List.of(ancoraErrada)).quebra())
                .isEqualTo(new QuebraDaCadeia(3, 2L, MotivoDaQuebra.ANCORA_DIVERGENTE));

        AncoraDiaria ancoraCerta = ancora(4, cadeia.get(3).hash());
        assertThat(verificar(cadeia.subList(0, 2), List.of(ancoraCerta)).quebra())
                .isEqualTo(new QuebraDaCadeia(4, 2L, MotivoDaQuebra.ANCORA_SEM_REGISTRO));
        assertThat(verificar(cadeia, List.of(ancoraCerta)).integra()).isTrue();
    }

    private static ResultadoVerificacao verificar(List<RegistroAuditoria> registros, List<AncoraDiaria> ancoras) {
        VerificadorDeCadeia verificador = new VerificadorDeCadeia(ancoras);
        for (RegistroAuditoria registro : registros) {
            if (!verificador.examinar(registro)) {
                break;
            }
        }
        return verificador.concluir();
    }

    private static List<RegistroAuditoria> cadeia(int tamanho) {
        List<RegistroAuditoria> cadeia = new ArrayList<>();
        Hash anterior = RegistroAuditoria.GENESE;
        for (int seq = 1; seq <= tamanho; seq++) {
            RegistroAuditoria registro = registro(seq, anterior, "v" + seq);
            cadeia.add(registro);
            anterior = registro.hash();
        }
        return cadeia;
    }

    private static RegistroAuditoria registro(long seq, Hash anterior, String valor) {
        TreeMap<String, String> detalhe = new TreeMap<>();
        detalhe.put("campo", valor);
        return RegistroAuditoria.encadear(seq, EM, "usuario-1", "MEMBRO", null, "ATO", "processo", "P-1", detalhe,
                anterior);
    }

    private static AncoraDiaria ancora(long seqFinal, Hash hash) {
        return new AncoraDiaria(LocalDate.of(2026, 10, 6), seqFinal, hash,
                new CarimboTempo("dG9rZW4=", "teste", EM), EM, "teste");
    }
}
