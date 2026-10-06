# Documentação de referência para desenvolvimento

Esta pasta é a **fonte de verdade** para quem desenvolve o SIREJ, humano ou agente. Quando algo aqui conflitar com `docs/fontes/`, vale o que está aqui (e nos ADRs em `docs/adr/`).

## Ordem de leitura

| # | Documento | Leia quando |
|---|---|---|
| 01 | [Visão e escopo](01-visao-e-escopo.md) | Sempre, na primeira tarefa |
| 02 | [Glossário e linguagem ubíqua](02-glossario.md) | Sempre, antes de nomear qualquer coisa |
| 03 | [Arquitetura e módulos](03-arquitetura.md) | Antes de criar classe, pacote ou dependência |
| 04 | [Domínio, estados e regras de negócio](04-dominio-e-regras.md) | Antes de qualquer regra ou transição de estado |
| 05 | [Modelo de dados](05-modelo-de-dados.md) | Antes de criar tabela ou migração |
| 06 | [Parametrização do regimento](06-parametrizacao.md) | Antes de escrever qualquer número, prazo ou lista fixa |
| 07 | [Distribuição, sessão e selo](07-distribuicao-e-selo.md) | Ao mexer em distribuição, pauta, turmas, sessão, votação |
| 08 | [Segurança e auditoria](08-seguranca.md) | Ao mexer em acesso, autenticação, auditoria, dados pessoais |
| 09 | [Integrações](09-integracoes.md) | Ao falar com qualquer sistema externo |
| 10 | [APIs e eventos](10-api-e-eventos.md) | Ao criar endpoint ou evento |
| 11 | [Frontend e UX](11-frontend-e-ux.md) | Ao mexer no portal ou no back-office |
| 12 | [Testes e qualidade](12-testes-e-qualidade.md) | Sempre, antes de entregar |
| 13 | [Convenções de código e repositório](13-convencoes.md) | Sempre |
| 14 | [Backlog em pacotes de trabalho](14-backlog-pacotes.md) | Para pegar uma tarefa |
| 15 | [Protocolo dos agentes](15-protocolo-agentes.md) | Sempre, antes de começar |
| 16 | [Dúvidas abertas](16-duvidas-abertas.md) | Ao encontrar lacuna; para registrar a sua |
| 18 | [Administração (M9)](18-administracao.md) | Ao mexer em parametrização, calendário, papéis, modelos de documento, temporalidade ou no console |

Os prompts para executar cada fase do plano com agentes estão em [`docs/prompts/`](../prompts/README.md).

## Rastreabilidade

- Regras de negócio têm id `RNxx` (doc 04). Requisitos não funcionais têm id `RNFxx` (doc 12). Pacotes de trabalho têm id `PT-xx` (doc 14).
- Todo teste que valida uma regra cita o id no nome ou em `@DisplayName` (ex.: `RN20_designacao_nao_legivel_antes_da_abertura`).
- Todo PR cita os `PT` e `RN` que toca.

## Documentos de origem

| Arquivo | Conteúdo |
|---|---|
| `docs/fontes/enunciado-projeto-sistema-jari.md` | Enunciado v1.0: contexto, escopo funcional, RN01–RN30, anexo de SP |
| `docs/fontes/arquitetura-infraestrutura-sirej.md` | Arquitetura e infraestrutura v1.0 |
| `docs/fontes/JARI_CET.pdf` | Edital 001/2026-JARI/CET e Regimento das JARIs de SP (Comunicado 007/23) |
| `docs/fontes/Estudo_JARI.pdf` | Notas de pesquisa |
| `docs/plano/plano-projeto-sirej.md` | Plano do projeto (trilhas, fases, riscos) |

Mudanças já decididas em relação às fontes: instalação única por órgão (não multi-tenant); designação sigilosa ou aberta por configuração; regras divergentes entre enunciado e regimento viram parâmetros (ver doc 06).
