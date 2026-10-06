package apoioteste.configuracao;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.postgresql.PostgreSQLContainer;

import br.com.sirej.configuracao.RegimentoVersaoPublicada;
import br.com.sirej.configuracao.VerificadorCofreChaves;
import br.com.sirej.configuracao.VerificadorCofreChavesSimulado;

/** Beans de apoio aos testes do módulo configuracao. Fora de {@code br.com.sirej}: nunca varridos sozinhos. */
public final class ApoioDeTeste {

    /** Imagem do banco nos testes (docs/dev/05: PostgreSQL 17+). */
    public static final String IMAGEM_POSTGRES = "postgres:17-alpine";

    private ApoioDeTeste() {
    }

    /** PostgreSQL 17 descartável, ligado ao contexto por {@code @ServiceConnection}. */
    @TestConfiguration(proxyBeanMethods = false)
    public static class Banco {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer(IMAGEM_POSTGRES);
        }
    }

    /** Cofre de chaves simulado presente (regra 7). */
    @TestConfiguration(proxyBeanMethods = false)
    public static class CofrePresente {

        @Bean
        VerificadorCofreChaves verificadorCofreChaves() {
            return VerificadorCofreChavesSimulado.comCofre();
        }
    }

    /** Registra os eventos publicados e se havia transação ativa no momento. */
    @TestConfiguration(proxyBeanMethods = false)
    public static class Ouvinte {

        @Bean
        EventosRegistrados eventosRegistrados() {
            return new EventosRegistrados();
        }
    }

    /** Eventos {@link RegimentoVersaoPublicada} recebidos, com a indicação de transação ativa. */
    public static class EventosRegistrados {

        public final List<RegimentoVersaoPublicada> eventos = new CopyOnWriteArrayList<>();
        public final List<Boolean> emTransacao = new CopyOnWriteArrayList<>();

        @EventListener
        public void ao(RegimentoVersaoPublicada evento) {
            eventos.add(evento);
            emTransacao.add(TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
