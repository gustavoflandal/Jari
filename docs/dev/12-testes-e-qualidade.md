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
