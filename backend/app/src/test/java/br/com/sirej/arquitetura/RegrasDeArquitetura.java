package br.com.sirej.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * As 7 regras ArchUnit obrigatórias do doc 12 (docs/dev/12-testes-e-qualidade.md), mais a regra de
 * dependências do {@code compartilhado} (doc 03 e doc 13).
 *
 * <p>Cada regra recebe o pacote raiz ({@code br.com.sirej} em produção). Assim a mesma regra roda contra
 * o código de produção e contra as violações plantadas em {@code src/test/java/fixturearquitetura}, que
 * ficam fora de {@code br.com.sirej} para não entrarem na varredura de componentes do Spring nem na
 * detecção de módulos do Spring Modulith. O critério exato de cada regra está no doc 12.
 *
 * <p>Os nomes de anotações e tipos do Spring são comparados como texto: o código de produção não
 * precisa ter Spring Web, Spring Security ou Spring Data no classpath para a regra valer.
 */
public final class RegrasDeArquitetura {

    /** Pacote raiz do código de produção (doc 03). */
    public static final String RAIZ_DE_PRODUCAO = "br.com.sirej";

    /** Prefixos de nome dos tipos da designação sigilosa (regra 1). */
    static final List<String> PREFIXOS_SIGILOSOS = List.of("Designacao", "Selo", "Semente");

    /**
     * Únicos tipos da designação visíveis fora de {@code distribuicao} (doc 03, regra 4; doc 07, seção 9):
     * a consulta auditada e o evento de revelação, ambos no pacote raiz de {@code distribuicao}.
     * Acrescentar um nome aqui exige revisão de segurança (doc 13) e linha no doc 16 (D-39).
     */
    static final Set<String> API_PUBLICA_DA_DESIGNACAO = Set.of("DesignacaoConsulta", "DesignacaoRevelada");

    /** Único tipo de {@code distribuicao} que um controller de outro módulo pode usar (regra 3, D-39). */
    static final String CONSULTA_DA_DESIGNACAO = "DesignacaoConsulta";

    /** Prefixos de método proibidos em repositório de tipo {@code @Imutavel} (regra 5, D-41). */
    static final List<String> PREFIXOS_DE_ALTERACAO = List.of(
            "save", "delete", "remove", "update", "merge",
            "salvar", "apagar", "excluir", "remover", "atualizar", "alterar");

    /** Módulos em que vale a regra 7 (doc 12). */
    static final List<String> MODULOS_SEM_LITERAL_DE_PRAZO = List.of("prazos", "secretaria");

    static final String IMUTAVEL = "br.com.sirej.compartilhado.Imutavel";
    static final String CONTROLLER = "org.springframework.stereotype.Controller";
    static final String REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    static final String REQUEST_MAPPING = "org.springframework.web.bind.annotation.RequestMapping";
    static final String PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
    static final String REPOSITORIO_SPRING_DATA = "org.springframework.data.repository.Repository";

    /** Métodos HTTP que não alteram estado (RFC 9110, métodos seguros usados pelo produto). */
    private static final Set<String> METODOS_HTTP_DE_LEITURA = Set.of("GET", "HEAD", "OPTIONS");

    private static final Set<String> TIPOS_JAVA_TIME_COM_NOW = Set.of(
            "java.time.Instant", "java.time.LocalDate", "java.time.LocalDateTime", "java.time.LocalTime",
            "java.time.ZonedDateTime", "java.time.OffsetDateTime", "java.time.OffsetTime", "java.time.Year",
            "java.time.YearMonth", "java.time.MonthDay");

    private RegrasDeArquitetura() {
    }

    /** Todas as regras do doc 12, na ordem do doc, mais a regra do compartilhado. */
    public static List<ArchRule> todas(String raiz) {
        return List.of(
                regra1DesignacaoSoEmDistribuicao(raiz),
                regra2SemAcessoAPacoteInternoDeOutroModulo(raiz),
                regra3SemRotaQueAltereDesignacaoOuDispareLote(raiz),
                regra4EndpointComAutorizacao(raiz),
                regra5RepositorioDeImutavelSemSaveNemDelete(raiz),
                regra6TempoSoPeloRelogio(raiz),
                regra7SemLiteralNumericoDePrazo(raiz),
                compartilhadoSoDependeDoJdk(raiz));
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 1 (RN20, invariante 1): só distribuicao acessa Designacao*, Selo*, Semente*.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra1DesignacaoSoEmDistribuicao(String raiz) {
        String distribuicao = raiz + ".distribuicao";

        DescribedPredicate<JavaClass> tipoSigilosoForaDaApi = DescribedPredicate.describe(
                "tipos Designacao*, Selo* ou Semente* (exceto " + API_PUBLICA_DA_DESIGNACAO
                        + " no pacote raiz de distribuicao)",
                tipo -> temNomeSigiloso(tipo) && !ehApiPublicaDaDesignacao(tipo, distribuicao));
        DescribedPredicate<JavaClass> nomeSigiloso = DescribedPredicate.describe(
                "têm nome Designacao*, Selo* ou Semente*", RegrasDeArquitetura::temNomeSigiloso);

        ArchRule acesso = noClasses()
                .that().resideInAPackage(raiz + "..")
                .and().resideOutsideOfPackage(distribuicao + "..")
                .should().dependOnClassesThat(tipoSigilosoForaDaApi)
                .allowEmptyShould(true)
                .because("só o módulo distribuicao lê ou grava a designação (invariante 1, RN20, doc 03 regra 4)");
        ArchRule moradia = classes()
                .that(nomeSigiloso).and().resideInAPackage(raiz + "..")
                .should().resideInAPackage(distribuicao + "..")
                .allowEmptyShould(true)
                .because("um tipo de designação, selo ou semente fora de distribuicao seria uma cópia fora do controle"
                        + " do sigilo (invariante 1, RN20)");
        return CompositeArchRule.of(acesso).and(moradia)
                .as("Regra 1 (RN20): apenas classes de " + distribuicao + ".. acessam tipos Designacao*, Selo*, Semente*");
    }

    private static boolean temNomeSigiloso(JavaClass tipo) {
        String nome = tipo.getBaseComponentType().getSimpleName();
        return PREFIXOS_SIGILOSOS.stream().anyMatch(nome::startsWith);
    }

    private static boolean ehApiPublicaDaDesignacao(JavaClass tipo, String distribuicao) {
        JavaClass base = tipo.getBaseComponentType();
        return base.getPackageName().equals(distribuicao) && API_PUBLICA_DA_DESIGNACAO.contains(base.getSimpleName());
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 2: nenhuma classe acessa pacote interno de outro módulo.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra2SemAcessoAPacoteInternoDeOutroModulo(String raiz) {
        ArchCondition<JavaClass> soApiPublica = new ArchCondition<>(
                "acessar outro módulo só pelo pacote raiz dele ou pelo subpacote api") {
            @Override
            public void check(JavaClass origem, ConditionEvents eventos) {
                String moduloDeOrigem = modulo(raiz, origem.getPackageName()).orElse(null);
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    JavaClass alvo = dependencia.getTargetClass().getBaseComponentType();
                    Optional<String> moduloDoAlvo = modulo(raiz, alvo.getPackageName());
                    if (moduloDoAlvo.isEmpty() || moduloDoAlvo.get().equals(moduloDeOrigem)) {
                        continue;
                    }
                    String apiRaiz = raiz + "." + moduloDoAlvo.get();
                    String pacote = alvo.getPackageName();
                    boolean publico = pacote.equals(apiRaiz) || pacote.equals(apiRaiz + ".api")
                            || pacote.startsWith(apiRaiz + ".api.");
                    if (!publico) {
                        eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()
                                + " (pacote interno do módulo " + moduloDoAlvo.get() + ")"));
                    }
                }
            }
        };
        return classes().that().resideInAPackage(raiz + "..")
                .should(soApiPublica)
                .allowEmptyShould(true)
                .because("a API pública de um módulo é o pacote raiz ou o subpacote api; o resto é interno (doc 03, regra 1)")
                .as("Regra 2: nenhuma classe acessa pacote internal (não público) de outro módulo");
    }

    /** Módulo de um pacote: o primeiro segmento depois da raiz; vazio fora da raiz ou na própria raiz. */
    static Optional<String> modulo(String raiz, String pacote) {
        if (!pacote.startsWith(raiz + ".")) {
            return Optional.empty();
        }
        String resto = pacote.substring(raiz.length() + 1);
        int ponto = resto.indexOf('.');
        return Optional.of(ponto < 0 ? resto : resto.substring(0, ponto));
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 3 (RN24, invariante 2): nenhuma rota de distribuição manual.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra3SemRotaQueAltereDesignacaoOuDispareLote(String raiz) {
        String distribuicao = raiz + ".distribuicao";
        DescribedPredicate<JavaClass> controller = controllers();

        ArchRule somenteLeituraEmDistribuicao = classes()
                .that(controller).and().resideInAPackage(distribuicao + "..")
                .should(exporSoEndpointsDeLeitura())
                .allowEmptyShould(true)
                .because("não existe endpoint para distribuir, reprocessar, escolher posição ou alterar designação"
                        + " (invariante 2, RN24, doc 07)");

        DescribedPredicate<JavaClass> distribuicaoForaDaConsulta = DescribedPredicate.describe(
                "pertencem a " + distribuicao + ".. e não são " + CONSULTA_DA_DESIGNACAO,
                tipo -> {
                    JavaClass base = tipo.getBaseComponentType();
                    boolean emDistribuicao = base.getPackageName().equals(distribuicao)
                            || base.getPackageName().startsWith(distribuicao + ".");
                    String consulta = distribuicao + "." + CONSULTA_DA_DESIGNACAO;
                    boolean ehConsulta = base.getName().equals(consulta) || base.getName().startsWith(consulta + "$");
                    return emDistribuicao && !ehConsulta;
                });
        ArchRule controllersDeOutrosModulos = noClasses()
                .that(controller).and().resideInAPackage(raiz + "..").and().resideOutsideOfPackage(distribuicao + "..")
                .should().dependOnClassesThat(distribuicaoForaDaConsulta)
                .allowEmptyShould(true)
                .because("um endpoint de outro módulo também não pode disparar lote nem alterar designação;"
                        + " fora de distribuicao, só a consulta auditada é alcançável pela web (RN24, RN20)");

        return CompositeArchRule.of(somenteLeituraEmDistribuicao).and(controllersDeOutrosModulos)
                .as("Regra 3 (RN24): nenhum @RestController com método que altere designação ou dispare lote");
    }

    private static DescribedPredicate<JavaClass> controllers() {
        return DescribedPredicate.describe("são @RestController ou @Controller",
                tipo -> anotadoOuMetaAnotado(tipo, REST_CONTROLLER) || anotadoOuMetaAnotado(tipo, CONTROLLER));
    }

    private static boolean anotadoOuMetaAnotado(JavaClass tipo, String anotacao) {
        return tipo.isAnnotatedWith(anotacao) || tipo.isMetaAnnotatedWith(anotacao);
    }

    private static ArchCondition<JavaClass> exporSoEndpointsDeLeitura() {
        return new ArchCondition<>("expor só endpoints GET, HEAD ou OPTIONS") {
            @Override
            public void check(JavaClass controller, ConditionEvents eventos) {
                Set<String> daClasse = controller.getAnnotations().stream()
                        .map(RegrasDeArquitetura::metodosHttpDoMapeamento)
                        .flatMap(Optional::stream).findFirst().orElse(Set.of());
                for (JavaMethod metodo : controller.getMethods()) {
                    Optional<Set<String>> doMetodo = metodo.getAnnotations().stream()
                            .map(RegrasDeArquitetura::metodosHttpDoMapeamento)
                            .flatMap(Optional::stream).findFirst();
                    if (doMetodo.isEmpty()) {
                        continue;
                    }
                    Set<String> efetivos = doMetodo.get().isEmpty() ? daClasse : doMetodo.get();
                    if (efetivos.isEmpty() || !METODOS_HTTP_DE_LEITURA.containsAll(efetivos)) {
                        eventos.add(SimpleConditionEvent.violated(metodo, metodo.getFullName()
                                + " atende " + (efetivos.isEmpty() ? "qualquer método HTTP" : efetivos)
                                + " em " + metodo.getSourceCodeLocation()));
                    }
                }
            }
        };
    }

    /**
     * Métodos HTTP de uma anotação de mapeamento ({@code @RequestMapping} ou composta, como
     * {@code @PostMapping}); vazio quando não restringe método; ausente quando não é mapeamento.
     */
    private static Optional<Set<String>> metodosHttpDoMapeamento(JavaAnnotation<?> anotacao) {
        return metodosHttpDoMapeamento(anotacao, new HashSet<>());
    }

    private static Optional<Set<String>> metodosHttpDoMapeamento(JavaAnnotation<?> anotacao, Set<String> visitadas) {
        JavaClass tipo = anotacao.getRawType();
        if (tipo.getName().equals(REQUEST_MAPPING)) {
            Set<String> metodos = new LinkedHashSet<>();
            anotacao.get("method").ifPresent(valor -> {
                if (valor instanceof JavaEnumConstant[] constantes) {
                    for (JavaEnumConstant constante : constantes) {
                        metodos.add(constante.name());
                    }
                }
            });
            return Optional.of(metodos);
        }
        if (!visitadas.add(tipo.getName()) || tipo.getPackageName().startsWith("java.lang.annotation")) {
            return Optional.empty();
        }
        for (JavaAnnotation<JavaClass> meta : tipo.getAnnotations()) {
            Optional<Set<String>> metodos = metodosHttpDoMapeamento(meta, visitadas);
            if (metodos.isPresent()) {
                return metodos;
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 4: todo método público de @RestController tem anotação de autorização.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra4EndpointComAutorizacao(String raiz) {
        DescribedPredicate<JavaClass> controllerNaRaiz = DescribedPredicate.describe(
                "são @RestController ou @Controller em " + raiz + "..",
                tipo -> controllers().test(tipo) && tipo.getPackageName().startsWith(raiz + "."));
        ArchCondition<JavaMethod> terPreAuthorize = new ArchCondition<>(
                "ter @PreAuthorize no próprio método (direta ou numa anotação composta)") {
            @Override
            public void check(JavaMethod metodo, ConditionEvents eventos) {
                boolean autorizado = metodo.isAnnotatedWith(PRE_AUTHORIZE) || metodo.isMetaAnnotatedWith(PRE_AUTHORIZE);
                if (!autorizado) {
                    eventos.add(SimpleConditionEvent.violated(metodo, metodo.getFullName()
                            + " não declara @PreAuthorize em " + metodo.getSourceCodeLocation()));
                }
            }
        };
        return methods()
                .that().arePublic()
                .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
                .and().doNotHaveModifier(JavaModifier.BRIDGE)
                .and().areDeclaredInClassesThat(controllerNaRaiz)
                .should(terPreAuthorize)
                .allowEmptyShould(true)
                .because("todo endpoint declara papel e escopo; endpoint público usa @PreAuthorize(\"permitAll()\")"
                        + " explicitamente (doc 08)")
                .as("Regra 4: todo método público de @RestController tem anotação de autorização");
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 5 (invariante 5): repositório de tipo @Imutavel não expõe save nem delete.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra5RepositorioDeImutavelSemSaveNemDelete(String raiz) {
        DescribedPredicate<JavaClass> repositorio = DescribedPredicate.describe(
                "são repositórios (Spring Data Repository ou nome *Repository)",
                tipo -> tipo.isAssignableTo(REPOSITORIO_SPRING_DATA) || tipo.getSimpleName().endsWith("Repository"));
        ArchCondition<JavaClass> semAlteracao = new ArchCondition<>(
                "não expor métodos " + PREFIXOS_DE_ALTERACAO + "* quando gerenciam tipo @Imutavel") {
            @Override
            public void check(JavaClass repo, ConditionEvents eventos) {
                Set<JavaClass> imutaveis = tiposImutaveisGerenciados(repo);
                if (imutaveis.isEmpty()) {
                    return;
                }
                for (JavaMethod metodo : repo.getAllMethods()) {
                    String nome = metodo.getName();
                    if (PREFIXOS_DE_ALTERACAO.stream().anyMatch(nome::startsWith)) {
                        eventos.add(SimpleConditionEvent.violated(metodo, repo.getName() + " gerencia "
                                + imutaveis.stream().map(JavaClass::getSimpleName).sorted().toList()
                                + " (@Imutavel) e expõe " + metodo.getFullName()));
                    }
                }
            }
        };
        return classes().that(repositorio).and().resideInAPackage(raiz + "..")
                .should(semAlteracao)
                .allowEmptyShould(true)
                .because("nada é apagado; correção é novo registro (invariante 5, doc 05)")
                .as("Regra 5: nenhum repositório expõe delete* nem save sobre entidades marcadas @Imutavel");
    }

    /** Tipos @Imutavel que aparecem nos argumentos genéricos dos supertipos ou na assinatura dos métodos. */
    private static Set<JavaClass> tiposImutaveisGerenciados(JavaClass repo) {
        Set<JavaClass> candidatos = new LinkedHashSet<>();
        coletarArgumentosGenericos(repo, candidatos, new HashSet<>());
        for (JavaMethod metodo : repo.getAllMethods()) {
            candidatos.addAll(metodo.getRawParameterTypes());
            candidatos.add(metodo.getRawReturnType());
        }
        Set<JavaClass> imutaveis = new LinkedHashSet<>();
        for (JavaClass candidato : candidatos) {
            JavaClass base = candidato.getBaseComponentType();
            if (base.isAnnotatedWith(IMUTAVEL)) {
                imutaveis.add(base);
            }
        }
        return imutaveis;
    }

    private static void coletarArgumentosGenericos(JavaType tipo, Set<JavaClass> saida, Set<String> visitados) {
        if (tipo instanceof JavaParameterizedType parametrizado) {
            for (JavaType argumento : parametrizado.getActualTypeArguments()) {
                saida.add(argumento.toErasure());
                coletarArgumentosGenericos(argumento, saida, visitados);
            }
        }
        JavaClass bruto = tipo.toErasure();
        if (!visitados.add(bruto.getName())) {
            return;
        }
        bruto.getSuperclass().ifPresent(superclasse -> coletarArgumentosGenericos(superclasse, saida, visitados));
        for (JavaType interfaceImplementada : bruto.getInterfaces()) {
            coletarArgumentosGenericos(interfaceImplementada, saida, visitados);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 6: nenhuma classe lê o relógio do sistema; usa Relogio.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra6TempoSoPeloRelogio(String raiz) {
        String compartilhado = raiz + ".compartilhado";
        DescribedPredicate<JavaClass> implementacaoDoRelogio = DescribedPredicate.describe(
                "são o Relogio do compartilhado",
                tipo -> tipo.getPackageName().equals(compartilhado) && tipo.getSimpleName().startsWith("Relogio"));
        DescribedPredicate<JavaAccess<?>> leituraDoRelogioDoSistema = DescribedPredicate.describe(
                "lê o relógio do sistema (java.time *.now, Clock.system*, System.currentTimeMillis, new Date(),"
                        + " Calendar.getInstance, new GregorianCalendar())",
                RegrasDeArquitetura::leRelogioDoSistema);
        return noClasses()
                .that().resideInAPackage(raiz + "..")
                .and(DescribedPredicate.not(implementacaoDoRelogio))
                .should().accessTargetWhere(leituraDoRelogioDoSistema)
                .allowEmptyShould(true)
                .because("tempo sempre via Relogio injetável (doc 13); prazos e semanas precisam ser testáveis e"
                        + " reproduzíveis")
                .as("Regra 6: nenhuma classe usa LocalDate.now()/Instant.now() (nem equivalentes); usar Relogio");
    }

    private static boolean leRelogioDoSistema(JavaAccess<?> acesso) {
        AccessTarget alvo = acesso.getTarget();
        String dono = alvo.getOwner().getName();
        String nome = alvo.getName();
        boolean semParametros = alvo instanceof AccessTarget.CodeUnitAccessTarget unidade
                && unidade.getRawParameterTypes().isEmpty();
        return (TIPOS_JAVA_TIME_COM_NOW.contains(dono) && nome.equals("now"))
                || (dono.equals("java.time.Clock") && nome.startsWith("system"))
                || (dono.equals("java.lang.System") && nome.equals("currentTimeMillis"))
                || (dono.equals("java.util.Date") && nome.equals("<init>") && semParametros)
                || (dono.equals("java.util.GregorianCalendar") && nome.equals("<init>") && semParametros)
                || (dono.equals("java.util.Calendar") && nome.equals("getInstance"));
    }

    // ------------------------------------------------------------------------------------------------
    // Regra 7 (invariante 8): nenhum literal numérico de prazo em prazos e secretaria.
    // ------------------------------------------------------------------------------------------------

    public static ArchRule regra7SemLiteralNumericoDePrazo(String raiz) {
        String[] pacotes = MODULOS_SEM_LITERAL_DE_PRAZO.stream().map(modulo -> raiz + "." + modulo + "..")
                .toArray(String[]::new);
        return classes().that().resideInAnyPackage(pacotes)
                .should(LiteraisNumericos.naoConterLiteralDePrazo())
                .allowEmptyShould(true)
                .because("prazo é configuração do regimento (invariante 8, doc 06); em prazos e secretaria só"
                        + " -1, 0 e 1 podem aparecer como literal")
                .as("Regra 7: nenhum literal numérico de prazo em classes de " + MODULOS_SEM_LITERAL_DE_PRAZO
                        + " fora de testes");
    }

    // ------------------------------------------------------------------------------------------------
    // Compartilhado: só JDK (doc 03 "sem regra de negócio"; doc 13 "domínio sem Spring, JPA ou biblioteca").
    // ------------------------------------------------------------------------------------------------

    public static ArchRule compartilhadoSoDependeDoJdk(String raiz) {
        String compartilhado = raiz + ".compartilhado";
        return classes().that().resideInAPackage(compartilhado + "..")
                .and().doNotHaveSimpleName("package-info")
                .should().onlyDependOnClassesThat().resideInAnyPackage("java..", compartilhado + "..")
                .allowEmptyShould(true)
                .because("tipos de valor não dependem de Spring, JPA nem biblioteca externa (doc 13)")
                .as("Compartilhado: tipos de valor dependem só do JDK");
    }
}
