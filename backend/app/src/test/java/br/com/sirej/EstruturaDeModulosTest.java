package br.com.sirej;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Critérios de aceite do PT-01 (docs/dev/14-backlog-pacotes.md): um módulo vazio por contexto
 * do doc 03, com os nomes exatos de lá, e fronteiras verificadas por {@code ApplicationModules.verify()}.
 *
 * <p>As regras ArchUnit do doc 12 são do PT-02 e não estão aqui.
 */
class EstruturaDeModulosTest {

    private static final Path RAIZ_DO_REPOSITORIO = localizarRaizDoRepositorio();

    private static final ApplicationModules MODULOS = ApplicationModules.of(SirejApplication.class);

    @Test
    @DisplayName("PT-01: ApplicationModules.verify() passa sobre o esqueleto")
    void PT01_fronteiras_dos_modulos_verificadas() {
        MODULOS.verify();
    }

    @Test
    @DisplayName("PT-01: os módulos Spring Modulith são exatamente os contextos do doc 03")
    void PT01_modulos_modulith_iguais_aos_contextos_do_doc_03() throws IOException {
        List<String> detectados = MODULOS.stream().map(ApplicationModule::getIdentifier)
                .map(Object::toString)
                .toList();

        assertThat(detectados).containsExactlyInAnyOrderElementsOf(contextosDoDoc03());
    }

    @Test
    @DisplayName("PT-01: existe um módulo Maven em backend/ para cada contexto do doc 03")
    void PT01_um_modulo_maven_por_contexto_do_doc_03() throws IOException {
        String pomDoBackend = Files.readString(RAIZ_DO_REPOSITORIO.resolve("backend/pom.xml"));

        for (String contexto : contextosDoDoc03()) {
            assertThat(RAIZ_DO_REPOSITORIO.resolve("backend").resolve(contexto).resolve("pom.xml"))
                    .as("backend/%s/pom.xml", contexto)
                    .isRegularFile();
            assertThat(pomDoBackend).as("módulo %s declarado em backend/pom.xml", contexto)
                    .contains("<module>" + contexto + "</module>");
        }
    }

    @Test
    @DisplayName("PT-01: a leitura do doc 03 encontra os 22 contextos (protege o próprio teste)")
    void PT01_leitura_do_doc_03_encontra_todos_os_contextos() throws IOException {
        assertThat(contextosDoDoc03())
                .hasSize(22)
                .contains("compartilhado", "distribuicao", "integracao")
                .doesNotHaveDuplicates();
    }

    /** Lê a tabela "Módulos de código" do doc 03, fonte de verdade dos nomes. */
    private static List<String> contextosDoDoc03() throws IOException {
        String doc = Files.readString(RAIZ_DO_REPOSITORIO.resolve("docs/dev/03-arquitetura.md"));
        int inicio = doc.indexOf("## Módulos de código");
        int fim = doc.indexOf("## Regras de dependência");
        assertThat(inicio).as("seção 'Módulos de código' no doc 03").isNotNegative();
        assertThat(fim).as("seção 'Regras de dependência' no doc 03").isGreaterThan(inicio);

        Matcher linha = Pattern.compile("(?m)^\\| `([a-z]+)`").matcher(doc.substring(inicio, fim));
        List<String> contextos = new ArrayList<>();
        while (linha.find()) {
            contextos.add(linha.group(1));
        }
        return contextos;
    }

    private static Path localizarRaizDoRepositorio() {
        Path atual = Path.of("").toAbsolutePath();
        while (atual != null) {
            if (Files.isRegularFile(atual.resolve("docs/dev/03-arquitetura.md"))) {
                return atual;
            }
            atual = atual.getParent();
        }
        throw new IllegalStateException("Raiz do repositório não encontrada a partir de " + Path.of("").toAbsolutePath());
    }
}
