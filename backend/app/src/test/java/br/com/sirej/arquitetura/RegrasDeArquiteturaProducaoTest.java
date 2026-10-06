package br.com.sirej.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import br.com.sirej.SirejApplication;

/**
 * PT-02: as regras do doc 12 passam sobre o código de produção (todos os módulos, sem classes de teste).
 * As provas de que cada regra reprova uma violação estão em {@link RegrasDeArquiteturaPegamViolacoesTest}.
 */
class RegrasDeArquiteturaProducaoTest {

    private static final JavaClasses PRODUCAO = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(RegrasDeArquitetura.RAIZ_DE_PRODUCAO);

    @Test
    @DisplayName("PT-02: ApplicationModules.verify() passa sobre a produção")
    void PT02_application_modules_verify() {
        ApplicationModules.of(SirejApplication.class).verify();
    }

    @Test
    @DisplayName("PT-02: a importação de produção alcança todos os módulos e exclui classes de teste")
    void PT02_importacao_de_producao_alcanca_todos_os_modulos_e_exclui_testes() {
        assertThat(PRODUCAO.stream().map(JavaClass::getPackageName)
                .flatMap(pacote -> RegrasDeArquitetura.modulo(RegrasDeArquitetura.RAIZ_DE_PRODUCAO, pacote).stream())
                .distinct())
                .hasSize(22)
                .contains("compartilhado", "distribuicao", "prazos", "secretaria");
        assertThat(PRODUCAO.contain("br.com.sirej.compartilhado.Cpf")).isTrue();
        assertThat(PRODUCAO.contain(RegrasDeArquiteturaProducaoTest.class)).isFalse();
        assertThat(PRODUCAO.stream().map(JavaClass::getPackageName))
                .noneMatch(pacote -> pacote.startsWith("fixturearquitetura"));
    }

    @Test
    @DisplayName("RN20: regra 1 passa na produção (designação só em distribuicao)")
    void RN20_regra1_designacao_so_em_distribuicao_passa_na_producao() {
        RegrasDeArquitetura.regra1DesignacaoSoEmDistribuicao(RegrasDeArquitetura.RAIZ_DE_PRODUCAO).check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: regra 2 passa na produção (sem acesso a pacote interno de outro módulo)")
    void PT02_regra2_sem_acesso_a_pacote_interno_passa_na_producao() {
        RegrasDeArquitetura.regra2SemAcessoAPacoteInternoDeOutroModulo(RegrasDeArquitetura.RAIZ_DE_PRODUCAO)
                .check(PRODUCAO);
    }

    @Test
    @DisplayName("RN24: regra 3 passa na produção (nenhuma rota de distribuição manual)")
    void RN24_regra3_nenhuma_rota_de_distribuicao_manual_passa_na_producao() {
        RegrasDeArquitetura.regra3SemRotaQueAltereDesignacaoOuDispareLote(RegrasDeArquitetura.RAIZ_DE_PRODUCAO)
                .check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: regra 4 passa na produção (endpoint com autorização)")
    void PT02_regra4_endpoint_com_autorizacao_passa_na_producao() {
        RegrasDeArquitetura.regra4EndpointComAutorizacao(RegrasDeArquitetura.RAIZ_DE_PRODUCAO).check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: regra 5 passa na produção (repositório de @Imutavel sem save nem delete)")
    void PT02_regra5_repositorio_de_imutavel_sem_save_nem_delete_passa_na_producao() {
        RegrasDeArquitetura.regra5RepositorioDeImutavelSemSaveNemDelete(RegrasDeArquitetura.RAIZ_DE_PRODUCAO)
                .check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: regra 6 passa na produção (tempo só pelo Relogio)")
    void PT02_regra6_tempo_so_pelo_relogio_passa_na_producao() {
        RegrasDeArquitetura.regra6TempoSoPeloRelogio(RegrasDeArquitetura.RAIZ_DE_PRODUCAO).check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: regra 7 passa na produção (sem literal de prazo em prazos e secretaria)")
    void PT02_regra7_sem_literal_de_prazo_passa_na_producao() {
        RegrasDeArquitetura.regra7SemLiteralNumericoDePrazo(RegrasDeArquitetura.RAIZ_DE_PRODUCAO).check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: compartilhado depende só do JDK na produção")
    void PT02_compartilhado_so_depende_do_jdk_na_producao() {
        RegrasDeArquitetura.compartilhadoSoDependeDoJdk(RegrasDeArquitetura.RAIZ_DE_PRODUCAO).check(PRODUCAO);
    }

    @Test
    @DisplayName("PT-02: a lista de regras aplicada no build tem as 7 do doc 12 e a do compartilhado")
    void PT02_todas_as_regras_aplicadas() {
        assertThat(RegrasDeArquitetura.todas(RegrasDeArquitetura.RAIZ_DE_PRODUCAO)).hasSize(8)
                .allSatisfy(regra -> regra.check(PRODUCAO));
    }
}
