package br.com.sirej.configuracao.infraestrutura;

import java.util.List;
import java.util.Map;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;

import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Valida a árvore resolvida do regimento contra o JSON Schema ({@code config/regimentos/esquema.json}, draft
 * 2020-12). Toda violação sai com a regra "esquema" e o caminho da chave.
 */
public final class ValidadorDeEsquema {

    /** Nome do arquivo do esquema, ao lado dos regimentos. */
    public static final String ARQUIVO = "esquema.json";

    private final Schema esquema;
    private final JsonMapper json;

    public ValidadorDeEsquema(String textoDoEsquema, JsonMapper json) {
        this.json = json;
        JsonNode no = json.readTree(textoDoEsquema);
        this.esquema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(no);
    }

    public List<Violacao> validar(Map<String, Object> arvore) {
        JsonNode no = json.valueToTree(arvore);
        return esquema.validate(no).stream()
                .map(erro -> new Violacao("esquema", caminho(erro.getInstanceLocation().toString(), erro.getProperty()),
                        erro.getMessage()))
                .toList();
    }

    /** JSON Pointer ({@code /designacao/modo}) para o caminho do YAML ({@code designacao.modo}). */
    private static String caminho(String local, String propriedade) {
        String base = local.replaceFirst("^\\$", "").replaceFirst("^[/.]", "").replace('/', '.');
        if (propriedade != null && !propriedade.isBlank() && !base.endsWith(propriedade)) {
            base = base.isEmpty() ? propriedade : base + "." + propriedade;
        }
        return base.isEmpty() ? "(raiz)" : base;
    }
}
