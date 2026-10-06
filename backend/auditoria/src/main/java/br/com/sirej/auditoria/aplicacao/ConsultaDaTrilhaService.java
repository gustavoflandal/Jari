package br.com.sirej.auditoria.aplicacao;

import java.util.List;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirej.auditoria.AlvoAuditoria;
import br.com.sirej.auditoria.Ator;
import br.com.sirej.auditoria.NovoRegistroAuditoria;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.auditoria.dominio.RegistroAuditoria;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;

/**
 * Consulta da trilha por alvo (doc 10: {@code GET /backoffice/auditoria?alvo=}, papel {@code AUDITOR}).
 *
 * <p>Toda consulta, permitida ou recusada, fica registrada na trilha (D-44). A recusa grava o registro e
 * só então lança a exceção, sem desfazer o registro.
 */
@Service
public class ConsultaDaTrilhaService {

    /** Papel autorizado a ler a trilha (doc 08). */
    public static final String PAPEL_AUDITOR = "AUDITOR";
    public static final String ACAO_CONSULTA = "TRILHA_AUDITORIA_CONSULTADA";
    public static final String ACAO_RECUSA = "TRILHA_AUDITORIA_CONSULTA_RECUSADA";

    private final RegistroAuditoriaRepository registros;
    private final TrilhaAuditoria trilha;

    ConsultaDaTrilhaService(RegistroAuditoriaRepository registros, TrilhaAuditoria trilha) {
        this.registros = registros;
        this.trilha = trilha;
    }

    @Transactional(noRollbackFor = ConsultaDaTrilhaRecusadaException.class)
    public List<RegistroAuditoria> porAlvo(Ator consultante, String ip, AlvoAuditoria alvo) {
        TreeMap<String, String> detalhe = new TreeMap<>();
        detalhe.put("alvo_tipo", alvo.tipo());
        detalhe.put("alvo_id", alvo.id());
        if (!PAPEL_AUDITOR.equals(consultante.papel())) {
            trilha.registrar(new NovoRegistroAuditoria(consultante, ip, ACAO_RECUSA,
                    new AlvoAuditoria("registro_auditoria", "trilha"), detalhe));
            throw new ConsultaDaTrilhaRecusadaException();
        }
        List<RegistroAuditoria> encontrados = registros.porAlvo(alvo.tipo(), alvo.id());
        trilha.registrar(new NovoRegistroAuditoria(consultante, ip, ACAO_CONSULTA,
                new AlvoAuditoria("registro_auditoria", "trilha"), detalhe));
        return encontrados;
    }
}
