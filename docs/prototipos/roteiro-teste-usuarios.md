# Roteiro de teste com usuários — protótipos da fase A0

Material para as pessoas do time que vão conduzir os testes. O agente de UX só preparou o material e consolida os achados recebidos (ver `achados.md`). As hipóteses H1–H17 estão no `README.md`.

## 1. Formato

- Teste moderado, individual, de 30 a 40 minutos por participante. Presencial ou por vídeo com compartilhamento de tela.
- Um moderador conduz; um observador anota (de preferência alguém que não desenhou as telas).
- Protocolo "pensar em voz alta": o participante fala o que está vendo e pensando. O moderador não ajuda nem explica a tela durante a tarefa; se o participante travar por mais de 2 minutos, registre como "não concluída sem ajuda" e só então ajude.
- Abra cada protótipo com `?moderador=1` e use **Zerar** antes de cada tarefa. Anote os números da barra (tempo, cliques, marcos) ao fim de cada tarefa.
- Recarregue a página entre participantes (o estado não persiste).

## 2. Perfis a recrutar

| Perfil | Quantidade | Critérios | Onde testar | Protótipo |
|---|---|---|---|---|
| **Recorrente leigo no celular** | 6 a 8 | Tem CNH ou veículo; já recebeu multa ou notificação; **sem formação jurídica**; nunca trabalhou em órgão de trânsito ou JARI. Variar idade (incluir 2 pessoas com 55+), escolaridade e familiaridade com celular. Pelo menos 1 pessoa que usa leitor de tela ou ampliação, se possível. | No **celular do próprio participante** (ou um aparelho Android de entrada), em rede móvel | Portal |
| **Membro com perfil "login e dois parágrafos"** | 4 a 5 | Atual ou ex-membro de JARI, ou pessoa com o perfil do edital de credenciamento: usa e-mail e editor de texto, não usa sistema processual. Misturar os três segmentos (comunidade, entidade executiva, sociedade civil). | Computador comum (notebook, tela 13–15") | Relator |
| **Presidente de junta** | 3 | Atual ou ex-presidente/vice de JARI. | Computador | Presidente |
| **Secretaria** | 2 a 3 | Servidor de secretaria de JARI. Participa como **observador** do teste do presidente e depois responde às perguntas da seção 5.4. | Computador | Presidente (observação) |

Exclusão: quem participou do desenho destas telas; quem trabalha no desenvolvimento do SIREJ.

Cinco participantes por perfil costumam revelar a maior parte dos problemas graves; o recorrente tem mais vagas porque o público é mais variado.

## 3. Cuidados LGPD e ética

- **Nenhum dado real.** Todos os dados das telas são fictícios. Peça ao participante para **não digitar** placa, CPF, nome ou documentos reais e para usar o botão "Usar arquivo de exemplo" no lugar de anexar fotos do próprio celular. Se ele digitar algo real por engano, não anote o conteúdo.
- **Identificação por código.** Cada participante recebe um código (`R01`, `M01`, `P01`, `S01`). O nome fica só no termo de consentimento, guardado separado das anotações. Nas anotações e no `achados.md`, só o código e o perfil.
- **Gravação só com consentimento** e só de tela e áudio, sem rosto. Se o teste for no celular do participante, grave a tela **somente** do navegador com o protótipo, depois de conferir que notificações estão silenciadas (modo "não perturbe"), para não captar mensagens pessoais. Na dúvida, não grave: anote.
- **Guarda.** Gravações e anotações brutas ficam em pasta de acesso restrito do projeto, **fora deste repositório**, e são apagadas depois da consolidação no `achados.md` (sugestão: até 30 dias após o último teste; confirmar com o encarregado de dados da empresa). O repositório recebe só achados anonimizados.
- **Membros, presidentes e servidores** podem citar casos reais durante a conversa: não anote nome de recorrente, placa, número de processo ou de auto real. Anote só a ideia ("membro disse que costuma consultar o histórico do veículo").
- Participação voluntária; o participante pode parar a qualquer momento sem justificar. Se houver gratificação, ela não depende do desempenho.

### Termo de consentimento (ler e colher assinatura ou aceite gravado)

> Convidamos você a participar de um teste de telas de um sistema para recursos de multas de trânsito, ainda em desenvolvimento. Vamos observar como você usa as telas; **quem está sendo testado é o sistema, não você**. Não existe resposta certa ou errada.
>
> As telas são um protótipo, sem valor jurídico, com dados inventados. Por favor, não digite seus dados pessoais reais.
>
> O teste leva até 40 minutos. Com sua autorização, vamos gravar a tela e o áudio (sem imagem do seu rosto) só para revisar as anotações. A gravação fica com a equipe do projeto, não é divulgada e será apagada depois da análise. Nas anotações você será identificado só por um código.
>
> Sua participação é voluntária. Você pode parar a qualquer momento, sem dar motivo, e pedir que suas anotações sejam descartadas.
>
> ☐ Concordo em participar. ☐ Autorizo a gravação de tela e áudio. ☐ Não autorizo a gravação.
>
> Nome: ________________________ Data: ___/___/______ Assinatura: ________________________
>
> Contato da equipe para dúvidas ou para retirar o consentimento: ________________________

## 4. Métricas

| Métrica | Como medir | Meta | Hipótese |
|---|---|---|---|
| **Tempo até o protocolo** | Barra do moderador: marco "Protocolo concluído" (a contagem começa ao Zerar, com a tela inicial aberta) | **< 5 min** para ≥ 80% dos recorrentes | H1 |
| Etapas para protocolar | Contar telas de 1 a 4 percorridas; anotar voltas atrás | ≤ 4 etapas, sem voltar | H1 |
| Sucesso da tarefa | Concluiu sem ajuda / concluiu com ajuda / não concluiu | ≥ 80% sem ajuda | todas |
| Pedido certo escolhido | Na tarefa R2, escolheu defesa da autuação ou indicação de condutor (e não procurou "recurso à JARI") | ≥ 80% | H2 |
| **Cliques entre processos (relator)** | Cliques entre a assinatura de um processo e o início do trabalho no próximo; pela construção da tela deve ser **1** (o próprio "Assinar voto e abrir o próximo"). Anote se o participante clicou em algo a mais para "achar" o próximo processo. | **1 clique**; 0 participantes procurando o próximo | H9 |
| Cliques por processo (relator) | Diferença de cliques entre marcos "Assinado …" consecutivos | Referência, sem meta (comparar com o sistema atual) | H8 |
| Tempo por processo (relator) | Diferença de tempo entre marcos "Assinado …" consecutivos | Referência; anotar o tempo gasto lendo autos vs. escrevendo | H8, H10 |
| Erros de validação | Quantas vezes apareceu "Falta completar…" | Referência | H11 |
| Facilidade percebida (SEQ) | Após cada tarefa: "De 1 (muito difícil) a 7 (muito fácil), quão fácil foi?" | Média ≥ 5,5 | todas |
| Compreensão | Perguntas pós-tarefa marcadas com **[C]**: certo / parcialmente / errado | ≥ 80% certo | H2–H7, H13, H15, H16 |

## 5. Tarefas e perguntas

Leia o cenário em voz alta. Entregue também por escrito, se o participante preferir.

### 5.1 Recorrente (portal, no celular)

**Aquecimento (2 min):** "Você já recebeu multa? O que fez? Já tentou recorrer?"

| Tarefa | Cenário (ler para o participante) | Critério de sucesso |
|---|---|---|
| R1 | "Você recebeu em casa a notificação de uma multa por avançar o sinal vermelho com o carro de placa **ABC1D23**. Você acha que a multa está errada e quer recorrer. Faça isso pelo celular." | Chega ao recibo do **recurso à JARI** do auto PROTO-000123, com um texto e uma prova de exemplo, **em menos de 5 minutos**, sem ajuda. |
| R2 | "Agora outro caso, com o mesmo carro: você recebeu um aviso de que foi autuado por estacionar em local proibido, mas quem estava com o carro era seu filho. O que você pode fazer?" (Não precisa concluir o protocolo.) | Encontra o auto PROTO-000456 e escolhe **"Indicar quem estava dirigindo"**; sabe dizer o prazo (13/10/2026). |
| R3 | "Você recorreu de outra multa há algumas semanas. Veja em que pé está." | Abre "Meus processos" → processo 2026.000777 e diz, com suas palavras, que está distribuído e vai ser julgado na semana de 12/10. |
| R4 | "Você recorreu da multa da moto **XYZ9W87**. Veja o resultado e o que pode fazer agora." | Diz que a multa foi mantida, dá o motivo e diz que pode recorrer ao CETRAN até 30/10. |

Perguntas depois das tarefas:

1. **[C]** "Qual é a diferença entre a defesa da autuação e o recurso à JARI?" (H2)
2. **[C]** "Até quando você podia recorrer da multa do sinal vermelho?" (H3: 22/10/2026 ou "16 dias")
3. **[C]** "Você precisa pagar a multa para recorrer?" (H4)
4. "No processo 2026.000777, você gostaria de saber quem vai julgar? Por quê? O que entendeu da explicação?" (H5)
5. **[C]** "Se você precisar provar que recorreu, o que mostraria? Onde está o número?" (H7)
6. "Alguma palavra que você não entendeu?" (anotar cada palavra)
7. "Teve algum momento em que ficou em dúvida se o pedido tinha ido?"
8. "Na etapa de anexar, com a rede ruim simulada, o que achou que estava acontecendo?" (só se o moderador ligou "Simular rede ruim")

### 5.2 Membro (relator, no computador)

**Explicação inicial (máx. 2 min, sempre a mesma):** "Esta é a tela que você usaria no dia da sessão, depois que o presidente abre a reunião. Seus processos aparecem em fila. Você lê o caso à esquerda e escreve à direita."

| Tarefa | Cenário | Critério de sucesso |
|---|---|---|
| M1 | "Relate e vote o primeiro processo da sua fila. Decida como achar melhor." | Assina o voto do 2026.000777 com checklist completo, resultado, dispositivo e texto ≥ 2 frases, sem ajuda. |
| M2 | "Continue com o próximo processo." | Chega ao 2026.000781 **sem procurar** o próximo (1 clique) e o assina. |
| M3 | "Continue." (processo 2026.000790, fora do prazo) | Percebe a etiqueta "Fora do prazo" e decide **por conta própria** (qualquer resultado é aceito; anotar se achou que o sistema tinha decidido). |
| M4 | "Agora você é revisor. Vote." (2026.000802) | Lê o voto do relator e acompanha ou diverge corretamente. |
| M5 (opcional) | "Suponha que o recorrente do processo atual é seu primo. O que você faz?" | Encontra "Declarar impedimento ou suspeição" e assina com o motivo certo. |

Perguntas:

1. "Pareceu um editor de texto ou um sistema? O que lembrou?" (H8)
2. "O resumo do caso no topo respondeu o que você precisava? O que faltou? Em algum momento abriu um documento só para conferir algo que poderia estar no resumo?" (H10)
3. "As três verificações ajudaram ou atrapalharam? Você mudaria algo?" (H11)
4. "Conseguiu ler bem os documentos com a marca d'água? Sentiu falta de baixar ou imprimir? Para quê?" (H12)
5. **[C]** "Como revisor, o que acontece se você escolher 'Acompanho o relator'?" (H13)
6. **[C]** "Depois de assinar, dá para mudar o voto? E se tiver um erro de digitação?" (resposta esperada: não; retificação registrada)
7. "Quantos processos você costuma relatar numa sessão? Esta tela aguentaria esse volume?"

### 5.3 Presidente (painel, no computador)

**Explicação inicial (máx. 1 min):** "Você é o presidente da 14ª Junta e vai conduzir a sessão de hoje. Faça o que faria numa sessão real."

| Tarefa | Cenário | Critério de sucesso |
|---|---|---|
| P1 | "Chegaram você e o membro B. Abra a sessão." | Tenta abrir, vê o bloqueio e explica que falta quórum (3 membros de 3 segmentos). |
| P2 | "Agora chegaram também C, D e E. O membro F avisou que não vem. Abra a sessão." | Marca as presenças e abre; diz o que foi revelado. |
| P3 | "Siga o roteiro até o julgamento." | Pula a ata anterior (opcional), registra as turmas e libera os processos, sem tentar escolher turmas ou substitutos. Explica por que E aparece substituindo F. |
| P4 | "Os votos vão chegando (o moderador clica 'Simular a próxima rodada' três vezes). Proclame o que for possível." | Proclama os processos com resultado; diante do 2026.000803, **não procura desempate** e retira de pauta. |
| P5 | "Encerre a sessão." | Encerra e diz o que acontece com os processos não julgados. |

Perguntas:

1. **[C]** "Antes de abrir, você sabia quais processos eram da sua junta? Por quê?" (H15)
2. **[C]** "Quem decidiu a divisão das turmas e quem substituiu o membro F?" (H15)
3. **[C]** "O que aconteceu com o processo 2026.000803? Existe alguma forma de desempatar?" (H16; resposta esperada: não)
4. "Algum passo do roteiro da sua junta está faltando ou sobrando? A ordem bate com a prática?"
5. "Você confia no que a tela diz sobre o selo e o sorteio? O que aumentaria essa confiança?"

### 5.4 Secretaria (observação do teste do presidente)

1. "Com o que viu, você conseguiria redigir a ata? O que faltou?" (H17)
2. "Que informação você precisaria receber do sistema no fim da sessão?"
3. "Os processos retirados de pauta ficaram claros? Você saberia o que fazer com eles?"

## 6. Depois de cada sessão

1. Moderador e observador conversam por 10 minutos e preenchem uma linha por achado no `achados.md` (ou num rascunho fora do repositório, para enviar depois).
2. Registre as métricas da seção 4 na tabela de métricas do `achados.md`.
3. Confira que nenhuma anotação tem dado pessoal real.
