package br.com.sirej.auditoria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.sirej.auditoria.aplicacao.VerificacaoDaCadeiaService;
import br.com.sirej.auditoria.dominio.ResultadoVerificacao;

/** PT-04: gravações simultâneas não quebram a cadeia (bloqueio consultivo na transação do chamador). */
class ConcorrenciaDaCadeiaTest extends TesteComBanco {

    private static final int THREADS = 8;
    private static final int POR_THREAD = 25;

    @Autowired
    private VerificacaoDaCadeiaService verificacao;

    @Test
    @DisplayName("PT-04: gravações concorrentes em várias threads mantêm a cadeia íntegra")
    void PT04_gravacoes_concorrentes_mantem_a_cadeia_integra() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<List<Long>>> resultados = new ArrayList<>();
        try {
            for (int t = 0; t < THREADS; t++) {
                int thread = t;
                resultados.add(executor.submit(() -> {
                    largada.await();
                    List<Long> seqs = new ArrayList<>();
                    for (int i = 0; i < POR_THREAD; i++) {
                        seqs.add(registrarEmTransacao("ATO_CONCORRENTE", "T" + thread + "-" + i).seq());
                    }
                    return seqs;
                }));
            }
            largada.countDown();
            List<Long> todos = new ArrayList<>();
            for (Future<List<Long>> resultado : resultados) {
                todos.addAll(resultado.get(2, TimeUnit.MINUTES));
            }

            assertThat(todos).hasSize(THREADS * POR_THREAD).doesNotHaveDuplicates();
        } finally {
            executor.shutdownNow();
        }

        ResultadoVerificacao resultado = verificacao.verificar();
        assertThat(resultado.integra()).as("quebra: %s", resultado.quebra()).isTrue();
        assertThat(resultado.registrosVerificados()).isEqualTo(THREADS * POR_THREAD);
    }
}
