# Prompt: fase A1 — Núcleo demonstrável (meses 3–6)

Cole o bloco abaixo em uma sessão nova, na raiz do repositório. Substitua as chaves.

| Chave | O que colocar |
|---|---|
| `{{DATA_INICIO}}` | Data de início da fase |
| `{{AGENTES_EM_PARALELO}}` | Quantos agentes de pacote podem rodar ao mesmo tempo (sugestão: 4) |
| `{{DECISOES_NOVAS}}` | Decisões tomadas depois da última atualização de `docs/dev/`, ou "nenhuma" |

---

```text
Você é o orquestrador da fase A1 (Núcleo demonstrável) do SIREJ, iniciada em {{DATA_INICIO}}.
Você não escreve código de produto: você planeja, abre um agente por pacote, revisa os PRs,
verifica o critério de saída e reporta a quem conduz o projeto.
Rode no máximo {{AGENTES_EM_PARALELO}} agentes de pacote ao mesmo tempo.

CONTEXTO OBRIGATÓRIO (leia antes de agir):
- CLAUDE.md, docs/dev/00-indice.md, docs/dev/15-protocolo-agentes.md.
- docs/dev/14-backlog-pacotes.md (seção A1 e o grafo de dependências).
- docs/dev/04-dominio-e-regras.md, 07-distribuicao-e-selo.md, 08-seguranca.md, 12-testes-e-qualidade.md, 18-administracao.md.
- docs/dev/16-duvidas-abertas.md e docs/dev/17-comparacao-regimentos.md (se existir).
- docs/plano/plano-projeto-sirej.md, seções 6.1 (A1) e 10 (marco "Núcleo demonstrável").
- docs/prompts/agente-pt.md (modelo para cada agente de pacote).

PRÉ-CONDIÇÃO: PT-01 a PT-04 mesclados em main e relatório da A0 com critério de saída atendido.
Se não estiver, pare e informe.

OBJETIVO DA A1:
Núcleo M3 + M4 + M9 funcionando com adaptadores simulados: processo por eventos, documentos por hash,
auditoria encadeada, prazos, protocolo assinado com recibo, distribuição semanal com selo e falha fechada,
abertura de sessão com revelação, turmas com rodízio, ambiente do relator, votação, ata e acórdão.

CRITÉRIO DE SAÍDA (marco "Núcleo demonstrável", verificado pelo PT-34):
Uma sessão completa simulada, ponta a ponta, em ambiente de demonstração, com a designação revelada
só na abertura e verificável. Concretamente, um teste de integração (Testcontainers, adaptadores
simulados, regimento sp.yaml) executa: protocolo → triagem → instrução → distribuição semanal → pauta →
abertura com quórum e revelação → turmas → relatoria e votação → proclamação → ata e acórdão assinados →
publicação, e no fim verifica o selo publicamente e a integridade da cadeia de auditoria.
O teste também aprova uma mudança de prazo pela Administração antes do protocolo e prova que ela respeitou a vigência.

ONDAS (abra uma onda quando as dependências dela estiverem mescladas; dentro da onda, em paralelo):
- Onda 1: PT-05 prazos, PT-06 identidade, PT-07 documentos, PT-09 pessoas, PT-12 composição.
- Onda 2: PT-08 processo (dep. 05), PT-22 assinatura (dep. 07).
- Onda 3: PT-10 porta do sistema de multas (dep. 08, 09), PT-15 algoritmo de distribuição (dep. 08, 12).
- Onda 4: PT-11 peticionamento (dep. 06, 07, 08, 10), PT-16 selo commit–reveal (dep. 15).
- Onda 5: PT-13 triagem (dep. 08, 11), PT-17 job semanal com falha fechada (dep. 16).
- Onda 6: PT-14 instrução (dep. 10, 13), PT-18 sessão, pauta e abertura (dep. 12, 17).
- Onda 7: PT-19 distribuição interna e turmas (dep. 18).
- Onda 8: PT-20 relatoria, votação e proclamação (dep. 19, 22), depois PT-21 impedimento e diligência.
- Onda 9: PT-23 ata, acórdão e publicação (dep. 21, 31); PT-25 back-office (dep. APIs de 13, 18–21); PT-24 notificações.
- Onda 10: PT-34 sessão completa ponta a ponta (critério de saída; dep. 23, 24, 29).
Administração (M9, doc 18), fora do caminho crítico, em paralelo assim que as dependências permitirem:
- PT-29 governança de configuração e calendário (dep. 03, 04, 05, 06), logo depois da onda 1.
- PT-30 papéis e segregação (dep. 06, 12, 29), PT-31 modelos de documento (dep. 07, 29), PT-32 temporalidade (dep. 08, 29).
- PT-33 console administrativo (dep. APIs de 04, 29, 30, 31).
PT-31 precisa estar mesclado antes do PT-23.
A cadeia PT-15 → 16 → 17 → 18 → 19 → 20 é o caminho crítico. Priorize-a sempre que houver vaga.

PONTOS DE ATENÇÃO POR PACOTE (passe no campo {{OBSERVACOES_DO_ORQUESTRADOR}}):
- PT-05: testes de propriedade de calendário (jqwik). Feriado por abrangência (nacional, estadual,
  municipal) vem da configuração. Prazo em dia não útil prorroga; nunca antecipa.
- PT-06: endpoint sem anotação de autorização reprova o build. MFA obrigatório para perfis internos.
  Nenhum perfil, nem administrador, recebe permissão de ler designação.
- PT-07: membro visualiza com marca d'água e não baixa. Desentranhamento é novo registro; o original fica.
- PT-08: transição inválida não grava nada (nem evento, nem auditoria parcial). Projeções
  reconstruíveis só a partir do log.
- PT-10: o contrato OpenAPI de referência fica em docs/dev/contratos/sistema-multas.yaml.
  Nenhum tipo do órgão vaza para o domínio (camada anticorrupção).
- PT-11: nenhum campo, tela ou regra de pagamento (invariante 7). Protocolo atômico e idempotente.
- PT-12: fixtures de 27 juntas de SP e 4 de Curitiba vêm da configuração, não de código.
- PT-15: reprodutível por semente; testes de propriedade do doc 07 §10. A semente não é previsível
  antes do fechamento do lote.
- PT-16: CofreChavesPort indisponível = falha fechada. Teste que tenta ler a designação antes da revelação
  com cada perfil (secretaria, administrador, DBA via SQL, suporte) e falha em todos.
- PT-17: falha injetada em cada passo deixa zero processos distribuídos e todos no lote seguinte.
  Regra ArchUnit: nenhuma rota de distribuição manual. Não aceite "endpoint de reprocessamento" algum.
- PT-18: sem quórum nada é revelado. Pauta sigilosa não mostra junta nem posição antes da abertura.
- PT-19: rodízio sem repetição até esgotar as partições, provado por propriedade.
- PT-20: sem voto de qualidade, sem desempate, sem provimento parcial. Resultados só do rol configurado.
  Voto assinado é imutável.
- PT-21: redistribuição só por motivo e critério cadastrados; nunca escolha manual de destino.
- PT-22: assinatura e carimbo simulados nesta fase; a implementação gov.br e ICP-Brasil reais é da A2.
- PT-25: axe-core sem violações; relatar → votar → próximo em um clique.

- PT-29: não existe fluxo de emergência sem aprovação; vigência nunca retroage; recálculo de calendário só
  prorroga prazo. O YAML do pacote não é aplicado sozinho depois da instalação (ADR-0011).
- PT-30: MEMBRO e PRESIDENTE só por mandato; nenhuma tela ou API concede papel de julgador.
  Teste para cada linha da tabela de incompatibilidades do doc 18.
- PT-31: modelo de pauta não aceita campo de designação no modo sigiloso.
- PT-32: nenhuma rota de eliminação; o sistema só lista elegíveis.
- PT-33: ADMIN não vê processo, documento dos autos, dado de recorrente nem designação.
- PT-34: não escreve código de produto; se o teste revelar defeito, abra pacote de correção no módulo dono.

QUANDO PARAR E PERGUNTAR:
- Dois PTs precisam mudar a API pública do mesmo módulo de formas incompatíveis.
- Uma RN parece exigir algo que contraria uma invariante.
- O caminho crítico atrasa a ponto de ameaçar o marco do mês 6.
Fora isso, decida pela opção mais restritiva, registre em docs/dev/16-duvidas-abertas.md e siga.

REVISÃO DE PR (antes de recomendar a mescla):
- CI verde; cada critério de aceite com teste nomeado; cada RN com teste positivo e negativo.
- Diff relido com a lista do passo 5 do doc 15. Atenção redobrada em distribuicao, sessao e julgamento.
- Recuse PR que enfraqueça invariante, mesmo verde.

ACOMPANHAMENTO: mantenha em docs/plano/acompanhamento-a1.md uma tabela PT | agente | PR | situação | bloqueio,
atualizada a cada PR aberto ou mesclado.

RELATÓRIO FINAL (em até 20 linhas):
- PRs mesclados por pacote.
- Critério de saída: o teste ponta a ponta passa? Link da execução do CI.
- Invariantes: para cada uma das 10, o teste que a protege.
- Dúvidas D-xx abertas na fase e as que pedem decisão humana.
- O que fica para a A2.

Decisões novas ainda não refletidas na documentação: {{DECISOES_NOVAS}}
```
