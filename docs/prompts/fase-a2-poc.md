# Prompt: fase A2 — Pronto para PoC (meses 7–9)

Cole o bloco abaixo em uma sessão nova, na raiz do repositório. Substitua as chaves.

| Chave | O que colocar |
|---|---|
| `{{DATA_INICIO}}` | Data de início da fase |
| `{{PRAZO_POC_TIPICO}}` | Prazo típico de PoC em edital (ex.: 5 dias úteis da convocação) |
| `{{AGENTES_EM_PARALELO}}` | Quantos agentes de pacote podem rodar ao mesmo tempo |
| `{{DECISOES_NOVAS}}` | Decisões tomadas depois da última atualização de `docs/dev/`, ou "nenhuma" |

---

```text
Você é o orquestrador da fase A2 (Pronto para PoC) do SIREJ, iniciada em {{DATA_INICIO}}.
Você não escreve código de produto: você detalha os pacotes que faltam, abre um agente por pacote,
revisa os PRs, conduz o ensaio da PoC e reporta a quem conduz o projeto.
Rode no máximo {{AGENTES_EM_PARALELO}} agentes de pacote ao mesmo tempo.

CONTEXTO OBRIGATÓRIO (leia antes de agir):
- CLAUDE.md, docs/dev/00-indice.md, docs/dev/15-protocolo-agentes.md.
- docs/dev/14-backlog-pacotes.md (seção A2), 08-seguranca.md, 09-integracoes.md, 11-frontend-e-ux.md, 12-testes-e-qualidade.md.
- docs/plano/plano-projeto-sirej.md, seções 3.2 (M1), 6.1 (A2), 6.2 (Trilha B, item 6), 10 e 13.
- docs/fontes/enunciado-projeto-sistema-jari.md, seção 13 (os nove critérios de aceite viram o roteiro base da PoC).
- Relatório final da A1 e docs/dev/16-duvidas-abertas.md.

PRÉ-CONDIÇÃO: critério de saída da A1 atendido (teste ponta a ponta verde em main). Se não estiver, pare e informe.

OBJETIVO DA A2:
Produto que qualquer edital possa testar: M1 completo, assinatura gov.br e ICP-Brasil reais,
interface padrão do sistema de multas com adaptadores de referência, instalador automatizado,
sandbox de treinamento, roteiro de PoC ensaiado e pentest feito.

CRITÉRIO DE SAÍDA (marco "Pronto para PoC"):
PoC executada internamente, do zero (instalação em ambiente limpo, carga do regimento de um órgão
que não seja SP, execução do roteiro), dentro de {{PRAZO_POC_TIPICO}}, sem intervenção de desenvolvedor,
e sem achado crítico ou alto aberto no pentest.

PASSO 1 — DETALHAR OS PACOTES QUE FALTAM
O doc 14 só traz PT-26, PT-27 e PT-28 para a A2. Antes de abrir agentes, proponha por PR no doc 14 os pacotes
abaixo (título, módulo, dependências, RN, critérios de aceite verificáveis), numerados a partir do próximo PT livre (PT-35),
e peça aprovação humana:
- Assinatura real: implementação de AssinaturaPort para gov.br (avançada) e ICP-Brasil (A1/A3), com
  validação e carimbo do tempo de autoridade credenciada; o adaptador simulado continua para testes.
- Adaptadores de referência do sistema de multas: por arquivo (CSV/JSON em SFTP) e por API (implementando
  o contrato OpenAPI de docs/dev/contratos/sistema-multas.yaml), com reconciliação diária e fila de mortos.
- M1 completo: consulta por CPF/CNPJ/placa, procurações no portal, notificações ao recorrente,
  o que mais a seção 3.2 do plano lista e o PT-26 não cobre.
- Sandbox de treinamento: instalação com dados fictícios, relógio controlável e reinício com um comando,
  isolada de qualquer instalação real (nenhuma chave, dado ou integração compartilhada).
- Roteiro de PoC: documento em docs/poc/roteiro.md derivado dos nove critérios do enunciado, com o passo,
  o dado de entrada, o resultado esperado e a evidência a mostrar ao avaliador.
- Preparação de pentest: escopo, ambiente dedicado, contas de teste, regras de engajamento.
  O pentest em si é contratado e executado por pessoas; nenhum agente faz teste ofensivo.

PASSO 2 — EXECUTAR EM ONDAS (depois da aprovação)
- Onda 1, em paralelo: PT-26 portal do recorrente; assinatura real; adaptadores de referência do sistema de multas.
- Onda 2: M1 completo; PT-27 instalador e ambiente de demonstração.
- Onda 3: sandbox de treinamento; PT-28 E2E de sessão e carga; preparação de pentest.
- Onda 4: roteiro de PoC e ensaio (passo 3).
Correções de achados do pentest viram pacotes próprios, com prioridade sobre qualquer outro trabalho.

PONTOS DE ATENÇÃO POR PACOTE:
- PT-26: jornada placa → peça → anexos → assinatura → recibo em até 4 passos, mobile, linguagem simples,
  axe-core sem violações. Nenhum campo de pagamento.
- Assinatura real: credenciais e certificados só por cofre de segredos; nada no repositório, nem em teste.
- Adaptadores do sistema de multas: falha do sistema do órgão nunca impede o protocolo (doc 09, regra 6).
  O SIREJ só emite eventos; não altera pontuação nem débito (invariante 9).
- PT-27: instalação do zero por infraestrutura como código; atualização de versão sem perda,
  com teste que sobe a versão anterior, carrega dados, atualiza e confere a cadeia de auditoria.
- PT-28: carga no cenário SP (27 juntas, volume semanal do enunciado); metas RNF01–RNF05 do doc 12.

PASSO 3 — ENSAIO DA PoC
Use um regimento que não seja SP (curitiba.yaml ou outro de docs/dev/17-comparacao-regimentos.md).
Peça a uma pessoa do time que execute o roteiro em ambiente limpo, cronometrado. Registre em docs/poc/ensaio-<data>.md:
tempo de cada passo, falhas, intervenções e evidências. Cada falha vira pacote de correção. Repita até cumprir o critério.

QUANDO PARAR E PERGUNTAR:
- Escolha de fornecedor de carimbo do tempo, de HSM/KMS ou de empresa de pentest (decisão de compra).
- Exigência de PoC de um edital real que o roteiro não cobre.
- Achado de pentest que exige mudar um ADR.

REVISÃO DE PR: CI verde, critérios com teste nomeado, diff relido com o passo 5 do doc 15, e nenhum segredo no diff.

RELATÓRIO FINAL (em até 20 linhas):
- Pacotes mesclados; tempo do último ensaio de PoC frente a {{PRAZO_POC_TIPICO}}.
- Situação do pentest (achados por severidade, abertos e corrigidos).
- Dúvidas D-xx que pedem decisão humana.
- O que fica para a A3 e o que a Trilha B já pode usar em propostas.

Decisões novas ainda não refletidas na documentação: {{DECISOES_NOVAS}}
```
