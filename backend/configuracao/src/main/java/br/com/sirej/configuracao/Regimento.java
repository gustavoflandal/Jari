package br.com.sirej.configuracao;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Regimento tipado e imutável (docs/dev/06, "Acesso no código"). Uma seção por chave de primeiro nível do YAML,
 * com os mesmos nomes: {@code regimento.prazos().recurso1a().dias()}.
 *
 * <p>Nenhum valor é fixo no código: todos vêm do YAML validado pelo esquema e pelas regras de consistência. Use
 * sempre {@link RegimentoVigente}; em testes, {@code RegimentoFixtures.sp()} e as variações {@code com*}.
 *
 * <p>Os métodos {@code com*} devolvem cópia com uma seção trocada; servem a testes de variação e não validam
 * nada. Um regimento só entra em vigor pelo módulo {@code configuracao}, depois de validado.
 */
public record Regimento(
        SecaoOrgao orgao,
        SecaoModulos modulos,
        SecaoIdentidade identidade,
        SecaoCalendario calendario,
        SecaoPrazos prazos,
        Map<String, RegraPeca> pecas,
        SecaoComposicao composicao,
        SecaoDesignacao designacao,
        SecaoDistribuicao distribuicao,
        SecaoTurmas turmas,
        SecaoSessao sessao,
        SecaoVotacao votacao,
        List<Resultado> resultados,
        SecaoRelatoria relatoria,
        SecaoImpedimento impedimento,
        SecaoDiligencia diligencia,
        SecaoAutos autos,
        SecaoPresenca presenca,
        SecaoMandato mandato,
        SecaoDocumentos documentos,
        SecaoRetencao retencao,
        SecaoAdministracao administracao,
        SecaoTemporalidade temporalidade) {

    public Regimento {
        pecas = mapa(pecas);
        resultados = lista(resultados);
    }

    /** Resultado do rol fechado com este código, se existir (RN26). */
    public Optional<Resultado> resultado(String codigo) {
        return resultados.stream().filter(r -> r.codigo().equals(codigo)).findFirst();
    }

    public Regimento comOrgao(SecaoOrgao v) { return new Regimento(v, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comModulos(SecaoModulos v) { return new Regimento(orgao, v, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comIdentidade(SecaoIdentidade v) { return new Regimento(orgao, modulos, v, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comCalendario(SecaoCalendario v) { return new Regimento(orgao, modulos, identidade, v, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comPrazos(SecaoPrazos v) { return new Regimento(orgao, modulos, identidade, calendario, v, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comPecas(Map<String, RegraPeca> v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, v, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comComposicao(SecaoComposicao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, v, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comDesignacao(SecaoDesignacao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, v, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comDistribuicao(SecaoDistribuicao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, v, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comTurmas(SecaoTurmas v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, v, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comSessao(SecaoSessao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, v, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comVotacao(SecaoVotacao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, v, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comResultados(List<Resultado> v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, v, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comRelatoria(SecaoRelatoria v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, v, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comImpedimento(SecaoImpedimento v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, v, diligencia, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comDiligencia(SecaoDiligencia v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, v, autos, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comAutos(SecaoAutos v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, v, presenca, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comPresenca(SecaoPresenca v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, v, mandato, documentos, retencao, administracao, temporalidade); }
    public Regimento comMandato(SecaoMandato v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, v, documentos, retencao, administracao, temporalidade); }
    public Regimento comDocumentos(SecaoDocumentos v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, v, retencao, administracao, temporalidade); }
    public Regimento comRetencao(SecaoRetencao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, v, administracao, temporalidade); }
    public Regimento comAdministracao(SecaoAdministracao v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, v, temporalidade); }
    public Regimento comTemporalidade(SecaoTemporalidade v) { return new Regimento(orgao, modulos, identidade, calendario, prazos, pecas, composicao, designacao, distribuicao, turmas, sessao, votacao, resultados, relatoria, impedimento, diligencia, autos, presenca, mandato, documentos, retencao, administracao, v); }

    // ---- Seções -------------------------------------------------------------------------------------------

    /** Identificação do órgão. {@code municipioIbge} é nulo para órgão estadual. */
    public record SecaoOrgao(String codigo, String nome, String esfera, String uf, Integer municipioIbge,
            String fusoHorario, String segundaInstancia) {
    }

    /** Módulos opcionais ativos; módulo inativo não expõe rotas nem telas. */
    public record SecaoModulos(boolean portalRecorrente, boolean autuacaoInstrucao, boolean segundaInstancia,
            boolean credenciamento, boolean transparencia, boolean demandasJudiciais) {
    }

    /** Provedores de login do cidadão, nível mínimo por ato (RN32) e MFA interno. */
    public record SecaoIdentidade(List<String> provedoresCidadao, Map<String, String> nivelMinimoPorAto,
            boolean mfaInternos) {
        public SecaoIdentidade {
            provedoresCidadao = lista(provedoresCidadao);
            nivelMinimoPorAto = mapa(nivelMinimoPorAto);
        }
    }

    /** Abrangência de feriados e prorrogação de vencimento (RN31). */
    public record SecaoCalendario(List<String> feriados, boolean prorrogaVencimentoEmDiaNaoUtil) {
        public SecaoCalendario {
            feriados = lista(feriados);
        }
    }

    /** Todos os prazos do rito (RN01, RN02, RN04, RN08, RN10, RN37). */
    public record SecaoPrazos(Prazo defesaAutuacao, Prazo indicacaoCondutor, Prazo recurso1a, Prazo recurso2a,
            Prazo cienciaPresumidaEletronica, Prazo julgamento, Prazo exigencia, Prazo informacaoAgente,
            Prazo relatoria, Prazo metaDefesaAutuacao, Alertas alertas) {

        /** Os prazos pelo nome da chave no YAML, na ordem do esquema (sem os alertas). */
        public Map<String, Prazo> porNome() {
            Map<String, Prazo> m = new LinkedHashMap<>();
            m.put("defesaAutuacao", defesaAutuacao);
            m.put("indicacaoCondutor", indicacaoCondutor);
            m.put("recurso1a", recurso1a);
            m.put("recurso2a", recurso2a);
            m.put("cienciaPresumidaEletronica", cienciaPresumidaEletronica);
            m.put("julgamento", julgamento);
            m.put("exigencia", exigencia);
            m.put("informacaoAgente", informacaoAgente);
            m.put("relatoria", relatoria);
            m.put("metaDefesaAutuacao", metaDefesaAutuacao);
            return Collections.unmodifiableMap(m);
        }
    }

    /**
     * Um prazo: em dias corridos, em sessões, ou com origem explícita da contagem ({@code IMPRESSO_NA}).
     * {@code dias} nulo só em metas sem prazo legal (regra de consistência 6).
     */
    public record Prazo(Integer dias, Integer sessoes, String origem, String conta, String acao) {
    }

    /** Antecedências dos alertas de prazo (RN37). */
    public record Alertas(List<Integer> diasAntes) {
        public Alertas {
            diasAntes = lista(diasAntes);
        }
    }

    /** Tipo de peça: quem decide e se vira processo de JARI (RN03, RN15, RN16). {@code decisor} pode ser nulo. */
    public record RegraPeca(String decisor, boolean viraProcessoJari) {
    }

    /** Segmentos, posições por junta, suplentes, presidência e mandato (RN21). */
    public record SecaoComposicao(List<String> segmentos, List<PosicaoDaJunta> posicoesPorJunta,
            int suplentesPorSegmento, boolean presidenciaLivre, DuracaoMandato mandato) {
        public SecaoComposicao {
            segmentos = lista(segmentos);
            posicoesPorJunta = lista(posicoesPorJunta);
        }
    }

    /** Vaga fixa de membro na junta, por letra, ligada a um segmento. */
    public record PosicaoDaJunta(String letra, String segmento) {
    }

    /** Duração do mandato e possibilidade de recondução. */
    public record DuracaoMandato(int meses, boolean reconducao) {
    }

    /** Modo de designação (RN20) e publicidade dos nomes após o julgamento. */
    public record SecaoDesignacao(ModoDesignacao modo, boolean revelarNomesAposJulgamento) {
    }

    /** Distribuição semanal (RN19, RN24, RN27). {@code falhaFechada} só aceita {@code true} (invariante 2). */
    public record SecaoDistribuicao(String periodicidade, String equidade, List<String> conexao,
            String conexaoComPendentes, String ordemNaPosicao, boolean falhaFechada,
            Redistribuicao redistribuicao) {
        public SecaoDistribuicao {
            conexao = lista(conexao);
        }
    }

    /** Motivos de redistribuição entre juntas (RN27). */
    public record Redistribuicao(List<String> motivosPermitidos, boolean exigeCriterioDoCoordenador) {
        public Redistribuicao {
            motivosPermitidos = lista(motivosPermitidos);
        }
    }

    /** Turmas de julgamento (RN21, RN25). */
    public record SecaoTurmas(int membrosPorTurma, boolean umPorSegmento, int maximoSimultaneas, String rodizio,
            boolean presidenteEViceEmTurmasDiferentes) {
    }

    /** Quórum, distribuição interna, substituição, roteiro e modalidades (RN22, RN35). */
    public record SecaoSessao(Quorum quorumAbertura, String executorDistribuicaoInterna, String substituicaoAusente,
            String relatorAusente, List<PassoRoteiro> roteiro, List<String> modalidades, boolean sustentacaoOral) {
        public SecaoSessao {
            roteiro = lista(roteiro);
            modalidades = lista(modalidades);
        }
    }

    /** Mínimo de membros e de segmentos distintos. */
    public record Quorum(int minimoMembros, int segmentosDistintos) {
    }

    /** Passo do roteiro da sessão; obrigatório bloqueia o avanço. */
    public record PassoRoteiro(String passo, boolean obrigatorio) {
    }

    /** Votação (RN07, RN14). Não há voto de qualidade, peso nem desempate (invariante 3). */
    public record SecaoVotacao(int votosMinimos, ExcecaoMaioriaSimples excecaoMaioriaSimples, String ordem,
            boolean votoRelatorVisivelAposConclusao, boolean exigeDispositivoNormativo) {
    }

    /** Exceção de deliberação com maioria simples. */
    public record ExcecaoMaioriaSimples(boolean permitida, int minimo, boolean exigePresidenteOuVice) {
    }

    /** Resultado do rol fechado (RN26) e se altera a penalidade (RN12). */
    public record Resultado(String codigo, boolean alteraPenalidade) {
    }

    /** Itens do checklist do relator (RN13). */
    public record SecaoRelatoria(List<String> checklist) {
        public SecaoRelatoria {
            checklist = lista(checklist);
        }
    }

    /** Motivos tipificados de impedimento e suspeição (RN34). */
    public record SecaoImpedimento(List<MotivoImpedimento> motivos) {
        public SecaoImpedimento {
            motivos = lista(motivos);
        }
    }

    /** Motivo tipificado; {@code tipo} é {@code IMPEDIMENTO} ou {@code SUSPEICAO}. */
    public record MotivoImpedimento(String codigo, String tipo) {
    }

    /** Requisitos da diligência presencial (RN28). */
    public record SecaoDiligencia(Quorum presencial) {
    }

    /** Acesso dos membros aos autos (RN29). */
    public record SecaoAutos(boolean downloadMembros, boolean marcaDagua,
            AcessoAutosForaDaSessao acessoMembrosForaDaSessao) {
    }

    /** Cancelamento de presença (RN36). */
    public record SecaoPresenca(boolean cancelaPorRecusaImotivada) {
    }

    /** Métricas de perda de mandato; o sistema só evidencia (RN30). */
    public record SecaoMandato(PerdaDeMandato perda) {
    }

    /** Faltas e hipóteses de perda de mandato. */
    public record PerdaDeMandato(int faltasSeguidas, int faltasIntercaladasAno,
            List<String> hipotesesComProcedimento) {
        public PerdaDeMandato {
            hipotesesComProcedimento = lista(hipotesesComProcedimento);
        }
    }

    /** Formatos, tamanho e documentos obtidos de ofício (RN33). */
    public record SecaoDocumentos(List<String> formatosAceitos, int tamanhoMaximoMb, List<String> obtidosDeOficio) {
        public SecaoDocumentos {
            formatosAceitos = lista(formatosAceitos);
            obtidosDeOficio = lista(obtidosDeOficio);
        }
    }

    /** Anos de retenção. */
    public record SecaoRetencao(int anos) {
    }

    /** Aprovadores por tipo de proposta, antecedência da vigência e papéis sensíveis (RN38, RN39, RN42). */
    public record SecaoAdministracao(Map<String, List<String>> aprovadores, AntecedenciaMinima antecedenciaMinima,
            List<String> papeisSensiveis, VigenciaMaximaPapel vigenciaMaximaPapel) {
        public SecaoAdministracao {
            Map<String, List<String>> copia = new LinkedHashMap<>();
            Objects.requireNonNull(aprovadores, "aprovadores").forEach((tipo, papeis) -> copia.put(tipo, lista(papeis)));
            aprovadores = Collections.unmodifiableMap(copia);
            papeisSensiveis = lista(papeisSensiveis);
        }
    }

    /** Antecedência mínima da vigência, em dias úteis. */
    public record AntecedenciaMinima(int diasUteis) {
    }

    /** Vigência máxima de papel concedido, em meses. */
    public record VigenciaMaximaPapel(int meses) {
    }

    /** Classes documentais e destinação (RN44). */
    public record SecaoTemporalidade(List<ClasseTemporalidade> classes, boolean eliminacaoFisica) {
        public SecaoTemporalidade {
            classes = lista(classes);
        }
    }

    /** Classe documental: guarda corrente, intermediária e destinação. */
    public record ClasseTemporalidade(String codigo, int correnteAnos, int intermediariaAnos, String destinacao) {

        /** Guarda total (corrente + intermediária), em anos. */
        public int guardaTotalAnos() {
            return correnteAnos + intermediariaAnos;
        }
    }

    private static <T> List<T> lista(List<T> valores) {
        return valores == null ? List.of() : List.copyOf(valores);
    }

    private static <K, V> Map<K, V> mapa(Map<K, V> valores) {
        return valores == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(valores));
    }
}
