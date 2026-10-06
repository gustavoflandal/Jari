package br.com.sirej.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;

/**
 * PT-02: prova que cada regra do doc 12 reprova uma violação plantada e aprova o código conforme.
 *
 * <p>As violações ficam em {@code src/test/java/fixturearquitetura/<regra>/violacao}, os exemplos conformes
 * em {@code .../<regra>/conforme}. Cada pacote imita a estrutura de módulos de {@code br.com.sirej}
 * ({@code <raiz>.distribuicao}, {@code <raiz>.secretaria} etc.); a regra recebe esse pacote como raiz.
 * Nada aqui fica em {@code src/main}.
 */
class RegrasDeArquiteturaPegamViolacoesTest {

    private static final String FIXTURES = "fixturearquitetura";

    // Regra 1 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("RN20: regra 1 reprova módulo que acessa Designacao*/Selo*/Semente* fora de distribuicao")
    void RN20_regra1_reprova_acesso_a_designacao_fora_de_distribuicao() {
        assertReprova(RegrasDeArquitetura::regra1DesignacaoSoEmDistribuicao, "regra1.violacao",
                "secretaria.PainelDeBacklog",
                "distribuicao.dominio.DesignacaoSemanal",
                "sessao.SorteioDaSessao",
                "distribuicao.dominio.SeloDistribuicao",
                "SementeDaSessao> does not reside in a package");
    }

    @Test
    @DisplayName("RN20: regra 1 aprova distribuicao interna e a API pública (DesignacaoConsulta, DesignacaoRevelada)")
    void RN20_regra1_aprova_designacao_dentro_de_distribuicao_e_api_publica() {
        assertAprova(RegrasDeArquitetura::regra1DesignacaoSoEmDistribuicao, "regra1.conforme");
    }

    // Regra 2 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: regra 2 reprova acesso a pacote interno (internal, dominio) de outro módulo")
    void PT02_regra2_reprova_acesso_a_pacote_interno_de_outro_modulo() {
        assertReprova(RegrasDeArquitetura::regra2SemAcessoAPacoteInternoDeOutroModulo, "regra2.violacao",
                "secretaria.Triagem",
                "processo.internal.ProjecaoDeSituacao",
                "processo.dominio.Movimentacao",
                "(pacote interno do módulo processo)");
    }

    @Test
    @DisplayName("PT-02: regra 2 aprova uso da API pública (raiz e api) e dos próprios pacotes internos")
    void PT02_regra2_aprova_api_publica_e_pacote_interno_do_proprio_modulo() {
        assertAprova(RegrasDeArquitetura::regra2SemAcessoAPacoteInternoDeOutroModulo, "regra2.conforme");
    }

    // Regra 3 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("RN24: regra 3 reprova rota de distribuição manual (POST em distribuicao, controller que dispara lote)")
    void RN24_regra3_reprova_rota_de_distribuicao_manual() {
        assertReprova(RegrasDeArquitetura::regra3SemRotaQueAltereDesignacaoOuDispareLote, "regra3.violacao",
                "DistribuicaoController.executarLote()",
                "DistribuicaoController.escolherPosicao(java.lang.String, java.lang.String)",
                "DistribuicaoController.reprocessar()",
                "atende qualquer método HTTP",
                "secretaria.web.SuporteController",
                "distribuicao.DistribuicaoSemanal");
    }

    @Test
    @DisplayName("RN24: regra 3 aprova consulta GET em distribuicao e POST em módulo que não toca distribuicao")
    void RN24_regra3_aprova_consulta_de_leitura_e_post_fora_de_distribuicao() {
        assertAprova(RegrasDeArquitetura::regra3SemRotaQueAltereDesignacaoOuDispareLote, "regra3.conforme");
    }

    // Regra 4 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: regra 4 reprova método público de controller sem @PreAuthorize no método")
    void PT02_regra4_reprova_endpoint_sem_autorizacao() {
        assertReprova(RegrasDeArquitetura::regra4EndpointComAutorizacao, "regra4.violacao",
                "ProcessoController.consultar(java.lang.String)",
                "SessaoController.abrir(java.lang.String)",
                "não declara @PreAuthorize");
        assertThatThrownBy(() -> RegrasDeArquitetura.regra4EndpointComAutorizacao(raiz("regra4.violacao"))
                .check(importar("regra4.violacao")))
                .message().doesNotContain("ProcessoController.listar()");
    }

    @Test
    @DisplayName("PT-02: regra 4 aprova @PreAuthorize direta, composta e permitAll() explícito")
    void PT02_regra4_aprova_endpoints_com_autorizacao() {
        assertAprova(RegrasDeArquitetura::regra4EndpointComAutorizacao, "regra4.conforme");
    }

    // Regra 5 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: regra 5 reprova repositório de @Imutavel com save/delete herdado ou declarado")
    void PT02_regra5_reprova_repositorio_de_imutavel_com_save_ou_delete() {
        assertReprova(RegrasDeArquitetura::regra5RepositorioDeImutavelSemSaveNemDelete, "regra5.violacao",
                "MovimentacaoRepository gerencia [Movimentacao] (@Imutavel) e expõe",
                "CrudRepository.save(java.lang.Object)",
                "CrudRepository.deleteById(java.lang.Object)",
                "RegistroAuditoriaRepository.deleteById(java.lang.Long)",
                "VotoRepository.salvar(",
                "VotoRepository.atualizar(");
    }

    @Test
    @DisplayName("PT-02: regra 5 aprova repositório de @Imutavel só com consulta e repositório comum com save")
    void PT02_regra5_aprova_repositorio_de_imutavel_so_com_consulta() {
        assertAprova(RegrasDeArquitetura::regra5RepositorioDeImutavelSemSaveNemDelete, "regra5.conforme");
    }

    // Regra 6 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: regra 6 reprova leitura direta do relógio do sistema (now, Clock.system*, Date)")
    void PT02_regra6_reprova_leitura_direta_do_relogio() {
        assertReprova(RegrasDeArquitetura::regra6TempoSoPeloRelogio, "regra6.violacao",
                "CalculoDeVencimento.vencimento()> calls method <java.time.LocalDate.now()>",
                "java.time.Instant.now()",
                "AberturaDeSessao",
                "java.lang.System.currentTimeMillis()",
                "java.util.Date.<init>()",
                "java.time.Clock.systemUTC()");
    }

    @Test
    @DisplayName("PT-02: regra 6 aprova uso do Relogio e a leitura do sistema dentro do próprio Relogio")
    void PT02_regra6_aprova_uso_do_relogio() {
        assertAprova(RegrasDeArquitetura::regra6TempoSoPeloRelogio, "regra6.conforme");
    }

    // Regra 7 ------------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: regra 7 reprova literal de prazo em prazos e secretaria (código, constante, enum, lambda, anotação)")
    void PT02_regra7_reprova_literal_de_prazo() {
        assertReprova(RegrasDeArquitetura::regra7SemLiteralNumericoDePrazo, "regra7.violacao",
                "PrazoDeRecurso: literal 30 em método vencimento",
                "Exigencia: literal 15 em campo PRAZO_EXIGENCIA",
                "Exigencia: literal 15 em método vencimento",
                "Exigencia: literal 10 em método lambda$",
                "Exigencia: literal 60 em método vencimentoLongo",
                "TipoDePrazo: literal 30 em método <clinit>",
                "TipoDePrazo: literal 15 em método <clinit>",
                "PrazoAnotado: literal 5 em anotação do método");
    }

    @Test
    @DisplayName("PT-02: regra 7 aprova prazo vindo de parâmetro, -1/0/1, ordinais de enum e literais fora de prazos/secretaria")
    void PT02_regra7_aprova_prazo_parametrizado() {
        assertAprova(RegrasDeArquitetura::regra7SemLiteralNumericoDePrazo, "regra7.conforme");
    }

    // Compartilhado ------------------------------------------------------------------------------------

    @Test
    @DisplayName("PT-02: compartilhado reprova dependência de biblioteca fora do JDK")
    void PT02_compartilhado_reprova_dependencia_fora_do_jdk() {
        assertReprova(RegrasDeArquitetura::compartilhadoSoDependeDoJdk, "compartilhado.violacao",
                "compartilhado.Formatador", "org.springframework.util.StringUtils");
    }

    @Test
    @DisplayName("PT-02: compartilhado aprova tipo que depende só do JDK")
    void PT02_compartilhado_aprova_tipo_so_com_jdk() {
        assertAprova(RegrasDeArquitetura::compartilhadoSoDependeDoJdk, "compartilhado.conforme");
    }

    // ---------------------------------------------------------------------------------------------------

    private static String raiz(String caso) {
        return FIXTURES + "." + caso;
    }

    private static JavaClasses importar(String caso) {
        JavaClasses classes = new ClassFileImporter().importPackages(raiz(caso));
        assertThat(classes).as("fixtures de %s", caso).isNotEmpty();
        return classes;
    }

    private static void assertReprova(Function<String, ArchRule> regra, String caso, String... trechos) {
        JavaClasses classes = importar(caso);
        assertThatThrownBy(() -> regra.apply(raiz(caso)).check(classes))
                .as("a regra deveria reprovar %s", caso)
                .isInstanceOf(AssertionError.class)
                .message().contains(trechos);
    }

    private static void assertAprova(Function<String, ArchRule> regra, String caso) {
        regra.apply(raiz(caso)).check(importar(caso));
    }
}
