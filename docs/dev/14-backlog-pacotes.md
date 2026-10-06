# 14 — Backlog em pacotes de trabalho

Cada pacote de trabalho (PT) é a unidade que um agente recebe. Um PT tem dono único, dependências explícitas e critérios de aceite verificáveis. Um agente só começa um PT quando todas as dependências estão mescladas em `main`.

Etapas do plano (doc `docs/plano/`): **A0** fundação, **A1** núcleo demonstrável, **A2** pronto para PoC, **A3** módulos complementares.

## Visão das dependências

```
PT-01 ─┬─ PT-02 ─┬─ PT-03 ─┬─ PT-05 ─────────────┐
       │         │         ├─ PT-12 ─┐           │
       │         ├─ PT-04 ─┤         │           │
       │         │         ├─ PT-06  │           │
       │         │         ├─ PT-07 ─┤           │
       │         │         └─ PT-09  │           │
       │         └─ PT-08 (dep. 03, 04, 05) ─────┤
       │                                         │
       │  PT-10 (dep. 08, 09) ── PT-11 (dep. 06, 07, 08, 10)
       │  PT-13 (dep. 08, 11) ── PT-14 (dep. 10, 13)
       │  PT-15 (dep. 08, 12) ── PT-16 ── PT-17
       │  PT-18 (dep. 12, 17) ── PT-19 ── PT-20 (dep. 22) ── PT-21 ── PT-23 ── PT-24
       │  PT-22 (dep. 07)
       │  PT-29 (dep. 03–06) ─┬─ PT-30 (dep. 12) ─┐
       │                      ├─ PT-31 (dep. 07) ─┼─ PT-33 (frontend)
       │                      └─ PT-32 (dep. 08) ─┘
       │  PT-34 (dep. 23, 24, 29): teste ponta a ponta da A1
       └─ PT-25 / PT-26 (frontend, dep. das APIs) ── PT-27 ── PT-28
```

Pode rodar em paralelo, depois do PT-03: PT-04, PT-05, PT-06, PT-07, PT-09, PT-12.

## Pacotes

### A0 — Fundação

| PT | Título | Módulo | Depende | RN | Critérios de aceite |
|---|---|---|---|---|---|
| PT-01 | Esqueleto do monorepo e CI | — | — | — | Maven multi-módulo com um módulo vazio por contexto do doc 03; frontend com `apps/portal`, `apps/backoffice`, `packages/tipos`; pipeline com build, testes, SAST/SCA; `./mvnw verify` e `npm test` verdes |
| PT-02 | Regras de arquitetura | compartilhado | 01 | — | `ApplicationModules.verify()`; as 7 regras ArchUnit do doc 12 implementadas (com testes que provam que pegam violação); `Relogio`, `@Imutavel`, tipos de valor (Cpf, Cnpj, Placa, Hash) |
| PT-03 | Configuração do regimento | configuracao | 02 | todas parametrizadas | JSON Schema; carga de `sp.yaml` e `curitiba.yaml` com `herda`; as 8 regras de consistência do doc 06 (com teste de falha para cada uma); `regimento_versao` imutável com hash; `RegimentoVigente`; `RegimentoFixtures` |
| PT-04 | Trilha de auditoria | auditoria | 02 | RN23 | Registro encadeado; gravação na mesma transação do chamador; tabela imutável no banco; job de verificação detecta adulteração simulada; ancoragem diária com carimbo simulado |

### A1 — Núcleo demonstrável

| PT | Título | Módulo | Depende | RN | Critérios de aceite |
|---|---|---|---|---|---|
| PT-05 | Calendário e motor de prazos | prazos | 03 | RN01, RN02, RN04, RN10, RN31, RN37 | Contagem a partir de ciência efetiva/presumida; prorrogação em dia não útil; feriados por abrangência; alertas T-10/5/1; testes de propriedade de calendário |
| PT-06 | Identidade e autorização | identidade | 03 | — | Keycloak em Testcontainers; papéis do doc 08; escopo por junta/sessão; adaptador gov.br simulado com níveis; MFA exigido para internos; endpoint sem anotação falha no build |
| PT-07 | Documentos | documentos | 03, 04 | RN11, RN29 | Upload com antivírus (simulado + ClamAV), PDF/A-2b, SHA-256, S3 MinIO com Object Lock; desentranhamento; visualização com marca d'água; membro não baixa; todo acesso auditado |
| PT-08 | Processo e movimentações | processo | 03, 04, 05 | máquina de estados | Agregado com todas as transições do doc 04; event store imutável; projeção de situação e linha do tempo reconstruível do log; transição inválida não grava nada |
| PT-09 | Pessoas, veículos, procurações | pessoas | 03 | — | CPF/CNPJ cifrado + hash de busca; procuração com escopo, validade e revogação |
| PT-10 | Porta do sistema de multas | instrucao, integracao | 08, 09 | RN18 | `SistemaMultasPort`, adaptador simulado com massa de AITs/NA/NP, contrato OpenAPI de referência, camada anticorrupção |
| PT-11 | Peticionamento e protocolo | peticionamento | 06, 07, 08, 10 | RN03, RN05, RN15, RN16, RN32, RN33, RN17 | Peças cabíveis por fase do AIT; rascunho; protocolo atômico com número, hash e recibo; idempotência; nenhum campo de pagamento; recurso de AIT com multa já paga é protocolado normalmente (RN17), com teste positivo e negativo |
| PT-12 | Composição | composicao | 03 | RN21 | Juntas, posições (letras por segmento), membros, mandatos titular/suplente, presidência; carga de 27 juntas SP e 4 de Curitiba por fixture |
| PT-13 | Triagem e exigência | secretaria | 08, 11 | RN01, RN09, RN17 | Tempestividade calculada; exigência com prazo e retorno à triagem; arquivamento por vencimento; inadmissão com motivo; remessa por incompetência preservando data; multa paga não é motivo de inadmissão (RN17) |
| PT-14 | Instrução mínima | instrucao | 10, 13 | — | Juntada automática do AIT e notificações; informação do agente com prazo; transição para `AGUARDANDO_DISTRIBUICAO` |
| PT-15 | Algoritmo de distribuição semanal | distribuicao | 08, 12 | RN19, RN27, RN06 | Grupos de conexão (union-find); prevenção; equidade; ordem cronológica; selo do processo; reprodutível por semente; testes de propriedade do doc 07 §10 |
| PT-16 | Selo commit–reveal | distribuicao, integracao | 15 | RN20 | `CofreChavesPort` (simulado + implementação KMS); compromisso na auditoria; designação cifrada; verificação pública; adulteração detectada; designação ilegível antes da revelação para todos os perfis |
| PT-17 | Job semanal com falha fechada | distribuicao | 16 | RN24 | ShedLock; transação única; suspensão com motivo; processos no próximo lote; teste de falha injetada em cada passo; nenhuma rota manual (ArchUnit) |
| PT-18 | Sessão, pauta e abertura | sessao | 12, 17 | RN22, RN27, RN36 | Agenda de sessões; pauta ordenada; presenças; quórum; abertura atômica com revelação; sem quórum nada é revelado; pauta sigilosa sem junta/posição antes da abertura; cancelamento da presença por recusa imotivada, com motivo registrado (RN36) |
| PT-19 | Distribuição interna e turmas | distribuicao, sessao | 18 | RN21, RN25, RN35 | Partições válidas; rodízio sem repetição até esgotar (propriedade); substituição por segmento; relator ausente → retorno à pauta; relatório para a Secretaria |
| PT-22 | Assinatura e carimbo do tempo | assinatura | 07 | RN23 | `AssinaturaPort` com DSS (PAdES) e adaptador simulado; carimbo do tempo simulado; validação de assinatura |
| PT-20 | Relatoria, votação e proclamação | julgamento | 19, 22 | RN07, RN13, RN14, RN23, RN26 | Relatório com checklist; voto com resultado do rol, fundamentação e dispositivo; ordem sequencial; apuração; exceção de 2 votos com presidente; voto assinado imutável; proclamação |
| PT-21 | Impedimento, diligência, redistribuição | julgamento, distribuicao | 20 | RN06, RN27, RN28, RN34, RN30 | Declaração tipificada e recomposição; contador por membro; diligência com prazo e retorno à pauta; presencial com 2 segmentos; redistribuição só por motivo e critério cadastrado; painel de evidências por membro (faltas seguidas e intercaladas, declarações de impedimento, prazo de relatoria) sem nenhuma ação automática de perda de mandato (RN30) |
| PT-23 | Ata, acórdão e publicação | sessao, julgamento, publicidade, instrucao | 21, 31 | RN10, RN26, RN12 | Ata, acórdão, ementa e certidão em PDF/A assinados; publicação (porta Diário Oficial simulada); abertura do prazo de 2ª instância; `instrucao` reage a `DecisaoPublicada` de decisão que altera a penalidade e grava `integracao.cumprimento.v1` no outbox para a `SistemaMultasPort` (adaptador simulado); nenhuma rota altera pontuação ou débito (RN12) |
| PT-24 | Notificações e prazos de julgamento | notificacao, prazos | 23 | RN08, RN37 | Notificação de cortesia e SNE simulado; comprovante de ciência; relatório diário de processos fora do prazo; lista de elegíveis a efeito suspensivo |
| PT-29 | Governança de configuração e calendário | configuracao | 03, 04, 05, 06 | RN38, RN39, RN40, RN41 | Ciclo da proposta (doc 18 §3) com diff e impacto; aprovação por pessoa distinta; vigência e não retroatividade por seção (doc 18 §4), com teste de lote e sessão iniciados antes da vigência; chaves de invariante recusadas; calendário com feriado, ponto facultativo e suspensão, revogação por novo registro, recálculo que só prorroga; importação e exportação YAML; alerta de pacote divergente; `PublicadorDeVersoes` idempotente com ShedLock |
| PT-30 | Papéis por escopo e segregação de funções | identidade | 06, 12, 29 | RN42, RN45 | Atribuição com escopo e fim; `MEMBRO`/`PRESIDENTE` só por mandato (evento de `composicao`); dupla aprovação de papéis sensíveis; autoconcessão recusada; tabela de incompatibilidades com teste para cada linha; suspensão automática por conflito; revogação encerra sessões; recertificação mensal; `ADMIN` recebe 403 em rotas de processo, documento e designação |
| PT-31 | Modelos de documento | configuracao | 07, 29 | RN43 | Versões imutáveis por tipo; campos de lista fechada; campos de designação recusados em pauta, recibo e exigência; pré-visualização só com dados sintéticos; documento gerado grava versão e hash do modelo; modelos de referência de SP na instalação |
| PT-32 | Temporalidade e fase arquivística | processo, documentos | 08, 29 | RN44 | Classes da configuração; fase arquivística separada da situação; job mensal de transição; lista de elegíveis sem processo com demanda judicial ou retenção legal; nenhuma rota de eliminação (ArchUnit) |
| PT-33 | Console administrativo | frontend | APIs de 04, 29, 30, 31 | RN38, RN45 | Telas do doc 18 §10; diff e impacto antes de submeter e aprovar; confirmação em aprovar, rejeitar e revogar; nenhuma tela mostra processo ou designação; axe-core sem violações |
| PT-34 | Sessão completa ponta a ponta (critério de saída da A1) | qualidade | 23, 24, 29 | todas da A1 | Teste de integração com regimento `sp.yaml` e adaptadores simulados: proposta de mudança de prazo aprovada com vigência futura → protocolo → triagem → instrução → distribuição semanal → pauta → abertura com revelação → turmas → votação → proclamação → ata e acórdão assinados → publicação; ao fim, verificação pública do selo e da cadeia de auditoria, e prova de que processo protocolado antes da vigência manteve o prazo antigo |
| PT-25 | Back-office: relator, presidente, secretaria | frontend | APIs de 13, 18–21 | RN29 | Telas do doc 11; caminho relatar→votar→próximo com um clique; painel do presidente com roteiro; axe-core sem violações |

### A2 — Pronto para PoC

| PT | Título | Módulo | Depende | RN | Critérios de aceite |
|---|---|---|---|---|---|
| PT-26 | Portal do recorrente | frontend | APIs de 11, 08 | RN03, RN05 | Jornada placa → peça → anexos → assinatura → recibo em ≤ 4 passos; linha do tempo; linguagem simples; mobile; axe-core |
| PT-27 | Instalador e ambiente de demonstração | infraestrutura | 25, 26 | RNF10, RNF07 | Instalação do zero automatizada (infra como código); carga de configuração do órgão; massa de demonstração; atualização de versão sem perda; observabilidade e alertas do doc 19 §6; backup e restauração ensaiados com RPO e RTO medidos; runbooks do doc 19 §9; recusa atualizar com sessão aberta ou lote em execução |
| PT-28 | E2E de sessão e carga | qualidade | 27 | RNF01–05 | Playwright: protocolo → triagem → distribuição → abertura → votação → publicação; carga no cenário SP |

### A3 — Módulos complementares (detalhar depois)

M2 completo (efeito suspensivo, cumprimento real, restituição da multa paga, RN17), M5 (2ª instância e CETRAN), M6 (credenciamento completo), M7 (transparência, retorno sistêmico, painéis), M8 (demandas judiciais), adaptadores SNE/CDT, RENAINF, Diário Oficial reais, identidade digital do PR, base de precedentes, apoio de IA à triagem.
