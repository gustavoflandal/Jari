# Achados dos testes com usuários — protótipos da fase A0

Registro dos achados dos testes descritos em `roteiro-teste-usuarios.md`. **Só dados anonimizados**: participante por código (`R01`, `M01`, `P01`, `S01`), nunca nome, placa, CPF, número de processo ou de auto reais.

## Como registrar

1. Uma linha por achado **por participante**. Se dois participantes tiveram o mesmo problema, são duas linhas (a consolidação junta depois).
2. Descreva o que a pessoa **fez ou disse**, não a sua interpretação: "Procurou o botão de pagar antes de recorrer" é melhor que "não entendeu a gratuidade". Citações curtas entre aspas ajudam.
3. Severidade (escala de Nielsen):
   - **0**: não é problema de usabilidade (preferência, elogio).
   - **1**: cosmético; corrigir se sobrar tempo.
   - **2**: menor; atrasa ou incomoda, mas a pessoa conclui.
   - **3**: maior; a pessoa conclui só com ajuda ou com erro; corrigir antes da A1.
   - **4**: catástrofe; a pessoa não conclui, ou o protótipo sugere algo que **viola uma invariante** do CLAUDE.md (ex.: participante acha que precisa pagar; presidente acha que pode escolher turma; cidadão deduz quem vai julgar). Corrigir obrigatoriamente.
4. Recomendação: o que mudar na tela (ou "nenhuma"). Se a recomendação exigir mudar uma regra de negócio, escreva "**regra**" e não proponha a regra: ela vai para `docs/dev/16-duvidas-abertas.md` por quem conduz o projeto.
5. Decisão: deixe vazio. É preenchida na consolidação (`aceito`, `aceito com ajuste`, `recusado: motivo`, `virou dúvida D-xx`).

Telas: `portal/inicio`, `portal/autuacoes`, `portal/passo1` … `portal/passo4`, `portal/recibo`, `portal/linha-do-tempo`, `portal/meus-processos`, `relator/dossie`, `relator/autos`, `relator/editor`, `relator/checklist`, `relator/resultado`, `relator/revisor`, `relator/impedimento`, `presidente/abertura`, `presidente/turmas`, `presidente/pauta`, `presidente/julgamento`, `presidente/encerramento`.

## Achados

| Id | Data | Perfil | Participante | Tela | Hipótese | Achado | Severidade | Recomendação | Decisão |
|---|---|---|---|---|---|---|---|---|---|
| | | | | | | | | | |

## Métricas por participante

| Participante | Perfil | Tarefa | Concluiu (sem ajuda / com ajuda / não) | Tempo | Cliques | SEQ (1–7) | Compreensão [C] (certo / parcial / errado) | Observação |
|---|---|---|---|---|---|---|---|---|
| | | | | | | | | |

## Como consolidar

Quando os testes de um perfil terminarem, quem consolida (o agente de UX, ao receber as anotações, ou uma pessoa do time):

1. **Agrupe** linhas que descrevem o mesmo problema e dê a cada grupo um id `A-01`, `A-02`… Mantenha as linhas originais e anote nelas o grupo (ex.: "→ A-03").
2. **Some** a frequência (quantos participantes do perfil tiveram o problema) e use a **maior** severidade do grupo.
3. **Calcule** as métricas: mediana e pior caso do tempo até o protocolo; % de recorrentes abaixo de 5 minutos; % de sucesso sem ajuda por tarefa; média de SEQ; % de respostas certas nas perguntas [C]; cliques entre processos no relator (meta: 1).
4. **Confronte com as hipóteses** H1–H17 do `README.md`: para cada uma, "confirmada", "refutada" ou "inconclusiva", com o número de participantes.
5. **Priorize**: severidade 4 primeiro (sempre corrigir), depois 3 com maior frequência. Qualquer achado que toque uma invariante vai para revisão de quem conduz o projeto, mesmo com severidade baixa.
6. **Registre a decisão** na coluna "Decisão" e, se a decisão mudar uma tela do produto, cite o pacote de trabalho (`PT-xx`) que vai implementar.
7. **Preencha** o resumo abaixo e apague as anotações brutas fora do repositório no prazo combinado (ver roteiro, seção 3).

## Resumo consolidado

_Ainda não há testes realizados._

| Hipótese | Resultado | Evidência (participantes) |
|---|---|---|
| H1 | | |
| H2 | | |
| H3 | | |
| H4 | | |
| H5 | | |
| H6 | | |
| H7 | | |
| H8 | | |
| H9 | | |
| H10 | | |
| H11 | | |
| H12 | | |
| H13 | | |
| H14 | | |
| H15 | | |
| H16 | | |
| H17 | | |

### Problemas consolidados

| Grupo | Problema | Frequência | Severidade | Recomendação | Decisão |
|---|---|---|---|---|---|
| | | | | | |
