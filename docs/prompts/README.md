# Prompts de execução por fase

Prompts prontos para colar em um agente (Claude Code ou equivalente) e executar cada fase do plano (`docs/plano/plano-projeto-sirej.md`, seção 6). Os prompts não repetem regras: eles mandam o agente ler `CLAUDE.md` e `docs/dev/`, que continuam sendo a fonte de verdade.

## Dois papéis

| Papel | Quem é | Prompt |
|---|---|---|
| **Orquestrador da fase** | Um agente por fase. Planeja as ondas, abre um agente por pacote, revisa PRs, verifica o critério de saída e reporta a quem conduz o projeto. Não escreve código de produto. | `fase-*.md`, `trilha-*.md` |
| **Agente de pacote** | Um agente por PT. Implementa um pacote de trabalho do doc 14 e entrega um PR. | `agente-pt.md` |

O orquestrador usa `agente-pt.md` como modelo para cada agente que abre, preenchendo as chaves `{{...}}`.

## Prompts

| Arquivo | Fase | Quando usar |
|---|---|---|
| [agente-pt.md](agente-pt.md) | Qualquer PT | Modelo para o agente que implementa um pacote |
| [fase-a0-fundacao.md](fase-a0-fundacao.md) | A0 Fundação (meses 1–2) | Início do produto |
| [fase-a1-nucleo.md](fase-a1-nucleo.md) | A1 Núcleo demonstrável (meses 3–6) | Depois do critério de saída da A0 |
| [fase-a2-poc.md](fase-a2-poc.md) | A2 Pronto para PoC (meses 7–9) | Depois do critério de saída da A1 |
| [fase-a3-complementares.md](fase-a3-complementares.md) | A3 Módulos complementares (meses 10–18) | Depois da A2, um módulo por vez |
| [trilha-b-proposta.md](trilha-b-proposta.md) | B Proposta por edital | Quando um edital for publicado |
| [trilha-c-contrato.md](trilha-c-contrato.md) | C0–C4 Execução contratual | Depois da assinatura de um contrato, uma etapa por vez |

## Como usar

1. Abra uma sessão nova do agente na raiz do repositório, com `main` atualizado.
2. Cole o prompt da fase inteiro. Substitua as chaves `{{...}}` que aparecem no topo (datas, órgão, edital).
3. Deixe o orquestrador trabalhar em ondas. Ele para e pergunta só quando a documentação não responde e a decisão muda o produto (ver "Quando parar").
4. Ao fim da fase, o orquestrador entrega um relatório com o critério de saída verificado. Só então abra a próxima fase.

## Regras comuns a todos os prompts

- Idioma: português do Brasil.
- As 10 invariantes do `CLAUDE.md` valem acima de qualquer prazo. Um PR que enfraquece uma delas não é mesclado, mesmo verde.
- Lacuna de norma vira linha no `docs/dev/16-duvidas-abertas.md` com opção provisória restritiva, e o trabalho continua.
- Nenhum agente mescla o próprio PR. Quem mescla é uma pessoa do time (ou o orquestrador, se o time autorizar por escrito).
- Nenhum agente envia nada para fora do repositório (órgão, e-mail, portal de compras) sem pedido explícito de uma pessoa.

## Manutenção

Quando o doc 14 ganhar pacotes novos (por exemplo, o detalhamento da A2 e da A3), atualize o prompt da fase correspondente na mesma mudança.
