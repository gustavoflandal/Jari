package br.com.sirej.configuracao.aplicacao;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import br.com.sirej.auditoria.AlvoAuditoria;
import br.com.sirej.auditoria.Ator;
import br.com.sirej.auditoria.NovoRegistroAuditoria;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.compartilhado.Relogio;
import br.com.sirej.configuracao.RegimentoInvalidoException;
import br.com.sirej.configuracao.RegimentoVersao;
import br.com.sirej.configuracao.RegimentoVersaoPublicada;
import br.com.sirej.configuracao.RegimentoVigente;
import br.com.sirej.configuracao.VerificadorCofreChaves;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento.RegimentoLido;
import br.com.sirej.configuracao.infraestrutura.RegimentoVersaoRepository;
import br.com.sirej.configuracao.infraestrutura.RegimentoVersaoRepository.LinhaRegimentoVersao;

/**
 * Carga do regimento na subida e implementação de {@link RegimentoVigente} (docs/dev/06, "Ciclo de vida";
 * docs/dev/18, seção 6).
 *
 * <ol>
 *   <li>Lê o regimento do pacote ({@code sirej.regimento}, variável {@code SIREJ_REGIMENTO}), resolve
 *       {@code herda} e valida esquema e regras de consistência. Falha impede a subida.</li>
 *   <li>Banco vazio: o regimento do pacote vira a primeira {@code regimento_versao}, aplicada pelo
 *       {@code INSTALADOR}; na mesma transação, o registro de auditoria {@value #ACAO_VERSAO_CRIADA} é gravado
 *       na trilha e {@link RegimentoVersaoPublicada} é publicado. Se a auditoria falhar, a versão não fica
 *       gravada e a subida falha (D-48).</li>
 *   <li>Banco com versões: o banco é a fonte de verdade. Mesmo hash, a versão existente é reaproveitada; hash
 *       diferente gera alerta de "configuração do pacote divergente" e nada é aplicado (ADR-0011).</li>
 *   <li>Toda versão gravada tem o hash conferido contra o conteúdo e é validada de novo; versão adulterada ou
 *       inválida impede a subida.</li>
 * </ol>
 */
@Service
@DependsOnDatabaseInitialization
public class CargaDoRegimento implements RegimentoVigente, SmartLifecycle {

    /** Quem aplica a primeira versão (docs/dev/18, seção 6). */
    public static final String INSTALADOR = "INSTALADOR";

    /** Ação na trilha de auditoria ao criar uma {@code regimento_versao} (doc 08: alteração de configuração). */
    public static final String ACAO_VERSAO_CRIADA = "REGIMENTO_VERSAO_CRIADA";

    /** Tipo do alvo na trilha de auditoria. */
    public static final String ALVO_REGIMENTO_VERSAO = "regimento_versao";

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private static final Logger LOG = LoggerFactory.getLogger(CargaDoRegimento.class);

    private final LeitorDeRegimento leitor;
    private final RegimentoVersaoRepository repositorio;
    private final TrilhaAuditoria trilha;
    private final TransactionTemplate transacao;
    private final ApplicationEventPublisher eventos;
    private final Relogio relogio;
    private final ObjectProvider<VerificadorCofreChaves> cofres;
    private final String nome;

    private volatile List<RegimentoVersao> versoes = List.of();

    public CargaDoRegimento(LeitorDeRegimento leitor, RegimentoVersaoRepository repositorio, TrilhaAuditoria trilha,
            TransactionTemplate transacao, ApplicationEventPublisher eventos, Relogio relogio,
            ObjectProvider<VerificadorCofreChaves> cofres, @Value("${sirej.regimento:}") String nome) {
        this.leitor = leitor;
        this.repositorio = repositorio;
        this.trilha = trilha;
        this.transacao = transacao;
        this.eventos = eventos;
        this.relogio = relogio;
        this.cofres = cofres;
        this.nome = nome;
    }

    private volatile boolean carregado;

    /**
     * A carga roda no início do ciclo de vida, depois de todos os beans e ouvintes de eventos existirem (o evento
     * {@link RegimentoVersaoPublicada} precisa alcançá-los) e antes de qualquer outro componente com ciclo de vida.
     * Falha aqui impede a subida. Nenhum bean deve ler o regimento durante a própria inicialização.
     */
    @Override
    public void start() {
        carregar();
        carregado = true;
    }

    @Override
    public void stop() {
        carregado = false;
    }

    @Override
    public boolean isRunning() {
        return carregado;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

    /** Executa a carga (na subida). */
    public final void carregar() {
        if (nome == null || nome.isBlank()) {
            throw new RegimentoInvalidoException(
                    "regimento da instalação não definido: informe sirej.regimento (SIREJ_REGIMENTO), ex.: sp");
        }
        boolean cofre = cofreDeChavesDisponivel();
        RegimentoLido doPacote = leitor.ler(nome, cofre);
        transacao.executeWithoutResult(status -> aplicarSeBancoVazio(doPacote));
        List<RegimentoVersao> carregadas = repositorio.todas().stream().map(linha -> conferir(linha, cofre)).toList();
        this.versoes = carregadas;
        versao();
    }

    @Override
    public RegimentoVersao versao() {
        if (versoes.isEmpty()) {
            throw new IllegalStateException("regimento ainda não carregado");
        }
        Instant agora = relogio.agora();
        return versoes.stream()
                .filter(v -> !v.vigenteDesde().isAfter(agora))
                .max(Comparator.comparing(RegimentoVersao::vigenteDesde))
                .orElseThrow(() -> new IllegalStateException("nenhuma versão do regimento vigente em " + agora));
    }

    @Override
    public Optional<RegimentoVersao> versao(UUID id) {
        return versoes.stream().filter(v -> v.id().equals(id)).findFirst();
    }

    private void aplicarSeBancoVazio(RegimentoLido doPacote) {
        repositorio.travarGravacao();
        List<LinhaRegimentoVersao> existentes = repositorio.todas();
        if (existentes.isEmpty()) {
            Instant agora = relogio.agora();
            LinhaRegimentoVersao nova = new LinhaRegimentoVersao(uuidV7(agora), doPacote.conteudoCanonico(),
                    doPacote.hash(), agora, INSTALADOR, null, agora, INSTALADOR);
            repositorio.inserir(nova);
            auditar(nova);
            eventos.publishEvent(new RegimentoVersaoPublicada(nova.id(), nova.hash(), nova.vigenteDesde(),
                    INSTALADOR, agora));
            LOG.info("Regimento '{}' aplicado como primeira versão {} (hash {})", nome, nova.id(), nova.hash());
            return;
        }
        LinhaRegimentoVersao ultima = existentes.getLast();
        if (ultima.hash().equals(doPacote.hash())) {
            LOG.info("Regimento '{}' do pacote igual à versão {}; versão reaproveitada", nome, ultima.id());
        } else {
            LOG.warn("Configuração do pacote divergente: regimento '{}' do pacote tem hash {}, a última versão {}"
                    + " tem hash {}. O banco prevalece; o YAML só entra por importação aprovada (ADR-0011).",
                    nome, doPacote.hash(), ultima.id(), ultima.hash());
        }
    }

    /**
     * Registro de auditoria da criação da versão, na transação da gravação (D-48). Autor técnico, sem dado pessoal;
     * detalhe é mapa plano de texto (D-43).
     */
    private void auditar(LinhaRegimentoVersao nova) {
        trilha.registrar(NovoRegistroAuditoria.de(new Ator(INSTALADOR, Ator.PAPEL_SISTEMA), null, ACAO_VERSAO_CRIADA,
                new AlvoAuditoria(ALVO_REGIMENTO_VERSAO, nova.id().toString()),
                Map.of("hash", nova.hash().hex(), "regimento", nome, "aplicado_por", nova.aplicadoPor(),
                        "vigente_desde", nova.vigenteDesde().toString())));
    }

    private RegimentoVersao conferir(LinhaRegimentoVersao linha, boolean cofre) {
        if (!leitor.hashDoConteudo(nome, linha.conteudo()).equals(linha.hash())) {
            throw new RegimentoInvalidoException("regimento_versao " + linha.id()
                    + " adulterada: o hash gravado não confere com o conteúdo");
        }
        RegimentoLido lido = leitor.lerConteudoCanonico(nome, linha.conteudo(), cofre);
        return new RegimentoVersao(linha.id(), linha.hash(), linha.vigenteDesde(), linha.aplicadoPor(),
                lido.regimento());
    }

    /** UUIDv7 (RFC 9562): tempo em milissegundos + aleatório (docs/dev/05, "Convenções"). */
    private static UUID uuidV7(Instant instante) {
        long msb = (instante.toEpochMilli() << 16) | 0x7000L | (ALEATORIO.nextInt() & 0x0FFF);
        long lsb = (ALEATORIO.nextLong() & 0x3FFFFFFFFFFFFFFFL) | Long.MIN_VALUE;
        return new UUID(msb, lsb);
    }

    private boolean cofreDeChavesDisponivel() {
        List<VerificadorCofreChaves> verificadores = cofres.orderedStream().toList();
        if (verificadores.isEmpty()) {
            return false;
        }
        for (VerificadorCofreChaves verificador : verificadores) {
            try {
                if (!verificador.disponivel()) {
                    return false;
                }
            } catch (RuntimeException e) {
                LOG.warn("Verificação do cofre de chaves falhou: {}", e.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }
}
