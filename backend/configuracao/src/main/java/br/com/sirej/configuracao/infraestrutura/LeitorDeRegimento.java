package br.com.sirej.configuracao.infraestrutura;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.RegimentoInvalidoException;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;
import br.com.sirej.configuracao.dominio.Heranca;
import br.com.sirej.configuracao.dominio.JsonCanonico;
import br.com.sirej.configuracao.dominio.RegrasDeConsistencia;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * Lê e valida um regimento (docs/dev/06, "Ciclo de vida", item 1), na ordem:
 * <ol>
 *   <li>lê o YAML (chave repetida reprova) e resolve {@code herda};</li>
 *   <li>regra de consistência 8 sobre as chaves cruas;</li>
 *   <li>JSON Schema ({@code esquema.json});</li>
 *   <li>converte para o {@link Regimento} tipado e aplica as demais regras de consistência.</li>
 * </ol>
 * Qualquer violação lança {@link RegimentoInvalidoException} com todas as violações da etapa. O resultado traz o
 * conteúdo canônico e o seu SHA-256.
 */
public final class LeitorDeRegimento {

    /** Extensão dos arquivos de regimento. */
    public static final String EXTENSAO = ".yaml";

    private static final TypeReference<Map<String, Object>> ARVORE = new TypeReference<>() {
    };

    private final FonteDeRegimentos fonte;
    private final YAMLMapper yaml;
    private final JsonMapper json;
    private final ValidadorDeEsquema esquema;

    public LeitorDeRegimento(FonteDeRegimentos fonte) {
        this.fonte = fonte;
        this.yaml = YAMLMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
        this.json = JsonMapper.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
        String textoDoEsquema = fonte.arquivo(ValidadorDeEsquema.ARQUIVO).orElseThrow(() ->
                new RegimentoInvalidoException("esquema do regimento não encontrado: " + ValidadorDeEsquema.ARQUIVO));
        this.esquema = new ValidadorDeEsquema(textoDoEsquema, json);
    }

    /** Lê o regimento pelo nome (ex.: {@code sp}), resolve a herança e valida tudo. */
    public RegimentoLido ler(String nome, boolean cofreDeChavesDisponivel) {
        Map<String, Object> arvore = Heranca.resolver(nome, this::arvoreCrua);
        return validar(nome, arvore, cofreDeChavesDisponivel);
    }

    /** Valida um conteúdo já resolvido (por exemplo, o JSON canônico gravado numa versão). */
    public RegimentoLido lerConteudoCanonico(String nome, String conteudoJson, boolean cofreDeChavesDisponivel) {
        return validar(nome, arvoreJson(nome, conteudoJson), cofreDeChavesDisponivel);
    }

    /** SHA-256 do conteúdo canônico de um JSON, sem validar nada (conferência de integridade). */
    public Hash hashDoConteudo(String nome, String conteudoJson) {
        return JsonCanonico.hash(arvoreJson(nome, conteudoJson));
    }

    private Map<String, Object> arvoreJson(String nome, String conteudoJson) {
        try {
            return json.readValue(conteudoJson, ARVORE);
        } catch (JacksonException e) {
            throw new RegimentoInvalidoException(nome, List.of(new Violacao("leitura", "(raiz)",
                    "conteúdo JSON ilegível: " + e.getOriginalMessage())));
        }
    }

    private RegimentoLido validar(String nome, Map<String, Object> arvore, boolean cofreDeChavesDisponivel) {
        List<Violacao> chaves = RegrasDeConsistencia.verificarChaves(arvore);
        if (!chaves.isEmpty()) {
            throw new RegimentoInvalidoException(nome, chaves);
        }
        List<Violacao> doEsquema = esquema.validar(arvore);
        if (!doEsquema.isEmpty()) {
            throw new RegimentoInvalidoException(nome, doEsquema);
        }
        Regimento regimento = json.convertValue(arvore, Regimento.class);
        List<Violacao> consistencia = new ArrayList<>(RegrasDeConsistencia.verificar(regimento, cofreDeChavesDisponivel));
        if (!consistencia.isEmpty()) {
            throw new RegimentoInvalidoException(nome, consistencia);
        }
        String canonico = JsonCanonico.de(arvore);
        return new RegimentoLido(nome, canonico, JsonCanonico.hash(arvore), regimento);
    }

    private Optional<Map<String, Object>> arvoreCrua(String nome) {
        return fonte.arquivo(nome + EXTENSAO).map(texto -> {
            try {
                Map<String, Object> arvore = yaml.readValue(texto, ARVORE);
                if (arvore == null) {
                    throw new RegimentoInvalidoException(nome, List.of(new Violacao("leitura", "(raiz)",
                            "arquivo vazio")));
                }
                return arvore;
            } catch (JacksonException e) {
                throw new RegimentoInvalidoException(nome, List.of(new Violacao("leitura", "(raiz)",
                        "YAML ilegível ou com chave repetida: " + e.getOriginalMessage())));
            }
        });
    }

    /**
     * Regimento lido e validado.
     *
     * @param nome nome do regimento (ex.: {@code sp})
     * @param conteudoCanonico JSON canônico do regimento resolvido (sem {@code herda})
     * @param hash SHA-256 do conteúdo canônico
     * @param regimento o regimento tipado
     */
    public record RegimentoLido(String nome, String conteudoCanonico, Hash hash, Regimento regimento) {
    }

    /** De onde vêm os arquivos de regimento e o esquema, pelo nome do arquivo. */
    @FunctionalInterface
    public interface FonteDeRegimentos {

        /** Conteúdo do arquivo, se existir. */
        Optional<String> arquivo(String nomeDoArquivo);
    }
}
