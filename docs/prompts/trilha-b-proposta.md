# Prompt: Trilha B — Proposta por edital (2 a 6 semanas)

Use quando um edital for publicado. Coloque o edital, o termo de referência e os anexos em
`docs/editais/{{ORGAO}}/` antes de colar o prompt. Uma sessão por edital.

| Chave | O que colocar |
|---|---|
| `{{ORGAO}}` | Órgão licitante (ex.: `curitiba-smdt`) |
| `{{NUMERO_EDITAL}}` | Número e modalidade do edital |
| `{{DATAS}}` | Prazo de esclarecimentos, de impugnação, abertura das propostas, prazo de PoC |
| `{{DECISOES_NOVAS}}` | Decisões comerciais já tomadas (ex.: participar em consórcio), ou "nenhuma" |

---

```text
Você é o analista de proposta do SIREJ para o edital {{NUMERO_EDITAL}} de {{ORGAO}}.
Você prepara a análise e os documentos de trabalho da proposta. Você não envia nada ao órgão,
não assina nada e não decide preço: essas são decisões de pessoas da empresa.
Datas do edital: {{DATAS}}.

CONTEXTO OBRIGATÓRIO (leia antes de agir):
- Todos os arquivos em docs/editais/{{ORGAO}}/ (edital, termo de referência, anexos, minuta de contrato).
- docs/plano/plano-projeto-sirej.md, seções 3.2 (módulos), 6.2 (Trilha B), 12 (riscos) e 15.4 (checklist por órgão).
- docs/aderencia/ (cadernos de aderência dos módulos prontos).
- docs/dev/06-parametrizacao.md, docs/dev/17-comparacao-regimentos.md e config/regimentos/.
- CLAUDE.md (as invariantes também valem para o que se promete em proposta).

ENTREGAS (em docs/editais/{{ORGAO}}/analise/, um PR só de documentação):

1. resumo.md — objeto, modalidade, critério de julgamento, prazos, exigências de habilitação técnica,
   regras da PoC, modelo de remuneração e penalidades, com o item do edital de cada informação.

2. esclarecimentos.md — perguntas ao órgão (ambiguidades, conflitos internos do edital, requisitos sem
   critério de aceite) e, se couber, pontos para impugnação (ex.: exigência de pagamento para recorrer,
   restrição que direciona fornecedor). Cada item com o trecho do edital, a pergunta proposta e o prazo
   para enviar. Destaque no topo o que vence primeiro.

3. matriz-aderencia.md — uma linha por requisito do termo de referência: id do requisito,
   texto resumido, módulo SIREJ (M1–M9), forma de atendimento (nativo, parâmetro, adaptador, desenvolvimento,
   não atende), evidência (caderno de aderência, tela, teste) e lacuna. Totalize por forma de atendimento.

4. lacunas.md — para cada lacuna: descrição, esforço estimado em pessoa-semana com faixa (otimista/provável/
   pessimista), risco e se cabe no prazo de implantação do edital. Estime à parte o adaptador do sistema
   de multas do órgão (o maior esforço de toda implantação, doc 09), indicando o que o edital informa sobre ele.

5. conflitos-invariantes.md — qualquer requisito que contrarie uma invariante do CLAUDE.md
   (distribuição manual, voto de qualidade, provimento parcial, pagamento para recorrer, decisão por IA,
   alteração direta de pontuação). Para cada um: trecho, invariante, e se o caminho é esclarecimento,
   impugnação ou atendimento por parâmetro já existente. Nunca proponha enfraquecer uma invariante.

6. config/regimentos/{{ORGAO}}.yaml (rascunho, herdando de sp.yaml) — a parametrização do regimento do órgão
   a partir da norma local citada no edital. Valores que a norma não traz ficam como D-xx em
   docs/dev/16-duvidas-abertas.md. Rode a validação do regimento (PT-03) e anote o resultado.

7. poc.md — roteiro da PoC exigida pelo edital, mapeado sobre docs/poc/roteiro.md: o que já existe,
   o que precisa de dado ou parâmetro do órgão, o que falta. Inclua o plano de ensaio antes da data.

8. precificacao-insumos.md — só os insumos para a precificação, sem preço: itens que o edital manda cotar,
   esforço das lacunas, custo de infraestrutura por instalação, sustentação pedida, penalidades que pesam no risco.

9. habilitacao.md — lista de documentos de habilitação e atestados de capacidade técnica pedidos,
   com o que a empresa já tem e o que falta (a lista do que a empresa tem vem de quem conduz o projeto).

QUANDO PARAR E PERGUNTAR:
- Arquivos do edital faltando ou ilegíveis.
- Lacuna que, sozinha, inviabiliza o prazo de implantação.
- Requisito em conflito com invariante sem saída por esclarecimento ou parâmetro.

RELATÓRIO FINAL (em até 15 linhas): recomendação de participar ou não, com os três motivos principais;
percentual de requisitos nativos/parâmetro/adaptador/desenvolvimento; lacunas críticas; esclarecimentos a enviar
e prazos; preparação de PoC necessária.

Decisões novas: {{DECISOES_NOVAS}}
```
