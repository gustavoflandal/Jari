package br.com.sirej.auditoria.aplicacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirej.auditoria.AlvoAuditoria;
import br.com.sirej.auditoria.AncoraExportavel;
import br.com.sirej.auditoria.ArmazenamentoAncoraPort;
import br.com.sirej.auditoria.Ator;
import br.com.sirej.auditoria.CarimboTempo;
import br.com.sirej.auditoria.CarimboTempoPort;
import br.com.sirej.auditoria.NovoRegistroAuditoria;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.auditoria.dominio.AncoraDiaria;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.dominio.ResultadoVerificacao;
import br.com.sirej.auditoria.infraestrutura.AncoraDiariaRepository;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;
import br.com.sirej.auditoria.infraestrutura.RelogioDaAuditoria;
import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Relogio;

/**
 * Ancoragem diária (doc 08): o hash do último registro de um dia encerrado é carimbado, exportado para
 * fora da produção e gravado em {@code ancora_diaria}, com registro na própria trilha.
 *
 * <p>Falha fechada: cadeia rompida, carimbo indisponível ou exportação com erro fazem a ancoragem do dia
 * falhar inteira (nada é gravado); a rotina tenta de novo na execução seguinte e o atraso gera alerta.
 */
@Service
public class AncoragemDiariaService {

    /** Código da ação registrada na trilha a cada âncora. */
    public static final String ACAO = "ANCORA_DIARIA_REGISTRADA";

    private static final Logger LOG = LoggerFactory.getLogger(AncoragemDiariaService.class);

    private final RegistroAuditoriaRepository registros;
    private final AncoraDiariaRepository ancoras;
    private final VerificacaoDaCadeiaService verificacao;
    private final TrilhaAuditoria trilha;
    private final ObjectProvider<CarimboTempoPort> carimbos;
    private final ObjectProvider<ArmazenamentoAncoraPort> armazenamentos;
    private final RelogioDaAuditoria relogio;

    AncoragemDiariaService(RegistroAuditoriaRepository registros, AncoraDiariaRepository ancoras,
            VerificacaoDaCadeiaService verificacao, TrilhaAuditoria trilha, ObjectProvider<CarimboTempoPort> carimbos,
            ObjectProvider<ArmazenamentoAncoraPort> armazenamentos, RelogioDaAuditoria relogio) {
        this.registros = registros;
        this.ancoras = ancoras;
        this.verificacao = verificacao;
        this.trilha = trilha;
        this.carimbos = carimbos;
        this.armazenamentos = armazenamentos;
        this.relogio = relogio;
    }

    /**
     * Ancora um dia já encerrado. Se o dia já tem âncora, devolve a existente (idempotente).
     *
     * @throws AncoragemRecusadaException se o dia não terminou, a cadeia está rompida ou falta adaptador
     */
    @Transactional
    public AncoraDiaria ancorar(LocalDate dia, Ator ator) {
        Relogio agora = relogio.relogio();
        if (!dia.isBefore(agora.hoje())) {
            throw new AncoragemRecusadaException("só dia encerrado pode ser ancorado");
        }
        Optional<AncoraDiaria> existente = ancoras.doDia(dia);
        if (existente.isPresent()) {
            return existente.get();
        }
        CarimboTempoPort carimbador = carimbos.getIfAvailable();
        ArmazenamentoAncoraPort armazenamento = armazenamentos.getIfAvailable();
        if (carimbador == null || armazenamento == null) {
            LOG.error("ALERTA: ancoragem de {} sem adaptador de carimbo do tempo ou de armazenamento", dia);
            throw new AncoragemRecusadaException("ancoragem sem adaptador de carimbo ou de armazenamento");
        }
        ResultadoVerificacao resultado = verificacao.verificar();
        if (!resultado.integra()) {
            LOG.error("ALERTA: ancoragem de {} recusada, cadeia rompida em seq={} motivo={}", dia,
                    resultado.quebra().seq(), resultado.quebra().motivo());
            throw new AncoragemRecusadaException("cadeia de auditoria rompida; ancoragem recusada");
        }

        Instant fimDoDia = dia.plusDays(1).atStartOfDay(agora.fuso()).toInstant();
        Optional<RegistroAuditoria> ultimo = registros.ultimoAntesDe(fimDoDia);
        long seqFinal = ultimo.map(RegistroAuditoria::seq).orElse(0L);
        Hash hashFinal = ultimo.map(RegistroAuditoria::hash).orElse(RegistroAuditoria.GENESE);

        CarimboTempo carimbo = carimbador.carimbar(hashFinal);
        String destino = armazenamento.exportar(new AncoraExportavel(dia, seqFinal, hashFinal, carimbo));
        AncoraDiaria ancora = new AncoraDiaria(dia, seqFinal, hashFinal, carimbo, agora.agora(), destino);
        ancoras.inserir(ancora);

        TreeMap<String, String> detalhe = new TreeMap<>();
        detalhe.put("seq_final", Long.toString(seqFinal));
        detalhe.put("hash_final", hashFinal.hex());
        detalhe.put("destino", destino);
        trilha.registrar(new NovoRegistroAuditoria(ator, null, ACAO, new AlvoAuditoria("ancora_diaria",
                dia.toString()), detalhe));
        LOG.info("auditoria: âncora de {} gravada (seq final {})", dia, seqFinal);
        return ancora;
    }

    /** Dias encerrados ainda sem âncora, do dia seguinte à última âncora (ou do primeiro registro) até ontem. */
    @Transactional(readOnly = true)
    public List<LocalDate> diasPendentes() {
        Relogio agora = relogio.relogio();
        LocalDate ontem = agora.hoje().minusDays(1);
        Optional<LocalDate> inicio = ancoras.ultima().map(a -> a.dia().plusDays(1))
                .or(() -> registros.primeiroInstante().map(i -> LocalDate.ofInstant(i, agora.fuso())));
        if (inicio.isEmpty() || inicio.get().isAfter(ontem)) {
            return List.of();
        }
        return inicio.get().datesUntil(ontem.plusDays(1)).toList();
    }
}
