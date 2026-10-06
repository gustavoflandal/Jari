package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento;
import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

/**
 * PT-03, invariante 8: o código não conhece o regimento de SP. Uma variação sintética, sem herança, com outro rol
 * de resultados, outros prazos e outra composição é aceita; e nenhum texto de valor do {@code sp.yaml} aparece no
 * código de produção do módulo.
 */
class NadaDeSpFixoNoCodigoTest {

    /**
     * Vocabulário do produto (não do regimento de SP) que o código precisa conhecer: valores de enum do esquema
     * tratados em Java e os nomes fixos das invariantes (regra 9). O fuso é padrão técnico da instalação.
     */
    private static final Set<String> VOCABULARIO_DO_PRODUTO = Set.of("SIGILOSO", "ABERTO", "PERMITIDO",
            "MEDIANTE_AUTORIZACAO_COORDENADOR", "PROIBIDO", "ADMIN", "REGIMENTO", "IMPORTACAO", "America/Sao_Paulo");

    @Test
    @DisplayName("PT-03: regimento sintético (outro rol, outros prazos, outra composição, modo aberto) é aceito")
    void PT03_variacao_sintetica_aceita() throws IOException {
        String yaml = recursoDeTeste("regimentos-teste/sintetico.yaml");
        LeitorDeRegimento leitor = RegimentoFixtures.leitor(Map.of("sintetico.yaml", yaml));

        Regimento r = leitor.ler("sintetico", false).regimento();

        assertThat(r.resultados()).extracting(Regimento.Resultado::codigo)
                .containsExactly("DEFERIMENTO", "INDEFERIMENTO", "NAO_CONHECIMENTO");
        assertThat(r.resultado("DEFERIMENTO")).get().extracting(Regimento.Resultado::alteraPenalidade).isEqualTo(true);
        assertThat(r.prazos().recurso1a().dias()).isEqualTo(45);
        assertThat(r.prazos().julgamento().dias()).isEqualTo(60);
        assertThat(r.prazos().defesaAutuacao().origem()).isNull();
        assertThat(r.prazos().relatoria().sessoes()).isEqualTo(2);
        assertThat(r.composicao().segmentos()).containsExactly("SERVIDORES", "USUARIOS");
        assertThat(r.composicao().posicoesPorJunta()).hasSize(5);
        assertThat(r.turmas().membrosPorTurma()).isEqualTo(5);
        assertThat(r.votacao().votosMinimos()).isEqualTo(5);
        assertThat(r.designacao().modo()).isEqualTo(ModoDesignacao.ABERTO);
        assertThat(r.orgao().municipioIbge()).isNull();
        assertThat(r.pecas()).containsOnlyKeys("RECURSO_1A_INSTANCIA", "JUNTADA_DOCUMENTOS");
        assertThat(RegimentoFixtures.violacoes(r)).isEmpty();
    }

    @Test
    @DisplayName("PT-03: nenhum texto de valor do sp.yaml aparece no código de produção de configuracao")
    void PT03_codigo_de_producao_nao_contem_valores_de_sp() throws IOException {
        Map<String, Object> sp = new YAMLMapper().readValue(
                Files.readString(RaizDoRepositorio.caminho("config/regimentos/sp.yaml")), new TypeReference<>() {
                });
        Set<String> valores = new TreeSet<>();
        coletarTextos(sp, valores);
        valores.removeAll(VOCABULARIO_DO_PRODUTO);
        assertThat(valores).contains("CANCELAMENTO_PENALIDADE", "COMUNIDADE", "CET-SP");

        Path producao = RaizDoRepositorio.caminho("backend/configuracao/src/main/java");
        List<String> achados = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(producao)) {
            for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String fonte = Files.readString(arquivo);
                for (String valor : valores) {
                    if (fonte.contains("\"" + valor + "\"")) {
                        achados.add(producao.relativize(arquivo) + ": \"" + valor + "\"");
                    }
                }
            }
        }
        assertThat(achados).as("literais do regimento de SP no código de produção").isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static void coletarTextos(Object no, Set<String> saida) {
        if (no instanceof Map<?, ?> mapa) {
            ((Map<String, Object>) mapa).forEach((chave, valor) -> {
                if (chave.matches("[A-Z][A-Z0-9_]+")) {
                    saida.add(chave);
                }
                coletarTextos(valor, saida);
            });
        } else if (no instanceof List<?> lista) {
            lista.forEach(item -> coletarTextos(item, saida));
        } else if (no instanceof String texto && texto.length() > 1) {
            saida.add(texto);
        }
    }

    private static String recursoDeTeste(String caminho) throws IOException {
        try (InputStream entrada = NadaDeSpFixoNoCodigoTest.class.getClassLoader().getResourceAsStream(caminho)) {
            assertThat(entrada).as(caminho).isNotNull();
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
