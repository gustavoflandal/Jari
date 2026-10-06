# 06 — Parametrização do regimento

**Regra de ouro:** se um valor pode variar entre órgãos, ele vem da configuração. Número, prazo, lista de resultados, composição, quórum, roteiro ou política de publicidade escritos no código são defeito.

O regimento de SP foi trocado por um simples Comunicado em 2023. O produto atende prefeituras e DETRANs de vários estados. Os dois fatos tornam a configuração requisito central do produto.

## Arquivos

- `config/regimentos/sp.yaml` — configuração de **referência**. Todos os testes de regra rodam contra ela.
- `config/regimentos/curitiba.yaml` — segunda configuração; herda de `sp` e sobrescreve.
- Cada instalação carrega **uma** configuração (`SIREJ_REGIMENTO=sp`).

## Ciclo de vida

1. No startup, o módulo `configuracao` lê o YAML, resolve `herda`, valida contra o esquema (JSON Schema em `config/regimentos/esquema.json`, a criar no PT-03) e contra as **regras de consistência** abaixo. Falha de validação impede a subida da aplicação.
2. A configuração validada vira uma `regimento_versao` imutável, com hash. Se o conteúdo não mudou, reaproveita a versão existente.
3. Todo ato que depende de regra grava a `config_versao` vigente (movimentação, voto, decisão, lote de distribuição). Assim, um processo antigo pode ser reconstituído com a regra da época.
4. Alteração em produção: proposta pela Administração (M9), aprovada por outra pessoa, com vigência futura, auditada. Sem deploy. Regras de vigência e não retroatividade no doc 18, seção 4 (RN38, RN39).
5. Depois da instalação, o **banco é a fonte de verdade**. Um YAML diferente no pacote não é aplicado sozinho: gera alerta e só entra por importação aprovada (ADR-0011).

## Acesso no código

- Use `RegimentoVigente` (API pública do módulo `configuracao`), nunca leia o YAML diretamente.
- Objetos de configuração são imutáveis e tipados (`record`). Ex.: `regimento.prazos().recurso1a().dias()`.
- Em testes, use `RegimentoFixtures.sp()` e variações (`RegimentoFixtures.sp().comVotacao(...)`) para cobrir as alternativas.

## Seções e semântica

| Seção | Controla | Regras ligadas |
|---|---|---|
| `orgao` | Identificação, esfera, UF, fuso, CETRAN competente | — |
| `modulos` | Quais módulos opcionais estão ativos; módulo inativo não expõe rotas nem telas | — |
| `identidade` | Provedores de login do cidadão; nível mínimo por ato; MFA interno | RN32 |
| `calendario` | Abrangência de feriados; prorrogação de vencimento | RN31 |
| `prazos` | Todos os prazos, com a origem da contagem e os alertas | RN01, RN02, RN04, RN08, RN10, RN37 |
| `pecas` | Tipos de peça ativos, decisor, se viram processo de JARI | RN03, RN15, RN16 |
| `composicao` | Segmentos, posições por junta, suplentes, presidência, mandato | RN21 |
| `designacao` | `SIGILOSO` ou `ABERTO`; publicidade dos nomes após julgamento | RN20 |
| `distribuicao` | Periodicidade, equidade, conexão, ordem, redistribuição | RN19, RN24, RN27 |
| `turmas` | Tamanho, segmentos, simultaneidade, rodízio, restrição de presidência | RN21, RN25 |
| `sessao` | Quórum, executor da distribuição interna, substituição, roteiro, modalidades | RN22, RN35 |
| `votacao` | Votos mínimos, exceção de maioria simples, ordem, visibilidade | RN07, RN14 |
| `resultados` | Rol fechado e se cada resultado altera a penalidade | RN26, RN12 |
| `relatoria` | Itens do checklist do relator | RN13 |
| `impedimento` | Motivos tipificados de impedimento e suspeição | RN34 |
| `diligencia` | Requisitos da diligência presencial | RN28 |
| `autos` | Download por membros, marca d'água, acesso fora da sessão | RN29 |
| `presenca`, `mandato` | Cancelamento de presença, métricas de perda de mandato | RN30, RN36 |
| `documentos` | Formatos, tamanho, documentos obtidos de ofício | RN33 |
| `retencao` | Anos de retenção | — |
| `administracao` | Aprovadores por tipo de mudança, antecedência mínima da vigência, papéis sensíveis, vigência máxima de papel | RN38, RN39, RN42 |
| `temporalidade` | Classes documentais, prazos de guarda e destinação | RN44 |

## Regras de consistência (validação obrigatória)

1. `distribuicao.falhaFechada` só aceita `true`. Qualquer outro valor impede a subida (invariante 2).
2. `resultados` não vazio, códigos únicos, ao menos um com `alteraPenalidade: true`.
3. `turmas.membrosPorTurma` ímpar e igual ao número de segmentos quando `umPorSegmento: true`.
4. `votacao.votosMinimos` ≤ `turmas.membrosPorTurma`; `excecaoMaioriaSimples.minimo` ≥ 2.
5. `composicao.posicoesPorJunta` tem ao menos `membrosPorTurma` posições, cada segmento declarado em `segmentos`.
6. Todo prazo tem `dias` > 0 ou origem explícita (`IMPRESSO_NA`); `null` só para metas sem prazo legal.
7. `designacao.modo = SIGILOSO` exige KMS configurado na instalação (verificação de conectividade no startup).
8. Não existe chave para voto de qualidade, peso de voto ou desempate. Se aparecer no YAML, a validação falha (invariante 3).
9. Cada tipo em `administracao.aprovadores` tem ao menos um papel, e nenhum deles é `ADMIN` para `REGIMENTO` (quem parametriza não aprova a própria área; RN38).
10. Toda classe de `temporalidade` tem guarda total (corrente + intermediária) ≥ `retencao.anos`; `temporalidade.eliminacaoFisica` só aceita `false` enquanto a D-19 estiver aberta.

## Valores PROVISÓRIOS

Itens marcados `PROVISÓRIO` em `sp.yaml` são escolhas da equipe onde a norma é omissa ou ambígua. Cada um tem uma entrada em `16-duvidas-abertas.md`. Implemente o parâmetro e use o valor provisório; não fixe o valor no código.

## Como adicionar um parâmetro

1. Confirme que a regra realmente varia entre órgãos ou é ambígua na norma.
2. Adicione ao esquema, ao `record` tipado, ao `sp.yaml` (com comentário citando a norma) e a esta tabela.
3. Adicione regra de consistência, se houver combinação inválida.
4. Teste ao menos dois valores do parâmetro.
