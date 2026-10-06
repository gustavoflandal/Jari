package br.com.sirej.configuracao.infraestrutura;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.configuracao.Regimento;
import br.com.sirej.configuracao.RegimentoInvalidoException;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;
import br.com.sirej.configuracao.dominio.Heranca;
import br.com.sirej.configuracao.dominio.JsonCanonico;
import br.com.sirej.configuracao.dominio.RegrasDeConsistencia;

/**
 * Lê e valida um regimento (docs/dev/06, "Ciclo de vida", item 1), na ordem:
 * <ol>
 *   <li>lê o YAML (só tipos simples; chave repetida reprova) e resolve {@code herda};</li>
 *   <li>regra de consistência 8 sobre as chaves cruas;</li>
 *   <li>JSON Schema ({@code esquema.json});</li>
 *   <li>converte para o {@link Regimento} tipado e aplica as demais regras de consistência.</li>
 * </ol>
 * Qualquer violação lança {@link RegimentoInvalidoException} com todas as violações da etapa. O resultado traz o
 * conteúdo canônico e o seu SHA-256. O JSON gravado no banco é lido pelo mesmo leitor (JSON é YAML).
 */
public final class LeitorDeRegimento {

    /** Extensão dos arquivos de regimento. */
    public static final String EXTENSAO = ".yaml";

    private final FonteDeRegimentos fonte;
    private final ValidadorDeEsquema esquema;

    public LeitorDeRegimento(FonteDeRegimentos fonte) {
        this.fonte = fonte;
        String textoDoEsquema = fonte.arquivo(ValidadorDeEsquema.ARQUIVO).orElseThrow(() ->
                new RegimentoInvalidoException("esquema do regimento não encontrado: " + ValidadorDeEsquema.ARQUIVO));
        this.esquema = new ValidadorDeEsquema(carregar(ValidadorDeEsquema.ARQUIVO, textoDoEsquema));
    }

    /** Lê o regimento pelo nome (ex.: {@code sp}), resolve a herança e valida tudo. */
    public RegimentoLido ler(String nome, boolean cofreDeChavesDisponivel) {
        Map<String, Object> arvore = Heranca.resolver(nome, this::arvoreCrua);
        return validar(nome, arvore, cofreDeChavesDisponivel);
    }

    /** Valida um conteúdo já resolvido (por exemplo, o JSON canônico gravado numa versão). */
    public RegimentoLido lerConteudoCanonico(String nome, String conteudoJson, boolean cofreDeChavesDisponivel) {
        return validar(nome, objeto(nome, conteudoJson), cofreDeChavesDisponivel);
    }

    /** SHA-256 do conteúdo canônico de um JSON, sem validar nada (conferência de integridade). */
    public Hash hashDoConteudo(String nome, String conteudoJson) {
        return JsonCanonico.hash(objeto(nome, conteudoJson));
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
        Regimento regimento = ConversorDeRegistros.paraRegistro(arvore, Regimento.class);
        List<Violacao> consistencia = RegrasDeConsistencia.verificar(regimento, cofreDeChavesDisponivel);
        if (!consistencia.isEmpty()) {
            throw new RegimentoInvalidoException(nome, consistencia);
        }
        return new RegimentoLido(nome, JsonCanonico.de(arvore), JsonCanonico.hash(arvore), regimento);
    }

    private Optional<Map<String, Object>> arvoreCrua(String nome) {
        return fonte.arquivo(nome + EXTENSAO).map(texto -> objeto(nome, texto));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> objeto(String nome, String texto) {
        Object arvore = carregar(nome, texto);
        if (!(arvore instanceof Map<?, ?> mapa)) {
            throw new RegimentoInvalidoException(nome, List.of(new Violacao("leitura", "(raiz)",
                    "o conteúdo precisa ser um objeto com as seções do regimento")));
        }
        return (Map<String, Object>) mapa;
    }

    private static Object carregar(String nome, String texto) {
        LoaderOptions opcoes = new LoaderOptions();
        opcoes.setAllowDuplicateKeys(false);
        opcoes.setAllowRecursiveKeys(false);
        try {
            return new Yaml(new SafeConstructor(opcoes)).load(texto);
        } catch (YAMLException e) {
            throw new RegimentoInvalidoException(nome, List.of(new Violacao("leitura", "(raiz)",
                    "YAML ilegível ou com chave repetida: " + e.getMessage())));
        }
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
