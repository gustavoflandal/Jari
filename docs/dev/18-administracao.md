# 18 — Administração (M9)

Especificação do módulo de Administração, que faz parte do núcleo e entra na fase A1. O enunciado (seção 5.9) pede quatro coisas e não as detalha:
- perfis e papéis por escopo;
- parametrização sem deploy de prazos, feriados, calendários de sessão, modelos de documento e regras de distribuição;
- auditoria imutável;
- temporalidade e arquivamento.

Nenhum edital analisado até agora especifica a Administração. Por isso este documento é a especificação do produto. As regras de negócio estão no doc 04 (RN38 a RN45) e as decisões provisórias no doc 16 (D-14 a D-19).

## 1. Princípio

A Administração é o lugar mais perigoso do sistema. Quem parametriza pode, em tese, mudar prazo, quórum, regra de distribuição ou quem tem acesso a quê. Por isso a ameaça nº 1 do doc 08 (direcionamento por um interno) também vale aqui. A Administração obedece a cinco regras:

1. **Ninguém age sozinho.** Toda mudança que afeta rito, acesso ou documento oficial é proposta por uma pessoa e aprovada por outra (RN38).
2. **Nada retroage.** Mudança tem vigência futura e nunca altera ato já praticado nem prazo já comunicado ao cidadão (RN39, RN41).
3. **Invariante não é parâmetro.** As 10 invariantes do `CLAUDE.md` não aparecem na tela e não podem ser alteradas por nenhum caminho (RN40).
4. **Administrador não vê processo.** O console nunca mostra conteúdo de processo, dado pessoal de recorrente ou designação (RN45).
5. **Tudo deixa rastro.** Proposta, aprovação, rejeição, publicação, concessão e revogação de papel e acesso ao console geram auditoria na mesma transação.

## 2. Funções

| # | Função | Módulo de código | Quem propõe | Quem aprova | PT |
|---|---|---|---|---|---|
| F1 | Parametrização do regimento (prazos, peças, composição, distribuição, turmas, sessão, votação, resultados, publicidade, módulos ativos) | `configuracao` | `ADMIN` | `COORDENADOR` (config. `administracao.aprovadores.REGIMENTO`) | PT-29 |
| F2 | Calendário: feriados, pontos facultativos e suspensões de expediente | `configuracao` | `ADMIN` ou `SECRETARIA` | `COORDENADOR` | PT-29 |
| F3 | Importação e exportação da configuração (YAML) | `configuracao` | `ADMIN` | `COORDENADOR` | PT-29 |
| F4 | Papéis por escopo e segregação de funções | `identidade` | `ADMIN` | Outro `ADMIN` para papéis sensíveis | PT-30 |
| F5 | Modelos de documento (ata, acórdão, certidão, notificação, exigência, recibo) | `configuracao` | `ADMIN` ou `SECRETARIA` | `COORDENADOR` | PT-31 |
| F6 | Temporalidade e destinação de documentos | `processo`, `documentos` | Configuração (`temporalidade`) | Comissão de avaliação, fora do sistema na A1 | PT-32 |
| F7 | Consulta da trilha de auditoria, verificação da cadeia e exportação de evidência | `auditoria` | — | — (papel `AUDITOR`) | PT-33 (tela); PT-04 (backend) |
| F8 | Painel das integrações (outbox, fila de mortos, reconciliação) | `integracao` | — | — | PT-33 |

Ficam fora da Administração, porque já têm dono:
- a agenda de sessões, que é da Secretaria e da coordenação (PT-18);
- os critérios de redistribuição, que são da coordenação (PT-21);
- a composição das juntas e os mandatos (PT-12).

Mudar a **regra** que governa essas coisas, como o quórum, o tamanho da turma ou os motivos de redistribuição, é F1.

## 3. Proposta de alteração (F1, F2, F3, F5)

Toda mudança de configuração, calendário ou modelo passa pelo mesmo ciclo, implementado uma vez em `configuracao` como `PropostaAlteracao`.

```
RASCUNHO ──submeter──▶ EM_APROVACAO ──aprovar──▶ APROVADA ──(vigência chega)──▶ VIGENTE
    │                      │                         │
    └──cancelar──▶ CANCELADA   └──rejeitar──▶ REJEITADA    └──cancelar antes da vigência──▶ CANCELADA
```

**Rascunho.** O proponente altera os valores em formulários por seção, nunca em YAML cru na tela. Cada salvamento roda:
- a validação do esquema;
- as regras de consistência do doc 06;
- as regras de vigência da seção 4.

O rascunho mostra os erros em linguagem de negócio, por exemplo "a turma precisa ter número ímpar de membros".

**Submissão** exige:
1. **diff legível** contra a versão vigente, gerado pelo sistema, no formato "Prazo de recurso de 1ª instância: 30 → 20 dias";
2. **justificativa** em texto;
3. **ato normativo de origem**, quando a mudança decorre de norma (tipo, número, data e documento anexado, como uma portaria ou comunicado);
4. **vigência pretendida**, com data e hora no fuso da instalação.

A submissão também valida o **impacto**. O sistema calcula e mostra:
- quantos processos em curso a mudança alcança;
- quais lotes e sessões futuras serão os primeiros sob a nova regra;
- se algum prazo em curso seria encurtado (bloqueia, pela RN41) ou prorrogado (avisa).

**Aprovação.** Quem aprova precisa ter um dos papéis de `administracao.aprovadores.<tipo>` e ser **pessoa diferente** do proponente. O aprovador vê o mesmo diff e o mesmo impacto. Ele aprova ou rejeita com motivo; a rejeição exige motivo. A decisão é registro imutável (`decisao_proposta`).

**Publicação.** Na data de vigência, o job `PublicadorDeVersoes` cria a nova `regimento_versao` (ou `modelo_documento_versao`, ou o registro de calendário) e publica `RegimentoVersaoPublicada`. Os módulos que guardam cache da configuração o invalidam ao receber esse evento. O job roda com ShedLock e é idempotente.

**Conflito.** Só pode existir **uma** proposta `EM_APROVACAO` ou `APROVADA` não vigente por seção do regimento. Uma segunda proposta para a mesma seção fica em rascunho até a primeira ser publicada, rejeitada ou cancelada. Assim duas mudanças nunca se sobrepõem em silêncio.

**Emergência.** Não existe fluxo de emergência que pule a aprovação. Uma indisponibilidade do sistema que afeta prazos é tratada como suspensão de expediente (seção 5), que também é aprovada, mas pode ter vigência imediata.

## 4. Vigência e não retroatividade (RN39)

| O que muda | A partir de quando vale | Por quê |
|---|---|---|
| `distribuicao`, `turmas`, `composicao.posicoesPorJunta` | Primeiro lote semanal **iniciado** depois da vigência | Um lote em curso usa uma única versão (doc 07) |
| `sessao`, `votacao`, `relatoria`, `resultados`, `impedimento`, `diligencia` | Primeira sessão **aberta** depois da vigência; sessão já aberta termina com a versão em que abriu | A abertura grava `config_versao` |
| `designacao.modo` | Primeiro lote iniciado depois da vigência. Designações já seladas continuam sigilosas até a abertura da sua sessão, qualquer que seja o novo modo | Trocar para `ABERTO` não pode revelar o que foi selado como sigiloso |
| `prazos` | Contagens cujo **termo inicial** ocorra depois da vigência; prazos já iniciados seguem a regra antiga (D-14) | Segurança jurídica: o cidadão foi informado do prazo |
| `pecas`, `documentos`, `identidade.nivelMinimoPorAto` | Peças protocoladas depois da vigência; rascunhos abertos são revalidados no protocolo | O protocolo grava a versão |
| `modulos` (desligar) | Imediato para novas entradas; processos em curso no módulo terminam nele (D-15) | Desligar não pode deixar processo órfão |
| `publicidade`, `notificacao` | Atos publicados depois da vigência | — |

Regras gerais:
- **Antecedência mínima.** A vigência precisa ser no mínimo `administracao.antecedenciaMinima` depois da aprovação (SP: 1 dia útil; PROVISÓRIO, D-16). A exceção é a suspensão de expediente.
- A vigência nunca é anterior ao momento da aprovação; o sistema recusa.
- Todo ato continua gravando `config_versao`. A reconstituição de um processo antigo usa a versão gravada no ato, nunca a vigente.

## 5. Calendário (F2, RN41)

Tipos de registro:
- `FERIADO`, com abrangência nacional, estadual ou municipal;
- `PONTO_FACULTATIVO`, do órgão;
- `SUSPENSAO_EXPEDIENTE`, com intervalo de data e hora e motivo, como uma indisponibilidade do sistema, greve ou calamidade.

Regras:
- **Carga anual.** Os feriados nacionais do ano seguinte entram como proposta gerada pelo sistema em 1º de outubro, a partir de uma lista mantida no produto e revisada a cada release. Os estaduais e municipais vêm da configuração do órgão. Todos passam pela aprovação.
- **Data passada.** Não se inclui nem se revoga feriado ou ponto facultativo com data passada. Um fato passado que precise afetar prazos entra como `SUSPENSAO_EXPEDIENTE` com ato normativo.
- **Só prorroga.** Ao publicar uma mudança de calendário, `prazos` recalcula os prazos em curso alcançados. O recálculo pode **prorrogar** um prazo, e nunca encurta um prazo já comunicado ao cidadão. Se a revogação de um feriado futuro encurtaria algum prazo comunicado, o prazo comunicado prevalece e a divergência fica registrada.
- **Indisponibilidade do sistema.** Quando o monitoramento registra indisponibilidade do portal no último dia de um prazo, o sistema gera automaticamente uma proposta de `SUSPENSAO_EXPEDIENTE` para aprovação. Nunca a publica sozinho. Ver D-17.
- **Nada se apaga.** Revogar é um registro novo (`calendario_revogacao`), e o original permanece.

## 6. Importação e exportação da configuração (F3)

- **Instalação.** Na primeira subida, com o banco vazio, o YAML de `config/regimentos/<orgao>.yaml` vira a versão 1 (`aplicado_por = INSTALADOR`).
- **Depois da instalação, o banco é a fonte de verdade.** Se, numa subida, o YAML do pacote tiver hash diferente da última versão importada, a aplicação sobe normalmente com a versão vigente do banco e registra um alerta de "configuração do pacote divergente". Ela não aplica o YAML sozinha (ADR-0011).
- **Importar** um YAML é uma proposta como qualquer outra: o sistema gera o diff contra a versão vigente, e a importação passa pela aprovação e pelas regras de vigência.
- **Exportar** gera o YAML da versão vigente ou de qualquer versão histórica, com o hash da versão. Serve para backup, para o repositório da implantação e para auditoria externa.

## 7. Papéis por escopo e segregação de funções (F4, RN42)

O usuário interno vem do provedor de identidade (Keycloak federado ao LDAP/AD do órgão). O SIREJ guarda as **atribuições de papel**, cada uma com papel, escopo, início, fim e origem.

**Escopo**:
- `ORGAO`, para o órgão todo;
- `JUNTA`, para uma junta;
- `SESSAO`, para uma sessão, usado em substituições pontuais.

**Origem**:
- `DERIVADA_MANDATO`: `MEMBRO` e `PRESIDENTE` (e o vice) **só** existem por derivação do mandato vigente em `composicao`. O console não tem como conceder papel de julgador. Mandato encerrado ou suspenso revoga o papel no mesmo instante.
- `CONCEDIDA`: os demais papéis, concedidos pelo console.

**Concessão**:
- Ninguém concede papel a si mesmo.
- Papéis sensíveis (`ADMIN`, `AUDITOR`, `COORDENADOR`) exigem dupla aprovação: um `ADMIN` propõe e **outro** `ADMIN` aprova. A primeira atribuição de `ADMIN` da instalação é feita pelo instalador e auditada.
- Toda atribuição concedida tem fim de vigência, no máximo `administracao.vigenciaMaximaPapel` (SP: 12 meses; PROVISÓRIO, D-18). A renovação é uma nova atribuição.
- A revogação é imediata, é um registro novo (`revogacao_papel`) e encerra as sessões ativas do usuário.

**Incompatibilidades.** O sistema recusa a atribuição, ou a derivação, que gere conflito:

| Papel | Incompatível com | Escopo do conflito |
|---|---|---|
| `ADMIN` | `MEMBRO`, `PRESIDENTE`, `SECRETARIA`, `AUDITOR`, `COORDENADOR` | Qualquer |
| `AUDITOR` | Qualquer papel que pratique ato processual | Qualquer |
| `SECRETARIA` | `MEMBRO`, `PRESIDENTE` | Mesma junta |
| `COORDENADOR` | `MEMBRO`, `PRESIDENTE` | Qualquer (D-18) |

Quando uma derivação de mandato conflita com um papel concedido, por exemplo um secretário que é empossado membro da mesma junta, o mandato **não** é bloqueado. O papel concedido é suspenso automaticamente e a coordenação recebe um alerta.

**Revisão periódica.** Todo mês o sistema gera a lista de atribuições ativas por papel, para recertificação pelo coordenador. A recertificação não é obrigatória para o funcionamento; uma atribuição não recertificada em 2 ciclos aparece em destaque no painel.

## 8. Modelos de documento (F5, RN43)

- Os tipos de modelo são `ATA`, `ACORDAO`, `EMENTA`, `CERTIDAO_JULGAMENTO`, `NOTIFICACAO_RESULTADO`, `EXIGENCIA`, `RECIBO_PROTOCOLO`, `PAUTA` e `RELATORIO_SECRETARIA`. Um módulo novo acrescenta tipos.
- O modelo é HTML restrito com campos de mesclagem de uma **lista fechada por tipo**, como `{{processo.numero}}`, `{{sessao.data}}` e `{{resultado.rotulo}}`. Campo desconhecido reprova a validação.
- **Campos sigilosos.** Os campos de designação (junta, posição, relator, revisor, 3º membro) só existem nos tipos emitidos **depois** da abertura da sessão: ata, acórdão, certidão e notificação de resultado. Em `PAUTA`, `RECIBO_PROTOCOLO` e `EXIGENCIA` esses campos não existem. Com `designacao.modo = SIGILOSO`, o modelo de pauta não pode conter junta ou posição.
- A pré-visualização usa dados sintéticos e nunca dados de processo real, porque o administrador não vê processo.
- A versão publicada é imutável. Todo documento gerado grava `modelo_versao_id` e o hash do modelo, e a geração em PDF/A é feita por `documentos`.
- O produto traz modelos de referência de SP, carregados na instalação.

## 9. Temporalidade e destinação (F6, RN44)

- A configuração `temporalidade` define classes documentais. Cada classe tem código, prazo de guarda corrente, prazo de guarda intermediária e destinação final (`ELIMINACAO` ou `GUARDA_PERMANENTE`). O prazo mínimo vem de `retencao.anos`.
- O processo ganha a **fase arquivística**, um atributo separado da situação do processo: `CORRENTE`, `INTERMEDIARIA` ou `AGUARDANDO_DESTINACAO`. A contagem começa quando o processo chega a `ENCERRADO`, `INADMITIDO` ou `ARQUIVADO`. Não confunda `SituacaoProcesso.ARQUIVADO`, que é o fim do rito, com a fase arquivística.
- Um job mensal move os processos entre as fases e gera a **lista de elegíveis à destinação**, para a comissão de avaliação de documentos do órgão.
- **Na A1 o sistema não elimina nada.** A eliminação física, que exige termo, publicação e Object Lock vencido, fica para decisão posterior (D-19) e para um pacote próprio. A invariante 5 continua valendo integralmente até lá.
- Um processo com demanda judicial registrada (M8), ou marcado com retenção legal, não entra na lista de elegíveis.

## 10. Console administrativo (F7, F8, PT-33)

Área `/admin` do back-office, visível só para `ADMIN`, `COORDENADOR` (aprovações) e `AUDITOR` (auditoria).

| Tela | Conteúdo |
|---|---|
| Início | Propostas aguardando minha aprovação; próximas vigências; alertas (configuração do pacote divergente, recertificação, fila de mortos, ancoragem atrasada) |
| Regimento | Versão vigente por seção, em linguagem de negócio, com artigo de origem; histórico de versões; criar proposta |
| Proposta | Formulário por seção, diff, impacto, justificativa, ato normativo, vigência; botões submeter, aprovar e rejeitar conforme o papel |
| Calendário | Visão anual com feriados, pontos facultativos e suspensões; criar proposta |
| Modelos | Lista por tipo; editor com campos permitidos; pré-visualização sintética; histórico |
| Usuários e papéis | Busca por nome ou login (nunca por CPF de recorrente); atribuições com escopo e vigência; conceder, revogar; recertificação |
| Auditoria (`AUDITOR`) | Consulta por alvo, ator e período; verificação da cadeia sob demanda; situação da ancoragem diária; exportação de pacote de evidência assinado |
| Integrações | Por porta: fila de saída, fila de mortos, última reconciliação e divergências. Ação permitida: reenfileirar uma mensagem da fila de mortos (idempotente, auditada). Não há ação que dispare distribuição |

Requisitos de interface (doc 11):
- linguagem de negócio, com artigo do regimento ao lado de cada parâmetro;
- confirmação explícita em aprovar, rejeitar e revogar;
- axe-core sem violações.

## 11. O que a Administração nunca faz

Cada item tem teste negativo obrigatório no PT correspondente.

1. Ler conteúdo de processo, documento dos autos, dado pessoal de recorrente ou designação.
2. Disparar, refazer, alterar ou cancelar distribuição, pauta, sessão ou voto.
3. Alterar ou remover registro de auditoria, versão de regimento, versão de modelo, decisão de proposta ou movimentação.
4. Expor ou alterar parâmetro que representa invariante: `distribuicao.falhaFechada`, a ausência de voto de qualidade e de pagamento como condição, o rol fechado de resultados como conceito.
5. Publicar mudança sem aprovação de outra pessoa, ou com vigência retroativa.
6. Conceder papel de julgador ou conceder papel a si mesmo.
7. Eliminar documento ou processo.
