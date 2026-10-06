# 12 — Testes e qualidade

## Pirâmide

| Nível | Ferramenta | Escopo | Obrigatório em |
|---|---|---|---|
| Unidade | JUnit 5, AssertJ | Domínio puro (agregados, máquina de estados, cálculo de prazo, apuração) | Todo PT |
| Propriedade | jqwik | Distribuição, rodízio, prazos com calendário | PTs de `distribuicao`, `prazos`, `sessao` |
| Módulo | Spring Modulith `@ApplicationModuleTest` | Um módulo com dependências simuladas | Todo PT de backend |
| Integração | Testcontainers (PostgreSQL, MinIO, Keycloak) | Persistência, imutabilidade, outbox | Todo PT que cria tabela ou porta |
| Arquitetura | ArchUnit + `ApplicationModules.verify()` | Fronteiras e regras de segurança | Sempre, no build |
| Contrato | OpenAPI + testes de adaptador | Portas de integração | PTs de integração |
| E2E | Playwright | Jornadas do cidadão e roteiro de sessão | Marcos A1 e A2 |
| Acessibilidade | axe-core | Todas as telas | PTs de frontend |
| Carga | Gatling ou k6 | Cenário SP (doc 03) | A2 e antes de cada go-live |

## Testes obrigatórios por regra

- Cada `RNxx` tocada: ao menos um teste que prova a regra e um que prova a recusa do caso inválido. Nome do teste começa com o id: `RN26_resultado_fora_do_rol_e_recusado`.
- Testes de regra rodam contra `RegimentoFixtures.sp()` e, quando a regra é parametrizada, contra ao menos uma variação.

## Regras ArchUnit obrigatórias (PT-02)

1. Apenas classes de `br.com.sirej.distribuicao..` acessam tipos `Designacao*`, `Selo*`, `Semente*`.
2. Nenhuma classe acessa pacote `internal` de outro módulo.
3. Nenhum `@RestController` em `distribuicao` com método que altere designação ou dispare lote.
4. Todo método público de `@RestController` tem anotação de autorização.
5. Nenhum repositório expõe `delete*` nem `save` sobre entidades marcadas `@Imutavel`.
6. Nenhuma classe de domínio usa `LocalDate.now()`/`Instant.now()`; usar `Relogio`.
7. Nenhum literal numérico de prazo (`30`, `15`) em classes de `prazos` e `secretaria` fora de testes (verificação por regra customizada).

### Como as regras são verificadas

As regras ficam em `backend/app/src/test/java/br/com/sirej/arquitetura/RegrasDeArquitetura.java` e rodam no `./mvnw verify` contra todas as classes de produção de `br.com.sirej` (sem classes de teste), junto com `ApplicationModules.verify()`. Cada regra recebe o pacote raiz como parâmetro e tem dois testes em `RegrasDeArquiteturaPegamViolacoesTest`: um que importa uma violação plantada em `src/test/java/fixturearquitetura/<regra>/violacao` e exige a reprovação, e outro que importa um exemplo conforme em `.../<regra>/conforme` e exige a aprovação. As fixtures ficam fora de `br.com.sirej` e nunca em `src/main`.

| Regra | Critério exato |
|---|---|
| 1 | Fora de `distribuicao..`, nenhuma classe depende de tipo cujo nome comece com `Designacao`, `Selo` ou `Semente`, e nenhum tipo com esses prefixos é declarado. Únicas exceções: `DesignacaoConsulta` e `DesignacaoRevelada` no pacote raiz de `distribuicao` (D-39). |
| 2 | Uma classe só depende de outro módulo pelo pacote raiz dele ou pelo subpacote `api..` (doc 03, regra 1). `internal`, `dominio`, `aplicacao`, `infraestrutura`, `web` e qualquer outro subpacote são internos. |
| 3 | Em `distribuicao..`, todo método de `@RestController`/`@Controller` com mapeamento atende só `GET`, `HEAD` ou `OPTIONS`; mapeamento sem método HTTP declarado conta como violação. Controller de outro módulo não depende de nenhum tipo de `distribuicao` além de `DesignacaoConsulta` (D-39). |
| 4 | Todo método público declarado em `@RestController`/`@Controller` tem `@PreAuthorize` no próprio método, direta ou por anotação composta; na classe não basta. Endpoint público declara `@PreAuthorize("permitAll()")` (D-42). |
| 5 | Repositório (subtipo de Spring Data `Repository` ou nome `*Repository`) que gerencia tipo `@Imutavel` (argumento genérico ou assinatura de método) não tem método, próprio ou herdado, com prefixo `save`, `delete`, `remove`, `update`, `merge`, `salvar`, `apagar`, `excluir`, `remover`, `atualizar` ou `alterar` (D-41). |
| 6 | Nenhuma classe de produção (não só de domínio) chama ou referencia `now()` de `Instant`, `LocalDate`, `LocalDateTime`, `LocalTime`, `ZonedDateTime`, `OffsetDateTime`, `OffsetTime`, `Year`, `YearMonth`, `MonthDay`; `Clock.system*`; `System.currentTimeMillis()`; `new Date()`; `new GregorianCalendar()`; `Calendar.getInstance()`. Exceção: o `Relogio` de `compartilhado`. |
| 7 | Em classes de produção de `prazos..` e `secretaria..`, o bytecode não contém literal numérico diferente de `-1`, `0` e `1`: no código (inclusive lambdas), em constantes de campo e em valores de anotação. Ignora o que o compilador gera (ordinal de enum, `$values()`, classes sintéticas, métodos ponte). Literal `char` também conta. Bytecode ilegível reprova (D-40). |
| extra | `compartilhado` depende só do JDK (doc 03 e doc 13). |

## Testes de imutabilidade (banco)

Para cada tabela **[imutável]** do doc 05: `UPDATE` e `DELETE` pelo usuário da aplicação falham.

## Requisitos não funcionais verificáveis

| Id | Requisito | Verificação |
|---|---|---|
| RNF01 | Consulta de andamento p95 < 500 ms | Teste de carga |
| RNF02 | Abertura de autos (50 docs) p95 < 2 s | Teste de carga |
| RNF03 | Registro de voto p95 < 400 ms | Teste de carga |
| RNF04 | Pauta de 500 processos < 10 s | Teste de integração com volume |
| RNF05 | Distribuição semanal (escala SP) < 5 min | Teste de integração com volume |
| RNF06 | Disponibilidade ≥ 99,9% na janela de sessão; ≥ 99,5% em horário útil | Monitoramento |
| RNF07 | RPO ≤ 15 min; RTO ≤ 4 h | Ensaio trimestral de restauração |
| RNF08 | WCAG 2.1 AA | axe-core + avaliação manual |
| RNF09 | OWASP ASVS nível 2 | Checklist + pentest |
| RNF10 | Instalação do zero em ambiente limpo, automatizada | Pipeline a cada release |

## Definição de pronto (DoD)

1. Critérios de aceite do PT automatizados e passando.
2. `./mvnw verify` verde (inclui ArchUnit e Modulith); testes do frontend verdes.
3. Cobertura de linhas ≥ 80% nos pacotes de domínio tocados; 100% das RN tocadas com teste positivo e negativo.
4. Nenhuma invariante do `CLAUDE.md` enfraquecida.
5. Migração Flyway nova é retrocompatível (mudança destrutiva em duas fases).
6. Documentação em `docs/dev/` atualizada se comportamento, parâmetro, evento ou endpoint mudou.
7. PR cita `PT-xx` e `RNxx`.

## Dados de teste

Somente sintéticos. Geradores em `backend/compartilhado/src/testFixtures`: CPF/CNPJ válidos fictícios, placas, AITs, juntas no formato SP (27 × 6) e Curitiba (4 × 6 + 3 suplentes).
