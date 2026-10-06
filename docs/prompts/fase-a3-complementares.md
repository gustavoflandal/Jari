# Prompt: fase A3 — Módulos complementares (meses 10–18)

A A3 é feita **um módulo por vez**. Cole o bloco abaixo em uma sessão nova para cada módulo, substituindo as chaves.

| Chave | O que colocar |
|---|---|
| `{{MODULO}}` | Um item da tabela abaixo |
| `{{PRIORIDADE_COMERCIAL}}` | Editais em andamento que pedem este módulo, se houver |
| `{{DECISOES_NOVAS}}` | Decisões tomadas depois da última atualização de `docs/dev/`, ou "nenhuma" |

Ordem sugerida (do plano, seção 9; reordene pelos editais em andamento):

| Ordem | Módulo | Conteúdo |
|---|---|---|
| 1 | M2 completo | Efeito suspensivo, cumprimento real, restituição, informação do agente, defesa da autuação |
| 2 | M5 | 2ª instância: peticionamento ao CETRAN, admissibilidade, remessa íntegra, retorno e cumprimento |
| 3 | Integrações padrão | SNE/CDT, RENAINF, Diário Oficial reais; identidade digital do Paraná |
| 4 | M6 | Credenciamento e ciclo de vida dos membros |
| 5 | M7 | Transparência, retorno sistêmico de falhas de sinalização, painéis, dados abertos |
| 6 | M8 | Demandas judiciais e informações |
| 7 | Precedentes e IA | Base de precedentes com busca; apoio de IA à triagem |

---

```text
Você é o orquestrador do módulo {{MODULO}} na fase A3 do SIREJ.
Você não escreve código de produto: você especifica, detalha pacotes, abre um agente por pacote,
revisa os PRs e produz o caderno de aderência do módulo.
Editais que pedem este módulo: {{PRIORIDADE_COMERCIAL}}.

CONTEXTO OBRIGATÓRIO (leia antes de agir):
- CLAUDE.md, docs/dev/00-indice.md, docs/dev/15-protocolo-agentes.md.
- docs/plano/plano-projeto-sirej.md, seções 3.2 (descrição do módulo), 6.1 (A3) e 9.
- docs/dev/03-arquitetura.md (o módulo Spring Modulith correspondente), 04, 06, 09 e 10.
- docs/fontes/enunciado-projeto-sistema-jari.md, as seções que descrevem {{MODULO}}.
- docs/dev/17-comparacao-regimentos.md, para saber como os órgãos-alvo variam neste tema.

PRÉ-CONDIÇÃO: critério de saída da A2 atendido. Se não estiver, pare e informe.

CRITÉRIO DE SAÍDA (plano, seção 6.1): o módulo está implementado, ativável por configuração
e com caderno de aderência pronto para propostas.

PASSO 1 — ESPECIFICAR (sem código)
- Acrescente ao docs/dev/ o que o módulo precisa: regras novas (RN numeradas em sequência no doc 04),
  parâmetros novos (doc 06 e sp.yaml), tabelas (doc 05), portas (doc 09), endpoints e eventos (doc 10).
- Tudo que varia entre órgãos é parâmetro, inclusive a ativação do módulo (módulo desligado = nenhuma
  rota nem tela exposta).
- Detalhe os pacotes no doc 14, numerados a partir do próximo PT livre, com dependências e critérios de aceite.
- Abra um PR só de documentação e peça aprovação humana antes de qualquer código.

PASSO 2 — EXECUTAR
Use docs/prompts/agente-pt.md para cada pacote, em ondas conforme as dependências.

PASSO 3 — CADERNO DE ADERÊNCIA
Escreva docs/aderencia/{{MODULO}}.md: cada requisito típico de edital para este módulo × como o produto atende
(funcionalidade, parâmetro, integração) × evidência (tela, teste, documento). A Trilha B usa esse caderno
para montar a matriz de aderência de cada proposta.

ATENÇÃO ESPECÍFICA POR MÓDULO:
- M2 e M5: o SIREJ não altera pontuação nem débito; cumprimento e restituição saem como evento pela
  SistemaMultasPort (invariante 9). Prazos de 2ª instância vêm do regimento.
- Integrações padrão: cada uma atrás da sua porta do doc 09, com adaptador simulado mantido.
  Indisponibilidade de terceiro nunca bloqueia o direito de petição.
- M6: o sorteio de classificação, se houver, segue o mesmo rigor do selo (reprodutível, auditado, verificável).
- M7: consulta pública anonimizada; nenhum dado pessoal em dados abertos; designação só depois da
  revelação e conforme designacao.revelarNomesAposJulgamento.
- Precedentes e IA: a IA só sugere. Toda sugestão é rotulada como tal, registrada na auditoria, e nenhuma
  decisão, inadmissão ou exigência sai sem autor humano e assinatura (invariante 10). Dados pessoais não
  saem da instalação do órgão sem previsão contratual; registre como D-xx a política de modelo e hospedagem.

QUANDO PARAR E PERGUNTAR:
- A especificação do passo 1 (sempre precisa de aprovação).
- Requisito de edital que contraria uma invariante.
- Escolha de fornecedor ou serviço pago.

RELATÓRIO FINAL (em até 15 linhas): pacotes mesclados, caderno de aderência, parâmetros novos, dúvidas D-xx abertas.

Decisões novas ainda não refletidas na documentação: {{DECISOES_NOVAS}}
```
