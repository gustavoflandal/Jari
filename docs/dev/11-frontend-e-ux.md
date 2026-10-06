# 11 — Frontend e UX

Duas aplicações com públicos opostos. Não compartilhe telas entre elas; compartilhe só tipos, schemas e componentes base.

## Portal do Recorrente (`frontend/apps/portal`)

Público: cidadão, majoritariamente no celular, muitas vezes em rede ruim, sem conhecimento jurídico.

- Mobile-first; WCAG 2.1 AA e eMAG; GovBR-DS (`@govbr-ds/core`) ou o design system do órgão.
- **Linguagem simples.** O cidadão precisa entender a diferença entre defesa e recurso sem advogado. Nada de "rejeição administrativa" sem explicação: todo resultado vem com motivo e próximo passo.
- O assistente começa pela **placa ou pelo AIT** e mostra só as peças cabíveis naquela fase, com o prazo restante em destaque (RN03, RN15, RN16).
- Protocolo em menos de 5 minutos: no máximo 4 passos (escolher peça → escrever/anexar → revisar → assinar).
- Recibo imediato com número, data/hora e hash; disponível em PDF.
- Linha do tempo do processo como tela principal após o protocolo.
- No modo sigiloso, nunca mostrar junta, posição ou julgador antes da sessão. Mostrar "distribuído, julgamento previsto na semana de dd/mm".
- Nenhuma tela pede pagamento ou comprovante de pagamento (invariante 7).
- Uploads: validação de formato e tamanho no cliente e no servidor; progresso visível; retomada em rede ruim.
- SSR (Next.js) só se o órgão aceitar Node em produção; senão SPA com páginas informativas pré-renderizadas (ADR-0009).

## Back-office da JARI (`frontend/apps/backoffice`)

Público: membros, secretaria, presidentes, coordenação. **Linha de base real do membro: acessar o sistema com login e senha e redigir dois parágrafos num editor de texto** (edital de credenciamento). O ambiente de relatoria precisa parecer um editor de texto com apoio, não um sistema processual.

- O caminho **relatar → votar → próximo** é o mais curto do sistema: no máximo um clique entre o fim de um processo e o início do próximo.
- Tela do relator em duas colunas: autos à esquerda (PDF.js, imagens do AIT com zoom), editor à direita (TipTap com salvamento automático, modelos, formatação restrita).
- Dossiê de contexto no topo dos autos: veículo, autuação, penalidade, histórico de defesas e recursos do mesmo veículo, tempestividade calculada, ocorrências.
- Checklist de três eixos (RN13) e seleção obrigatória de resultado do rol e dispositivo normativo, sem jargão de workflow.
- Sem atalhos obrigatórios, sem fluxos multitela, sem modais em cascata.
- Membro não tem botão de download; visualizador com marca d'água (RN29).
- Painel do presidente: roteiro da reunião (passos obrigatórios bloqueiam avanço), presenças, botão de abertura, acompanhamento dos votos, proclamação.
- Secretaria: fila de triagem, exigências, backlog com alertas de prazo, pauta e ata.

## Padrões técnicos

- TypeScript `strict`, React 19, Vite, TanStack Query para estado de servidor, Zod para validação (schemas gerados do OpenAPI em `packages/tipos`).
- Nenhuma regra de negócio só no frontend; validação no cliente é conveniência.
- Textos de interface em arquivos de mensagens (`pt-BR`), revisados pelo glossário (doc 02).
- Testes: Vitest para componentes, Playwright para E2E (inclui roteiro completo de uma sessão), axe-core em pipeline.
