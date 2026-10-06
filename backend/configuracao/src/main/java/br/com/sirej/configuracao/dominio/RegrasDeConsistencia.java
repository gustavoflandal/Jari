package br.com.sirej.configuracao.dominio;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import br.com.sirej.configuracao.ModoDesignacao;
import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.Regimento.ClasseTemporalidade;
import br.com.sirej.configuracao.Regimento.PosicaoDaJunta;
import br.com.sirej.configuracao.Regimento.Prazo;
import br.com.sirej.configuracao.Regimento.Resultado;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;

/**
 * As regras de consistência do doc 06 ("Regras de consistência (validação obrigatória)"), numeradas como lá.
 * Toda violação cita o número da regra. A regra 8 olha as chaves cruas do YAML resolvido (antes do esquema, que
 * recusaria a chave só como desconhecida); as demais olham o regimento tipado.
 *
 * <p>Nenhum valor de regimento está aqui: os únicos nomes fixos são os das invariantes (papel {@code ADMIN} na
 * regra 9; termos de voto de qualidade na regra 8; termo de provimento parcial na regra 11).
 */
public final class RegrasDeConsistencia {

    /** Termos que denunciam voto de qualidade, peso de voto ou desempate (regra 8; invariante 3). */
    private static final List<String> TERMOS_VOTO_DE_QUALIDADE = List.of("qualidade", "peso", "desempate", "minerva");

    /** Termo que denuncia provimento parcial num resultado (regra 11; invariante 4). */
    private static final String TERMO_PROVIMENTO_PARCIAL = "parcial";

    /** Papel que nunca aprova mudança de regimento (regra 9; RN38). */
    private static final String PAPEL_ADMIN = "ADMIN";

    /** Tipos de proposta que mudam o regimento: REGIMENTO (doc 06) e IMPORTACAO de YAML (D-50). */
    private static final List<String> TIPOS_QUE_MUDAM_REGIMENTO = List.of("REGIMENTO", "IMPORTACAO");

    /** Prefixo das chaves de prazo que são metas internas, únicas que aceitam {@code dias: null} (regra 6). */
    private static final String PREFIXO_META = "meta";

    private RegrasDeConsistencia() {
    }

    /** Regra 8 sobre a árvore crua resolvida: nenhuma chave de voto de qualidade, peso de voto ou desempate. */
    public static List<Violacao> verificarChaves(Map<String, Object> arvore) {
        List<Violacao> violacoes = new ArrayList<>();
        procurarChavesProibidas(arvore, "", violacoes);
        return violacoes;
    }

    /**
     * Regras 1 a 7, 9, 10 e 11 sobre o regimento tipado.
     *
     * @param cofreDeChavesDisponivel se a instalação tem cofre de chaves configurado e alcançável (regra 7)
     */
    public static List<Violacao> verificar(Regimento r, boolean cofreDeChavesDisponivel) {
        List<Violacao> v = new ArrayList<>();
        regra1FalhaFechada(r, v);
        regra2Resultados(r, v);
        regra3Turmas(r, v);
        regra4Votacao(r, v);
        regra5Posicoes(r, v);
        regra6Prazos(r, v);
        regra7CofreDeChaves(r, cofreDeChavesDisponivel, v);
        regra9Aprovadores(r, v);
        regra10Temporalidade(r, v);
        regra11SemProvimentoParcial(r, v);
        return v;
    }

    private static void regra1FalhaFechada(Regimento r, List<Violacao> v) {
        if (!r.distribuicao().falhaFechada()) {
            v.add(Violacao.daRegra(1, "distribuicao.falhaFechada",
                    "a distribuição semanal é tudo-ou-nada; só aceita true (invariante 2)"));
        }
    }

    private static void regra2Resultados(Regimento r, List<Violacao> v) {
        List<Resultado> resultados = r.resultados();
        if (resultados.isEmpty()) {
            v.add(Violacao.daRegra(2, "resultados", "o rol de resultados não pode ser vazio"));
            return;
        }
        Set<String> vistos = new HashSet<>();
        for (Resultado resultado : resultados) {
            if (!vistos.add(resultado.codigo())) {
                v.add(Violacao.daRegra(2, "resultados", "código de resultado repetido: " + resultado.codigo()));
            }
        }
        if (resultados.stream().noneMatch(Resultado::alteraPenalidade)) {
            v.add(Violacao.daRegra(2, "resultados", "ao menos um resultado precisa ter alteraPenalidade: true"));
        }
    }

    private static void regra3Turmas(Regimento r, List<Violacao> v) {
        int membros = r.turmas().membrosPorTurma();
        if (membros % 2 == 0) {
            v.add(Violacao.daRegra(3, "turmas.membrosPorTurma",
                    "a turma precisa ter número ímpar de membros (tem " + membros + ")"));
        }
        int segmentos = r.composicao().segmentos().size();
        if (r.turmas().umPorSegmento() && membros != segmentos) {
            v.add(Violacao.daRegra(3, "turmas.membrosPorTurma", "com umPorSegmento: true, a turma precisa ter um membro"
                    + " por segmento (" + segmentos + " segmentos, " + membros + " membros)"));
        }
    }

    private static void regra4Votacao(Regimento r, List<Violacao> v) {
        int membros = r.turmas().membrosPorTurma();
        if (r.votacao().votosMinimos() > membros) {
            v.add(Violacao.daRegra(4, "votacao.votosMinimos", "votos mínimos (" + r.votacao().votosMinimos()
                    + ") não podem passar de membros por turma (" + membros + ")"));
        }
        if (r.votacao().excecaoMaioriaSimples().minimo() < 2) {
            v.add(Violacao.daRegra(4, "votacao.excecaoMaioriaSimples.minimo",
                    "a exceção de maioria simples exige ao menos 2 votos"));
        }
    }

    private static void regra5Posicoes(Regimento r, List<Violacao> v) {
        List<PosicaoDaJunta> posicoes = r.composicao().posicoesPorJunta();
        int membros = r.turmas().membrosPorTurma();
        if (posicoes.size() < membros) {
            v.add(Violacao.daRegra(5, "composicao.posicoesPorJunta", "a junta precisa de ao menos " + membros
                    + " posições, uma por membro da turma (tem " + posicoes.size() + ")"));
        }
        Set<String> letras = new HashSet<>();
        for (PosicaoDaJunta posicao : posicoes) {
            if (!r.composicao().segmentos().contains(posicao.segmento())) {
                v.add(Violacao.daRegra(5, "composicao.posicoesPorJunta", "a posição " + posicao.letra()
                        + " usa o segmento " + posicao.segmento() + ", que não está em composicao.segmentos"));
            }
            if (!letras.add(posicao.letra())) {
                v.add(Violacao.daRegra(5, "composicao.posicoesPorJunta", "letra de posição repetida: " + posicao.letra()));
            }
        }
    }

    private static void regra6Prazos(Regimento r, List<Violacao> v) {
        r.prazos().porNome().forEach((nome, prazo) -> verificarPrazo(nome, prazo, v));
        for (Integer dias : r.prazos().alertas().diasAntes()) {
            if (dias == null || dias <= 0) {
                v.add(Violacao.daRegra(6, "prazos.alertas.diasAntes", "antecedência de alerta precisa ser > 0"));
            }
        }
    }

    private static void verificarPrazo(String nome, Prazo prazo, List<Violacao> v) {
        String caminho = "prazos." + nome;
        if (prazo.dias() != null && prazo.dias() <= 0) {
            v.add(Violacao.daRegra(6, caminho + ".dias", "prazo precisa ter dias > 0"));
        }
        if (prazo.sessoes() != null && prazo.sessoes() <= 0) {
            v.add(Violacao.daRegra(6, caminho + ".sessoes", "prazo em sessões precisa ser > 0"));
        }
        boolean temQuantidade = (prazo.dias() != null && prazo.dias() > 0)
                || (prazo.sessoes() != null && prazo.sessoes() > 0);
        boolean meta = nome.startsWith(PREFIXO_META);
        if (!temQuantidade && prazo.origem() == null && !meta) {
            v.add(Violacao.daRegra(6, caminho, "prazo precisa ter dias > 0 ou origem explícita (IMPRESSO_NA);"
                    + " dias: null só para metas sem prazo legal"));
        }
    }

    private static void regra7CofreDeChaves(Regimento r, boolean cofreDeChavesDisponivel, List<Violacao> v) {
        if (r.designacao().modo() == ModoDesignacao.SIGILOSO && !cofreDeChavesDisponivel) {
            v.add(Violacao.daRegra(7, "designacao.modo", "o modo SIGILOSO exige cofre de chaves (HSM/KMS) configurado"
                    + " e alcançável na instalação"));
        }
    }

    private static void regra9Aprovadores(Regimento r, List<Violacao> v) {
        r.administracao().aprovadores().forEach((tipo, papeis) -> {
            if (papeis.isEmpty()) {
                v.add(Violacao.daRegra(9, "administracao.aprovadores." + tipo,
                        "cada tipo de proposta precisa de ao menos um papel aprovador"));
            }
            if (TIPOS_QUE_MUDAM_REGIMENTO.contains(tipo) && papeis.contains(PAPEL_ADMIN)) {
                v.add(Violacao.daRegra(9, "administracao.aprovadores." + tipo,
                        "ADMIN não aprova mudança de regimento: quem parametriza não aprova a própria área (RN38)"));
            }
        });
    }

    private static void regra10Temporalidade(Regimento r, List<Violacao> v) {
        int retencao = r.retencao().anos();
        for (ClasseTemporalidade classe : r.temporalidade().classes()) {
            if (classe.guardaTotalAnos() < retencao) {
                v.add(Violacao.daRegra(10, "temporalidade.classes." + classe.codigo(), "guarda total ("
                        + classe.guardaTotalAnos() + " anos) menor que retencao.anos (" + retencao + ")"));
            }
        }
        if (r.temporalidade().eliminacaoFisica()) {
            v.add(Violacao.daRegra(10, "temporalidade.eliminacaoFisica",
                    "eliminação física só aceita false enquanto a D-19 estiver aberta"));
        }
    }

    /**
     * Regra 11: nenhum resultado representa provimento parcial (invariante 4). O resultado só tem {@code codigo} (o
     * esquema recusa outra chave, inclusive uma descrição); o código é comparado sem acento e em qualquer caixa.
     */
    private static void regra11SemProvimentoParcial(Regimento r, List<Violacao> v) {
        List<Resultado> resultados = r.resultados();
        for (int i = 0; i < resultados.size(); i++) {
            String codigo = resultados.get(i).codigo();
            if (codigo != null && normalizar(codigo).contains(TERMO_PROVIMENTO_PARCIAL)) {
                v.add(Violacao.daRegra(11, "resultados[" + i + "].codigo", "o resultado " + codigo
                        + " representa provimento parcial, que não existe (invariante 4)"));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void procurarChavesProibidas(Object no, String caminho, List<Violacao> v) {
        if (no instanceof Map<?, ?> mapa) {
            ((Map<String, Object>) mapa).forEach((chave, valor) -> {
                String aqui = caminho.isEmpty() ? chave : caminho + "." + chave;
                String normalizada = normalizar(chave);
                if (TERMOS_VOTO_DE_QUALIDADE.stream().anyMatch(normalizada::contains)) {
                    v.add(Violacao.daRegra(8, aqui, "não existe voto de qualidade, peso de voto nem desempate"
                            + " (invariante 3)"));
                }
                procurarChavesProibidas(valor, aqui, v);
            });
        } else if (no instanceof List<?> lista) {
            for (int i = 0; i < lista.size(); i++) {
                procurarChavesProibidas(lista.get(i), caminho + "[" + i + "]", v);
            }
        }
    }

    private static String normalizar(String chave) {
        return Normalizer.normalize(chave, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }
}
