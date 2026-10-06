# 03 — Arquitetura e módulos

Decisões formais nos ADRs (`docs/adr/`). Este documento diz **onde cada coisa mora** e **quem pode depender de quem**.

## Estilo

- **Monólito modular** com Spring Modulith (ADR-0001). Um único deployable de backend, módulos com fronteira imposta em build.
- **Instalação única por órgão** (ADR-0007): não há coluna `tenant_id`. A configuração do órgão vem de `config/regimentos/<orgao>.yaml`, carregada e validada no startup e versionada no banco.
- **Event sourcing restrito ao processo** (ADR-0004): a movimentação processual é um log imutável; a situação do processo e a linha do tempo são projeções.
- **Outbox transacional no PostgreSQL** para eventos de integração (ADR-0003). Sem broker externo até haver necessidade comprovada.
- **Portas e adaptadores** para todo sistema externo (doc 09).

## Pacote raiz

`br.com.sirej` (provisório; o nome final da empresa pode trocar o prefixo, nunca a estrutura).

## Módulos de código

| Módulo (pacote) | Responsabilidade | Módulo de produto |
|---|---|---|
| `compartilhado` | Tipos de valor (Cpf, Cnpj, Placa, Renavam, Hash, NumeroProcesso), `Relogio` injetável, erros base. Sem regra de negócio. | — |
| `configuracao` | Carga, validação e versionamento do regimento; propostas de alteração com aprovação e vigência; calendário; modelos de documento; importação e exportação (doc 18) | M9 |
| `auditoria` | Trilha append-only encadeada, ancoragem diária, consulta de trilha | M9 |
| `identidade` | Usuários internos e externos, atribuições de papel por escopo, segregação de funções, MFA, integração com o provedor OIDC (doc 18) | M9 |
| `pessoas` | Pessoa, veículo, procuração, representação | M1 |
| `documentos` | Upload, antivírus, PDF/A, hash, armazenamento S3, renditions, visualização com marca d'água, desentranhamento | transversal |
| `assinatura` | Assinatura PAdES, carimbo do tempo, validação | transversal |
| `prazos` | Motor de prazos: contagem, suspensão, vencimento, alertas | transversal |
| `processo` | Processo, máquina de estados, movimentações (event store), projeções de situação e linha do tempo | transversal |
| `peticionamento` | Rascunho, assistente por tipo de peça, protocolo, recibo, recepção de papel | M1 |
| `instrucao` | Auto eletrônico, informação do agente, defesa da autuação, efeito suspensivo, cumprimento | M2 |
| `secretaria` | Triagem, exigência, remessa por incompetência, painel de backlog | M3 |
| `composicao` | Juntas, posições, membros, segmentos, mandatos, suplentes, presidência, presença | M3 / M6 |
| `distribuicao` ⚠ | Distribuição semanal, selo, distribuição interna, turmas, rodízio, redistribuição, consulta auditada da designação | M3 |
| `sessao` | Pauta, edital de pauta, sessão, abertura, roteiro, ata | M3 / M4 |
| `julgamento` | Relatoria, dossiê, editor de voto, votação, apuração, acórdão, ementa, impedimento, suspeição, diligência | M4 |
| `notificacao` | Notificação oficial e de cortesia, comprovantes de ciência | transversal |
| `segundainstancia` | Recurso ao CETRAN, admissibilidade, remessa, retorno | M5 |
| `credenciamento` | Edital de credenciamento, entidades, sorteio, convocação, habilitação, provas, posse, perda de mandato | M6 |
| `publicidade` | Consulta pública, estatísticas regimentais, retorno sistêmico, dados abertos | M7 |
| `judicial` | Demandas judiciais, subsídio de defesa, consultas ao CETRAN/CONTRAN | M8 |
| `integracao` | Adaptadores de saída e entrada, outbox, reconciliação | transversal |

⚠ = módulo de segurança reforçada (doc 07 e 08).

## Regras de dependência

1. Um módulo só usa outro pela **API pública** (pacote raiz do módulo ou subpacote `api`). Pacotes `internal` são privados. Verificado por `ApplicationModules.verify()` e ArchUnit.
2. **Nenhum módulo lê tabela de outro.** Cada módulo é dono das suas tabelas (prefixo de schema por módulo, doc 05).
3. `compartilhado`, `configuracao` e `auditoria` podem ser usados por todos. `auditoria` não depende de nenhum módulo de negócio.
4. **Somente `distribuicao` lê ou grava a designação** (junta, posição, sequência, relator, turma) antes de ela ser revelada. Outros módulos recebem a designação por evento `DesignacaoRevelada` ou consultam `DesignacaoConsulta`, que audita cada chamada e recusa leitura antecipada no modo sigiloso. Regra ArchUnit dedicada (doc 12).
5. `integracao` depende das portas declaradas nos módulos de negócio, nunca o contrário.
6. O frontend nunca recebe dados que o backend não deveria revelar ao perfil; filtragem é sempre no servidor.

## Comunicação entre módulos

- **Chamada síncrona** pela API pública quando a operação precisa ser atômica com a transação do chamador.
- **Evento de aplicação** (Spring Modulith `@ApplicationModuleListener`, persistido no registro de eventos) para reações assíncronas dentro do monólito.
- **Evento de integração** (outbox) para sistemas externos.

Lista de eventos no doc 10.

## Transações críticas (atômicas)

| Transação | Inclui |
|---|---|
| Protocolo | número único + hash dos documentos + movimentação `PROTOCOLADO` + recibo + auditoria |
| Distribuição semanal | todo o lote + selo + compromisso na auditoria, ou nada |
| Abertura da sessão | presenças + quórum + distribuição interna + revelação + auditoria |
| Registro de voto | voto + assinatura + auditoria |
| Proclamação | decisão + acórdão + movimentação + evento de publicação |

## Visão de implantação

Contêineres em 2 ou 3 VMs, ou Kubernetes apenas se o órgão já opera (ADR-0008). Componentes: API/núcleo (3 instâncias), serviços de apoio (ClamAV, renditions, PDF), Keycloak, PostgreSQL primário + réplica de leitura, Redis, armazenamento S3 com Object Lock, HSM/KMS. Painéis e transparência leem da réplica.

## Metas de desempenho (p95)

| Operação | Meta |
|---|---|
| Consulta de andamento pelo cidadão | 500 ms |
| Abertura de autos com até 50 documentos | 2 s |
| Primeira imagem de AIT visível | 1,5 s |
| Registro de voto | 400 ms |
| Geração de pauta com 500 processos | 10 s |
| Distribuição semanal completa (escala SP) | 5 min |
