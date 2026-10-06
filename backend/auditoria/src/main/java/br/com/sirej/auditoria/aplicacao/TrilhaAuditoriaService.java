package br.com.sirej.auditoria.aplicacao;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirej.auditoria.NovoRegistroAuditoria;
import br.com.sirej.auditoria.RegistroAuditoriaGravado;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;
import br.com.sirej.auditoria.infraestrutura.RelogioDaAuditoria;
import br.com.sirej.compartilhado.Hash;

/**
 * Grava o próximo elo da cadeia na transação do chamador ({@link Propagation#MANDATORY}: sem transação
 * ativa, recusa). O bloqueio consultivo da cadeia vale até o fim dessa transação, então gravações
 * concorrentes ficam em fila e cada uma encadeia no elo efetivamente gravado pela anterior.
 *
 * <p>O log da aplicação recebe só a sequência e o código da ação: nunca autor, IP, alvo ou detalhe.
 */
@Service
public class TrilhaAuditoriaService implements TrilhaAuditoria {

    private static final Logger LOG = LoggerFactory.getLogger(TrilhaAuditoriaService.class);

    private final RegistroAuditoriaRepository registros;
    private final RelogioDaAuditoria relogio;

    TrilhaAuditoriaService(RegistroAuditoriaRepository registros, RelogioDaAuditoria relogio) {
        this.registros = registros;
        this.relogio = relogio;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public RegistroAuditoriaGravado registrar(NovoRegistroAuditoria novo) {
        Objects.requireNonNull(novo, "registro");
        registros.travarCadeia();
        Hash anterior = registros.ultimoHash().orElse(RegistroAuditoria.GENESE);
        long seq = registros.proximaSequencia();
        RegistroAuditoria registro = RegistroAuditoria.encadear(seq, relogio.relogio().agora(),
                novo.ator().id(), novo.ator().papel(), novo.ip(), novo.acao(), novo.alvo().tipo(),
                novo.alvo().id(), novo.detalhe(), anterior);
        registros.inserir(registro);
        LOG.debug("auditoria: registro seq={} acao={} gravado", registro.seq(), registro.acao());
        return new RegistroAuditoriaGravado(registro.seq(), registro.em(), registro.hash());
    }
}
