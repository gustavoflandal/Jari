# 15 — Protocolo dos agentes de desenvolvimento

Como um agente (ou pessoa) recebe, executa e entrega um pacote de trabalho. O objetivo é que vários agentes trabalhem em paralelo sem pisar uns nos outros e sem enfraquecer as invariantes.

## 1. Receber

O orquestrador entrega ao agente:
- o id do PT (`PT-xx`) do doc 14;
- o branch base (`main` atualizado);
- qualquer decisão nova ainda não refletida na documentação.

O agente confirma que **todas as dependências** do PT estão mescladas. Se não estiverem, para e informa.

## 2. Ler (nesta ordem)

1. `CLAUDE.md` (invariantes).
2. Doc 14, linha do PT.
3. Docs 02 (glossário) e 03 (arquitetura).
4. Os docs indicados pelas RN do PT (04, 06, 07, 08, 09...).
5. `16-duvidas-abertas.md`, para saber o que já foi decidido provisoriamente.

## 3. Executar

- Trabalhe **somente** no(s) módulo(s) do PT. Precisa mudar API de outro módulo? Pare e registre como dúvida para o orquestrador; não altere.
- Escreva primeiro os testes dos critérios de aceite e das RN (devem falhar), depois o código.
- Use a configuração (`RegimentoVigente`) para todo valor que varia entre órgãos. Nunca fixe número de prazo, quórum, resultado ou composição.
- Para sistema externo, use a porta e o adaptador simulado. Não chame serviço real em teste.
- Não crie atalho "temporário" que contorne sigilo, auditoria, imutabilidade ou falha fechada, nem para depuração.
- Mantenha o PR pequeno. Se o PT ficou grande demais, entregue em partes, cada uma verde.

## 4. Lacunas e conflitos

- A documentação não responde → registre em `16-duvidas-abertas.md` (id `D-xx`, PT, pergunta, opção adotada, por quê) e siga com a **opção mais restritiva** (mais sigilo, mais auditoria, falha fechada, nada automático).
- Documentação e fonte (`docs/fontes/`) divergem → vale `docs/dev/`; se parecer erro, registre a dúvida.
- Duas regras parecem conflitar → não escolha em silêncio; registre e implemente como parâmetro, se possível.

## 5. Entregar

Antes de abrir o PR:
1. `./mvnw verify` e testes do frontend verdes localmente.
2. Releia o diff procurando: valor fixo que deveria ser parâmetro; log com dado sensível; endpoint sem autorização; acesso a tabela de outro módulo; `UPDATE`/`DELETE` em tabela imutável; qualquer caminho que revele ou altere designação.
3. Atualize `docs/dev/` se mudou comportamento, parâmetro, endpoint, evento ou tabela.

O PR contém:
- Título em Conventional Commits com o PT: `feat(prazos): motor de prazos com calendário (PT-05)`.
- Descrição: Antes / Depois, RN implementadas, como testar, dúvidas registradas.
- Lista de critérios de aceite do PT, cada um com o teste que o cobre.

## 6. Relatório ao orquestrador

Ao terminar, o agente responde em até 10 linhas:
- PT, link do PR, situação do CI;
- RN cobertas e testes correspondentes;
- dúvidas registradas (`D-xx`) e a opção adotada;
- o que ficou fora do PT, se algo.

## 7. Paralelismo

- Um agente por PT. Dois agentes nunca editam o mesmo módulo ao mesmo tempo.
- Arquivos compartilhados (`docs/dev/*`, `config/regimentos/*`, `pom.xml` raiz): mudanças pequenas, rebase antes de abrir o PR.
- Migrações Flyway usam timestamp no nome para evitar colisão.
