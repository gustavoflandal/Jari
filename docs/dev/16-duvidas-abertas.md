# 16 — Dúvidas abertas

Lacunas de norma ou de decisão, com a opção provisória adotada. Toda opção provisória é **parâmetro** (doc 06), nunca valor fixo no código. Quando o órgão ou a empresa decidir, atualize a linha, o `sp.yaml` e o teste.

Formato: id · origem · dúvida · opção provisória · por quê.

| Id | Origem | Dúvida | Opção provisória | Por quê |
|---|---|---|---|---|
| D-01 | Regimento SP arts. 19 e 29, XIII | Membro pode acessar autos fora do dia/horário da reunião? | `autos.acessoMembrosForaDaSessao: PERMITIDO` (só para processos já revelados) | Antes da revelação o sigilo já impede; restringir mais inviabiliza relatar ~38 processos numa manhã |
| D-02 | Regimento SP art. 17 §§1–3 | Presidente e vice precisam ficar em turmas diferentes? | `turmas.presidenteEViceEmTurmasDiferentes: false` | Norma não exige; parâmetro disponível |
| D-03 | Regimento SP art. 22 | Conexo que chega em semana posterior, com o anterior ainda pendente, vai à mesma posição? | `distribuicao.conexaoComPendentes: MESMA_POSICAO` | Preserva "mesmo membro e mesma turma" |
| D-04 | Regimento SP art. 27, VII; enunciado RN14 | Votação sequencial (relator → revisor → 3º) ou paralela? | `votacao.ordem: SEQUENCIAL` | Prática descrita pela página institucional de SP |
| D-05 | Regimento SP arts. 6º §3 e 17 §1 x enunciado 5.4 | Decisão com 2 votos é válida? | Permitida como exceção com presidente ou vice presente (`votacao.excecaoMaioriaSimples`) | Texto do regimento vigente prevalece sobre o enunciado |
| D-06 | Regimento SP art. 12, VI | Prazo de relatoria | `prazos.relatoria: { sessoes: 1 }` | Norma omissa; métrica apenas evidencia |
| D-07 | Regimento SP art. 15 | Roteiro de reunião obrigatório? | Só os passos III e IV (+ abertura, julgamento, encerramento) bloqueiam | Caput diz "poderão obedecer" |
| D-08 | Regimento SP art. 30 | O que é o "planejamento mensal de distribuição interna"? | Não implementar | Sem definição |
| D-09 | Enunciado critério 2 | Nomes dos julgadores aparecem após o julgamento? | `designacao.revelarNomesAposJulgamento: true` | Responsabilização nominal; política do órgão pode mudar |
| D-10 | Regimento SP art. 29, IV | Processos de relator ausente | Voltam à pauta seguinte da mesma posição | Texto da Secretaria "recolocar na pauta" |
| D-11 | Curitiba | Segmentos, posições e uso dos 3 suplentes por junta | A confirmar na Lei 15.154/2017 | Texto não analisado |
| D-12 | Curitiba | Provedor de identidade do cidadão (gov.br, identidade digital PR ou ambos) | Ambos, por configuração | Hoje Curitiba usa plataformas do Paraná |
| D-13 | Prazos SP | Prazo de exigência e de informação do agente | 10 dias (provisório) | Norma omissa |

## Como registrar uma nova dúvida

Adicione uma linha com o próximo id, o PT que a encontrou e a opção adotada. Prefira sempre a opção que preserva sigilo, auditoria e falha fechada.
