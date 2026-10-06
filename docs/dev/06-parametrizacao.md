# 06 — Parametrização do regimento

**Regra de ouro:** se um valor pode variar entre órgãos, ele vem da configuração. Número, prazo, lista de resultados, composição, quórum, roteiro ou política de publicidade escritos no código são defeito.

O regimento de SP foi trocado por um simples Comunicado em 2023. O produto atende prefeituras e DETRANs de vários estados. Os dois fatos tornam a configuração requisito central do produto.

## Arquivos

- `config/regimentos/sp.yaml` — configuração de **referência**. Todos os testes de regra rodam contra ela.
- `config/regimentos/curitiba.yaml` — segunda configuração; herda de `sp` e sobrescreve.
- `config/regimentos/esquema.json` — JSON Schema (draft 2020-12) do regimento resolvido. Toda seção e toda chave são obrigatórias; chave fora do esquema é recusada.
- Cada instalação carrega **uma** configuração (`SIREJ_REGIMENTO=sp`). Sem essa variável a aplicação não sobe. Os arquivos de `config/regimentos` vão no pacote (`classpath:regimentos/`); `SIREJ_REGIMENTOS_LOCAL` aponta outro diretório, com recurso ao pacote para o `herda`. O esquema vem sempre do pacote.
- **Herança** (`herda: <nome>`): objetos são mesclados chave a chave, recursivamente; listas e valores simples do filho substituem os do pai por inteiro; `null` explícito também substitui; vários níveis são aceitos; ciclo ou pai inexistente reprova (D-51).
- Todo valor do `sp.yaml` tem comentário com o artigo de origem, a norma externa ou a `D-xx` (teste `PT03_cada_valor_do_sp_yaml_cita_artigo_ou_duvida`).

## Ciclo de vida

1. No startup, o módulo `configuracao` lê o YAML (chave repetida reprova), resolve `herda`, aplica a regra de consistência 8 às chaves cruas, valida contra o esquema (`config/regimentos/esquema.json`) e aplica as demais **regras de consistência** abaixo. Falha de validação impede a subida da aplicação, com mensagem que cita a regra (`[regra 3 do doc 06] turmas.membrosPorTurma: ...`) ou `[esquema]`.
2. A configuração validada vira uma `regimento_versao` imutável, com hash: SHA-256 (`Hash` do `compartilhado`) do **conteúdo canônico** (JSON do regimento resolvido, chaves em ordem, sem espaços; comentários e ordem do YAML não mudam o hash). Com o banco vazio, vira a primeira versão (`aplicado_por = INSTALADOR`) e, na mesma transação, grava o registro de auditoria `REGIMENTO_VERSAO_CRIADA` (`TrilhaAuditoria`) e publica `RegimentoVersaoPublicada`; falha da auditoria desfaz a versão e impede a subida (D-48). Se o conteúdo não mudou, reaproveita a versão existente; se mudou, vale o item 5. Na subida, o hash de cada versão gravada é conferido contra o conteúdo (adulteração impede a subida) e a versão é validada de novo.
3. Todo ato que depende de regra grava a `config_versao` vigente (movimentação, voto, decisão, lote de distribuição). Assim, um processo antigo pode ser reconstituído com a regra da época.
4. Alteração em produção: proposta pela Administração (M9), aprovada por outra pessoa, com vigência futura, auditada. Sem deploy. Regras de vigência e não retroatividade no doc 18, seção 4 (RN38, RN39).
5. Depois da instalação, o **banco é a fonte de verdade**. Um YAML diferente no pacote não é aplicado sozinho: gera alerta e só entra por importação aprovada (ADR-0011).

## Acesso no código

- Use `RegimentoVigente` (API pública do módulo `configuracao`), nunca leia o YAML diretamente. `versao()` dá a versão vigente (id para gravar como `config_versao`, hash, vigência); `versao(id)` dá uma versão histórica. O regimento é carregado no início do ciclo de vida da aplicação: nenhum bean o lê durante a própria inicialização.
- Objetos de configuração são imutáveis e tipados (`record`). Ex.: `regimento.prazos().recurso1a().dias()`.
- Em testes, use `RegimentoFixtures.sp()` e variações (`RegimentoFixtures.sp().comVotacao(...)`) para cobrir as alternativas; `RegimentoFixtures.vigente(regimento)` dá um `RegimentoVigente` fixo e `RegimentoFixtures.violacoes(regimento)` confere uma variação contra as regras. Ficam em `backend/configuracao/src/testFixtures` e são publicados como `sirej-configuracao` com classificador `testfixtures` (dependência de teste: `<classifier>testfixtures</classifier>`), junto com `VerificadorCofreChavesSimulado`.

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
| `autos` | Download por membros, marca d'água, acesso fora da sessão (`PERMITIDO`, `MEDIANTE_AUTORIZACAO_COORDENADOR` ou `PROIBIDO`; D-26) | RN29 |
| `presenca`, `mandato` | Cancelamento de presença, métricas de perda de mandato | RN30, RN36 |
| `documentos` | Formatos, tamanho, documentos obtidos de ofício | RN33 |
| `retencao` | Anos de retenção | — |
| `administracao` | Aprovadores por tipo de mudança, antecedência mínima da vigência, papéis sensíveis, vigência máxima de papel | RN38, RN39, RN42 |
| `temporalidade` | Classes documentais, prazos de guarda e destinação | RN44 |

## Regras de consistência (validação obrigatória)

Implementadas em `configuracao` (`RegrasDeConsistencia`); cada uma tem teste de falha e teste positivo em `RegrasDeConsistenciaTest`.

1. `distribuicao.falhaFechada` só aceita `true`. Qualquer outro valor impede a subida (invariante 2).
2. `resultados` não vazio, códigos únicos, ao menos um com `alteraPenalidade: true`.
3. `turmas.membrosPorTurma` ímpar e igual ao número de segmentos quando `umPorSegmento: true`.
4. `votacao.votosMinimos` ≤ `turmas.membrosPorTurma`; `excecaoMaioriaSimples.minimo` ≥ 2.
5. `composicao.posicoesPorJunta` tem ao menos `membrosPorTurma` posições, cada segmento declarado em `segmentos`, sem letra repetida (D-53).
6. Todo prazo tem `dias` > 0 (ou `sessoes` > 0) ou origem explícita (`IMPRESSO_NA`); `null` só para metas sem prazo legal (chaves `meta*`); antecedências de `prazos.alertas` > 0 (D-53).
7. `designacao.modo = SIGILOSO` exige KMS configurado na instalação (verificação de conectividade no startup, por `VerificadorCofreChaves`; sem implementação registrada, conta como ausente; D-49).
8. Não existe chave para voto de qualidade, peso de voto ou desempate. Se aparecer no YAML, a validação falha (invariante 3).
9. Cada tipo em `administracao.aprovadores` tem ao menos um papel, e nenhum deles é `ADMIN` para `REGIMENTO` nem para `IMPORTACAO` (quem parametriza não aprova a própria área; RN38; D-50).
10. Toda classe de `temporalidade` tem guarda total (corrente + intermediária) ≥ `retencao.anos`; `temporalidade.eliminacaoFisica` só aceita `false` enquanto a D-19 estiver aberta.
11. Nenhum resultado configurável representa provimento parcial (invariante 4; RN26). Código ou descrição com `PARCIAL`, ou equivalente sem acento e em qualquer caixa, reprova (D-52). Hoje o resultado só tem `codigo`; o esquema recusa qualquer outra chave.

## Valores PROVISÓRIOS

Itens marcados `PROVISÓRIO` em `sp.yaml` são escolhas da equipe onde a norma é omissa ou ambígua. Cada um tem uma entrada em `16-duvidas-abertas.md`. Implemente o parâmetro e use o valor provisório; não fixe o valor no código.

## Como adicionar um parâmetro

1. Confirme que a regra realmente varia entre órgãos ou é ambígua na norma.
2. Adicione ao esquema, ao `record` tipado, ao `sp.yaml` (com comentário citando a norma) e a esta tabela.
3. Adicione regra de consistência, se houver combinação inválida.
4. Teste ao menos dois valores do parâmetro.
