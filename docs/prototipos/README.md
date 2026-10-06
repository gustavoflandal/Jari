# Protótipos navegáveis — fase A0, Frente 3

Protótipos em HTML estático das três telas críticas do doc 11 (`docs/dev/11-frontend-e-ux.md`), feitos para **teste com usuários**. Não são código de produto: nada aqui vai para `frontend/`, e as regras de negócio do produto continuam valendo só pelo `docs/dev/`.

Todas as telas mostram a faixa **"PROTÓTIPO — SEM VALOR JURÍDICO"**. Todos os dados (placas, autos, nomes, CPFs, números de processo) são fictícios. Nada sai do navegador: não há servidor, formulário enviado nem gravação.

## Como abrir

1. Abra `docs/prototipos/index.html` direto no navegador (duplo clique; funciona em `file://`). Não precisa de build, servidor nem internet: o CSS (`assets/proto.css`) e o JS (`assets/proto.js`) estão no repositório.
2. No celular: copie a pasta `docs/prototipos/` para o aparelho e abra `index.html`, ou sirva a pasta numa rede local (`npx http-server docs/prototipos`) e acesse pelo IP do computador.
3. **Modo moderador:** acrescente `?moderador=1` ao endereço de qualquer tela (ex.: `portal/index.html?moderador=1`). Aparece uma barra no rodapé com cronômetro, contagem de cliques e marcos ("Busca feita", "Protocolo concluído", "Assinado 2026.000777", "Passo 1 concluído"). O botão **Zerar** reinicia a contagem antes de cada tarefa.
4. "Hoje", para efeito de prazos, é fixo em **06/10/2026**, para que todos os participantes vejam os mesmos prazos.

## Mapa das telas

| Protótipo | Arquivo | Telas/estados | Perfil de teste |
|---|---|---|---|
| Portal do recorrente | `portal/index.html` (+ `portal.js`) | Início (placa ou auto) → autuações do veículo → **1. Escolher** o pedido cabível, com prazo em destaque → **2. Escrever e anexar** (validação de formato e tamanho, progresso, simulação de rede ruim com retomada) → **3. Revisar** → **4. Assinar** → Recibo (número, data/hora, SHA-256 de cada documento, código do recibo, salvar em PDF) → Linha do tempo → Meus processos | Recorrente leigo, no celular |
| Ambiente do relator | `relator/index.html` (+ `relator.js`) | Fila dos processos da sessão; resumo do caso (dossiê) no topo dos autos; visualizador com abas, zoom e marca d'água nominal; editor com formatação restrita, modelos e salvamento automático; checklist de três eixos; resultado do rol; dispositivo normativo; **Assinar voto e abrir o próximo**; tela de revisor (acompanhar ou divergir); declaração de impedimento ou suspeição; tela de fim | Membro de junta |
| Painel do presidente | `presidente/index.html` (+ `presidente.js`) | Roteiro da reunião (7 passos do `sp.yaml`, obrigatórios bloqueiam avanço); presenças e quórum; abertura com revelação e conferência do selo; turmas sorteadas pelo rodízio; pauta revelada com relator, revisor, 3º membro e substituições; acompanhamento dos votos e proclamação; proposições; encerramento | Presidente de junta; secretaria como observadora |

### Dados de teste

- **Portal:** placa `ABC1D23` (três autuações: multa aplicada → recurso à JARI; aviso de autuação → defesa ou indicação de condutor; recurso em andamento → juntada) e placa `XYZ9W87` (recurso julgado, penalidade mantida → recurso ao CETRAN). Também aceita o número do auto (`PROTO-000123`, `PROTO-000456`, `PROTO-000789`, `PROTO-000321`). Em "Meus processos": um processo **distribuído** (modo sigiloso) e um **julgado**.
- **Relator:** quatro processos na fila do "Membro Fictício B": três como relator (um deles fora do prazo pelo cálculo do sistema) e um como revisor, com o voto do relator já assinado.
- **Presidente:** "Membro Fictício A" preside; seis posições A–F, duas por segmento. Doze processos selados para a junta. Para ver substituição e retirada de pauta, deixe a posição F ausente; o processo `2026.000803` termina **sem maioria** (três resultados diferentes).

## O que cada protótipo quer validar (hipóteses)

Cada hipótese tem tarefa e critério no `roteiro-teste-usuarios.md`; os resultados vão para `achados.md`.

**Portal do recorrente**

| Id | Hipótese |
|---|---|
| H1 | Um cidadão leigo protocola um recurso à JARI pelo celular, sem ajuda, em **menos de 5 minutos** e em no máximo 4 etapas. |
| H2 | Mostrando só os pedidos cabíveis na fase, com "quem decide" e o "por que não aparecem outras opções", o cidadão entende a diferença entre defesa da autuação e recurso à JARI e escolhe o pedido certo. |
| H3 | O prazo restante em destaque ("Faltam 16 dias", "Prazo até 22/10/2026") é percebido e lembrado depois da tarefa. |
| H4 | O cidadão entende que **não precisa pagar** a multa para recorrer. |
| H5 | "Distribuído. Julgamento previsto na semana de 12/10", sem junta nem julgador, é suficiente: o cidadão não sente falta de saber quem vai julgar e entende por quê. |
| H6 | Diante de um resultado (penalidade mantida), o cidadão entende o motivo e o próximo passo possível (CETRAN, com prazo). |
| H7 | O recibo é entendido como comprovante: o cidadão encontra o número e consegue salvá-lo. |

**Ambiente do relator**

| Id | Hipótese |
|---|---|
| H8 | Um membro com o perfil "login e dois parágrafos" relata e vota um processo sem treinamento além de uma explicação de 2 minutos. |
| H9 | **Um clique** entre assinar um processo e abrir o próximo é suficiente, e o aviso verde basta para o membro saber que o voto anterior foi assinado. |
| H10 | O resumo do caso no topo dos autos responde às perguntas mais comuns sem abrir documentos. |
| H11 | O checklist de três eixos é entendido como apoio, não como burocracia. |
| H12 | A ausência de download e a marca d'água não atrapalham a leitura dos autos. |
| H13 | O revisor entende a diferença entre acompanhar e divergir, e o que cada um exige. |

**Painel do presidente**

| Id | Hipótese |
|---|---|
| H14 | O presidente conduz o roteiro sem ajuda e entende por que o botão de abertura está bloqueado sem quórum. |
| H15 | O presidente entende que não escolhe distribuição, turmas nem substitutos, e confia no que o sistema mostra (selo conferido, rodízio). |
| H16 | O presidente acompanha os votos, proclama e, diante de "sem maioria", não procura um desempate. |
| H17 | A secretaria, observando, encontra o que precisa para a ata (presenças, turmas, resultados, retiradas de pauta). |

## Como as invariantes aparecem nas telas

| Invariante / regra | Onde |
|---|---|
| 1 e RN20 — sigilo da designação | Portal: processo distribuído mostra só "julgamento previsto na semana de dd/mm". Presidente: antes da abertura, "distribuição selada" e o compromisso público do lote; nem a quantidade de processos da junta aparece. Relator: a barra diz que os processos foram revelados na abertura. |
| 2 e RN24 — sem distribuição manual | Nenhuma tela tem botão para distribuir, escolher posição, turma ou substituto. Turmas e substituições aparecem como resultado do sistema. |
| 3 e RN21 — sem voto de qualidade | Apuração só por maioria de votos de igual peso; "sem maioria" bloqueia a proclamação e diz que não há desempate. Não existe papel de desempate no painel. |
| 4 e RN26 — rol fechado | Relator e revisor escolhem só entre os cinco resultados do `sp.yaml`; não há provimento parcial nem texto livre no resultado. |
| 5 e RN11 — nada é apagado | O cidadão é avisado de que o pedido enviado não pode ser apagado; o voto assinado não é editável (erro material → retificação). |
| 7, RN05 e RN17 — sem pagamento | Nenhum campo pede pagamento ou comprovante. O portal diz que recorrer é gratuito e que quem já pagou pode recorrer. |
| 10 — nenhuma decisão automática | A tempestividade calculada aparece como informação ("a decisão é sua"); nenhum resultado vem pré-marcado; o presidente proclama, o sistema só apura. |
| RN03, RN15, RN16 — peças cabíveis | O portal só oferece os pedidos cabíveis na fase do auto e explica quem decide. |
| RN13 — checklist | Três eixos obrigatórios antes de assinar o voto do relator. |
| RN22 — quórum | Abertura bloqueada sem 3 membros de 3 segmentos. |
| RN29 — autos | Sem botão de download ou impressão para o membro; marca d'água com nome, CPF mascarado, data e hora. |
| RN33 — documentos de ofício | O portal avisa que CRLV e documento de identidade não são pedidos. |
| RN34, RN35 | Declaração de impedimento com motivo do rol; substituição do ausente pelo mesmo segmento da outra turma. |

## Decisões provisórias (onde o `docs/dev/` não decide)

Seguindo o CLAUDE.md, adotamos a opção mais restritiva. Nenhuma delas vira regra de produto: são escolhas de protótipo para o teste. As que dependem de decisão do órgão estão registradas no `docs/dev/16-duvidas-abertas.md` (D-37 e D-38).

1. **Peças por fase do auto.** Defesa da autuação e indicação de condutor só na fase de aviso da autuação; recurso à JARI só depois da notificação da penalidade; recurso ao CETRAN só depois do resultado publicado; juntada só com recurso em andamento. **Conversão em advertência e pedido de efeito suspensivo não são oferecidos** no portal, porque o doc 04 não diz em que fase cabem nem se o cidadão os pede pelo portal.
2. **Formato do número de protocolo** (`2026.001001`) é ilustrativo; os docs 05 e 10 não fixam formato.
3. **Respostas do checklist RN13.** O doc define os três eixos, não as respostas. O protótipo usa "Verifiquei, sem ressalvas" / "Verifiquei, com ressalvas (explico no voto)", ambas obrigatórias.
4. **Três votos, nenhuma maioria (D-37).** O doc 07, seção 7, diz que "empate é impossível com número ímpar", mas com cinco resultados possíveis três votos podem divergir em três resultados diferentes (ex.: manutenção, cancelamento, rejeição). O protótipo **bloqueia a proclamação**, diz que não há desempate e oferece apenas "Retirar de pauta": o processo volta à pauta seguinte da mesma posição (por analogia com a exceção de 2 votos divergentes, que "volta à pauta").
5. **Resultado apurado e não proclamado até o encerramento:** o processo é retirado de pauta e volta na próxima sessão (mesma lógica de "sessão encerrada sem julgar", doc 04).
6. **Antes da abertura, o presidente não vê nem a quantidade de processos da junta.** O doc proíbe mostrar junta, posição e sequência; a contagem por junta também revela a designação agregada, então foi omitida.
7. **Abertura como transação única** (doc 07, seção 4): presenças, quórum, revelação, partição e mapeamento acontecem no clique de abertura. Os passos III e IV do roteiro aparecem como registro das turmas e liberação dos processos aos membros; o presidente não escolhe nada neles.
8. **Voto do revisor que acompanha** registra por remissão o resultado, a fundamentação e o dispositivo do relator; divergir exige resultado diferente, fundamentação e dispositivo (doc 07, seção 7.2).
9. **Depois do julgamento**, o portal mostra a junta e informa que os nomes constam da decisão, porque `designacao.revelarNomesAposJulgamento: true` (provisório, D-09).
10. **Exceção de 2 votos é inalcançável na abertura em SP (D-38).** Com 6 posições (2 por segmento), quórum de 3 segmentos e substituição pelo mesmo segmento, um assento só fica vago se os dois membros de um segmento faltarem, e aí não há quórum. A exceção (`votacao.excecaoMaioriaSimples`) só apareceria com impedimento declarado durante a sessão. O parâmetro é mantido; em SP ele só se aplica a fatos ocorridos durante a sessão. O protótipo implementa a apuração, mas o cenário padrão não a exercita.
11. **Assinatura do cidadão** é simulada com um clique e uma declaração; o protótipo não imita a tela do gov.br e não pede senha.
12. **Hashes** do recibo são SHA-256 reais, mas calculados sobre o texto do pedido e sobre nome, tamanho e data do arquivo anexado (o protótipo não lê o conteúdo do arquivo). O código de verificação do recibo é ilustrativo.
13. **Presidente sempre presente** no protótipo ("você"); a abertura pelo vice na ausência do presidente não foi prototipada.
14. **Impedimento declarado** tira o processo da fila do membro; a recomposição da turma é só anunciada.

## Acessibilidade (WCAG 2.1 AA)

Cuidados aplicados: `lang="pt-BR"`, link "Pular para o conteúdo", títulos hierárquicos, todo campo com rótulo, grupos de opções com `fieldset`/`legend`, mensagens de erro ligadas ao campo (`aria-describedby`, `aria-invalid`) e resumo de erros com links, avisos em `aria-live`, foco movido para o título a cada troca de tela, foco visível em todos os elementos interativos, alvos de toque de 44 px, abas do visualizador com setas do teclado, contraste mínimo de 4.5:1 nos textos, nenhum atalho obrigatório, nenhuma janela modal, respeito a `prefers-reduced-motion`. A marca d'água é imagem de fundo e não interfere com leitores de tela.

### Resultado do axe-core

Rodado em 06/10/2026 com axe-core 4.14.0 em Chromium (Playwright 1.56.1), regras `wcag2a`, `wcag2aa`, `wcag21a`, `wcag21aa`. O script `verificacao/axe-prototipos.js` percorre as três jornadas e roda o axe em cada estado: portal em viewport de celular (390×844), relator e presidente em desktop (1366×900).

- **25 estados de tela verificados, 0 violações.**
- Primeira rodada: a marca d'água em texto falhou em `color-contrast` (proposital, é decorativa). Foi trocada por imagem de fundo SVG, que também evita seleção do texto.
- A mesma rodada achou um problema de usabilidade: a barra do moderador cobria o botão "Acompanhar meu processo" no celular. A barra foi para o rodapé, com espaço reservado.
- Checagens extras do script: sem rolagem horizontal a 390 px (portal e relator); nenhum botão de download/impressão no relator; processo distribuído sem as palavras junta/posição/relator/julgador; abertura recusada sem quórum; menções a pagamento só para dizer que ele não é exigido; nenhum erro de JavaScript.
- O axe não substitui teste manual: falta conferir com leitor de tela (NVDA/TalkBack) e navegação só por teclado com usuários. Ver roteiro.

Para repetir (numa pasta temporária, fora do repositório):

```sh
npm i axe-core@4 playwright@1.56.1
PLAYWRIGHT_BROWSERS_PATH=/opt/pw-browsers node <repo>/docs/prototipos/verificacao/axe-prototipos.js <repo>/docs/prototipos
```

## Limitações conhecidas

- O visual é **inspirado** no GovBR-DS (cores, botões arredondados, mensagens), sem a biblioteca oficial nem a fonte Rawline; o produto usará `@govbr-ds/core` ou o design system do órgão.
- O visualizador dos autos simula páginas em HTML; o produto usará PDF.js.
- O editor usa `contenteditable`; o produto usará TipTap.
- Os estados não persistem: recarregar a página reinicia a jornada (de propósito, para cada participante começar do zero).
- Secretaria (triagem, pauta, ata) e console administrativo estão fora deste pacote.
