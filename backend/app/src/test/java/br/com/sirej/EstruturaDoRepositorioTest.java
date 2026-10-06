package br.com.sirej;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Critérios de aceite do PT-01 que não são código Java: frontend em workspaces e pipeline com
 * build, testes, SAST, SCA e SBOM. Protege o esqueleto contra remoção silenciosa dessas etapas.
 */
class EstruturaDoRepositorioTest {

    private static final Path RAIZ = localizarRaizDoRepositorio();

    @Test
    @DisplayName("PT-01: frontend com apps/portal, apps/backoffice e packages/tipos em npm workspaces")
    void PT01_frontend_com_os_tres_workspaces() throws IOException {
        String pacoteRaiz = Files.readString(RAIZ.resolve("frontend/package.json"));

        for (String workspace : new String[] {"apps/portal", "apps/backoffice", "packages/tipos"}) {
            assertThat(pacoteRaiz).as("workspace %s declarado", workspace).contains("\"" + workspace + "\"");
            assertThat(RAIZ.resolve("frontend").resolve(workspace).resolve("package.json")).isRegularFile();
        }
        assertThat(pacoteRaiz).contains("\"test\": \"npm run test --workspaces");
        assertThat(Files.readString(RAIZ.resolve("frontend/tsconfig.base.json"))).contains("\"strict\": true");
    }

    @Test
    @DisplayName("PT-01: pipeline com build e testes do backend e do frontend")
    void PT01_pipeline_com_build_e_testes() throws IOException {
        String ci = Files.readString(RAIZ.resolve(".github/workflows/ci.yml"));

        assertThat(ci).contains("./mvnw -B -ntp verify", "npm ci", "npm test", "npm run typecheck", "npm run build");
    }

    @Test
    @DisplayName("PT-01: pipeline com SAST (CodeQL) em Java e TypeScript")
    void PT01_pipeline_com_sast() throws IOException {
        String codeql = Files.readString(RAIZ.resolve(".github/workflows/codeql.yml"));

        assertThat(codeql).contains("github/codeql-action/init@", "github/codeql-action/analyze@",
                "language: java-kotlin", "language: javascript-typescript");
    }

    @Test
    @DisplayName("PT-01: pipeline com SCA que bloqueia severidade alta")
    void PT01_pipeline_com_sca() throws IOException {
        String ci = Files.readString(RAIZ.resolve(".github/workflows/ci.yml"));

        assertThat(ci).contains("actions/dependency-review-action@", "fail-on-severity: high",
                "npm audit --audit-level=high");
    }

    @Test
    @DisplayName("PT-01: pipeline publica SBOM CycloneDX do backend e do frontend")
    void PT01_pipeline_publica_sbom() throws IOException {
        String ci = Files.readString(RAIZ.resolve(".github/workflows/ci.yml"));

        assertThat(ci).contains("name: sbom-backend", "META-INF/sbom/application.cdx.json",
                "name: sbom-frontend", "@cyclonedx/cyclonedx-npm");
    }

    @Test
    @DisplayName("PT-01: nenhum workflow depende de segredo")
    void PT01_pipeline_sem_segredos() throws IOException {
        try (var workflows = Files.list(RAIZ.resolve(".github/workflows"))) {
            for (Path workflow : workflows.toList()) {
                assertThat(Files.readString(workflow)).as(workflow.getFileName().toString())
                        .doesNotContain("secrets.");
            }
        }
    }

    @Test
    @DisplayName("PT-01: Maven Wrapper presente e com checksum da distribuição")
    void PT01_maven_wrapper_presente() throws IOException {
        assertThat(RAIZ.resolve("mvnw")).isRegularFile().isExecutable();
        assertThat(Files.readString(RAIZ.resolve(".mvn/wrapper/maven-wrapper.properties")))
                .contains("distributionSha256Sum=");
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
