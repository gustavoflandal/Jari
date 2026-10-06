package br.com.sirej.auditoria.aplicacao;

import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sirej.auditoria.AlvoAuditoria;
import br.com.sirej.auditoria.Ator;
import br.com.sirej.auditoria.NovoRegistroAuditoria;
import br.com.sirej.auditoria.TrilhaAuditoria;
import br.com.sirej.auditoria.dominio.ResultadoVerificacao;
import br.com.sirej.auditoria.dominio.VerificadorDeCadeia;
import br.com.sirej.auditoria.infraestrutura.AncoraDiariaRepository;
import br.com.sirej.auditoria.infraestrutura.RegistroAuditoriaRepository;

/**
 * Verificação da cadeia (doc 08: roda diariamente e alerta em divergência; doc 18, F7: também sob
 * demanda). Recalcula cada hash a partir do conteúdo gravado e confere o encadeamento e as âncoras.
 */
@Service
public class VerificacaoDaCadeiaService {

    /** Código da ação registrada na trilha a cada verificação. */
    public static final String ACAO = "CADEIA_AUDITORIA_VERIFICADA";

    private static final Logger LOG = LoggerFactory.getLogger(VerificacaoDaCadeiaService.class);

    private final RegistroAuditoriaRepository registros;
    private final AncoraDiariaRepository ancoras;
    private final TrilhaAuditoria trilha;

    VerificacaoDaCadeiaService(RegistroAuditoriaRepository registros, AncoraDiariaRepository ancoras,
            TrilhaAuditoria trilha) {
        this.registros = registros;
        this.ancoras = ancoras;
        this.trilha = trilha;
    }

    /** Só verifica, sem registrar. */
    @Transactional(readOnly = true)
    public ResultadoVerificacao verificar() {
        VerificadorDeCadeia verificador = new VerificadorDeCadeia(ancoras.todas());
        registros.percorrerEmOrdem(verificador::examinar);
        return verificador.concluir();
    }

    /** Verifica, registra o resultado na própria trilha e alerta no log se houver quebra. */
    @Transactional
    public ResultadoVerificacao verificarERegistrar(Ator ator) {
        ResultadoVerificacao resultado = verificar();
        TreeMap<String, String> detalhe = new TreeMap<>();
        detalhe.put("integra", Boolean.toString(resultado.integra()));
        detalhe.put("registros_verificados", Long.toString(resultado.registrosVerificados()));
        resultado.pontoDeQuebra().ifPresent(quebra -> {
            detalhe.put("seq_quebra", Long.toString(quebra.seq()));
            detalhe.put("motivo", quebra.motivo().name());
            LOG.error("ALERTA: cadeia de auditoria rompida em seq={} (último íntegro: {}), motivo={}",
                    quebra.seq(), quebra.seqAnteriorIntegro(), quebra.motivo());
        });
        trilha.registrar(new NovoRegistroAuditoria(ator, null, ACAO,
                new AlvoAuditoria("registro_auditoria", "cadeia"), detalhe));
        return resultado;
    }
}
