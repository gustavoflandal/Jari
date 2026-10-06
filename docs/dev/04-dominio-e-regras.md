# 04 — Domínio, estados e regras de negócio

## 1. Tipos de peça e quem decide

| Tipo (`TipoPeca`) | Prazo padrão SP | Quem decide | Vira processo de JARI? |
|---|---|---|---|
| `DEFESA_AUTUACAO` | Impresso na NA (parâmetro) | Órgão autuador | Não |
| `INDICACAO_CONDUTOR` | 15 dias da NA | Órgão autuador | Não (roteamento) |
| `RECURSO_1A_INSTANCIA` | 30 dias da ciência da NP | JARI | Sim |
| `RECURSO_2A_INSTANCIA` | 30 dias da publicação/notificação da decisão | CETRAN | Remessa |
| `PEDIDO_EFEITO_SUSPENSIVO` | — | Autoridade de trânsito | Não |
| `CONVERSAO_ADVERTENCIA` | — | Autoridade de trânsito | Não |
| `JUNTADA_DOCUMENTOS` | Enquanto o processo estiver aberto | — | Anexa a processo existente |

A tabela vem da configuração (`pecas.*`, doc 06). O assistente de peticionamento usa a fase do AIT para oferecer **só as peças cabíveis** (RN03, RN15, RN16).

## 2. Máquina de estados do processo de recurso

Estados (`SituacaoProcesso`):

`RASCUNHO`, `PROTOCOLADO`, `EM_TRIAGEM`, `EM_EXIGENCIA`, `INADMITIDO`, `ARQUIVADO`, `EM_INSTRUCAO`, `AGUARDANDO_DISTRIBUICAO`, `DISTRIBUIDO`, `PAUTADO`, `EM_JULGAMENTO`, `EM_DILIGENCIA`, `JULGADO`, `PUBLICADO`, `REMETIDO_2A_INSTANCIA`, `TRANSITADO`, `EM_CUMPRIMENTO`, `ENCERRADO`.

| De | Evento | Guarda | Para |
|---|---|---|---|
| `RASCUNHO` | `PecaProtocolada` | Peça assinada com nível de autenticação exigido; documentos com hash | `PROTOCOLADO` |
| `PROTOCOLADO` | `TriagemIniciada` | — | `EM_TRIAGEM` |
| `EM_TRIAGEM` | `ExigenciaEmitida` | Motivo tipificado; prazo calculado | `EM_EXIGENCIA` |
| `EM_EXIGENCIA` | `ExigenciaAtendida` | Dentro do prazo | `EM_TRIAGEM` |
| `EM_EXIGENCIA` | `ExigenciaVencida` | Prazo vencido sem atendimento | `ARQUIVADO` |
| `EM_TRIAGEM` | `RecursoInadmitido` | Motivo do rol configurado | `INADMITIDO` |
| `EM_TRIAGEM` | `RecursoAdmitido` | — | `EM_INSTRUCAO` |
| `EM_INSTRUCAO` | `InstrucaoConcluida` | Informação do agente juntada ou prazo do agente vencido | `AGUARDANDO_DISTRIBUICAO` |
| `AGUARDANDO_DISTRIBUICAO` | `ProcessoDistribuido` | Somente pelo job semanal (doc 07) | `DISTRIBUIDO` |
| `DISTRIBUIDO` | `ProcessoPautado` | Pauta gerada para a sessão | `PAUTADO` |
| `PAUTADO` | `SessaoAberta` | Abertura válida (quórum, presidente) | `EM_JULGAMENTO` |
| `EM_JULGAMENTO` | `DiligenciaSolicitada` | Motivo e prazo | `EM_DILIGENCIA` |
| `EM_DILIGENCIA` | `DiligenciaConcluida` | — | `DISTRIBUIDO` (volta à pauta da mesma posição) |
| `EM_JULGAMENTO` | `ProcessoRetiradoDePauta` | Relator ausente, impedimento sem substituto, sessão encerrada sem julgar | `DISTRIBUIDO` |
| `EM_JULGAMENTO` | `ResultadoProclamado` | Votos suficientes assinados (RN07, RN14) | `JULGADO` |
| `JULGADO` | `DecisaoPublicada` | Acórdão assinado; notificação emitida | `PUBLICADO` |
| `PUBLICADO` | `Recurso2aInstanciaProtocolado` | Dentro do prazo (RN10) | `REMETIDO_2A_INSTANCIA` |
| `PUBLICADO` | `PrazoRecursalVencido` | — | `TRANSITADO` |
| `REMETIDO_2A_INSTANCIA` | `DecisaoSuperiorRecebida` | — | `EM_CUMPRIMENTO` |
| `TRANSITADO` | `CumprimentoIniciado` | Decisão altera a penalidade | `EM_CUMPRIMENTO` |
| `TRANSITADO` | `ProcessoEncerrado` | Decisão não altera a penalidade | `ENCERRADO` |
| `EM_CUMPRIMENTO` | `CumprimentoConfirmado` | Confirmação do sistema de multas | `ENCERRADO` |
| qualquer aberto | `RemetidoPorIncompetencia` | RN09 | `ENCERRADO` (com remessa registrada) |

Regras da máquina:

- Transição só acontece por **comando de domínio** que grava a movimentação; não existe `setSituacao`.
- Toda transição grava: data de entrada no estado, **prazo-alvo** do estado (motor de prazos) e o ator.
- Transição inválida lança `TransicaoInvalidaException` e não grava nada.
- A situação é projeção das movimentações; reconstruir a projeção a partir do log deve dar o mesmo resultado (teste obrigatório).

## 3. Regras de negócio

Formato: id · regra · parâmetro (doc 06) · teste mínimo obrigatório.

### Prazos e admissibilidade

| Id | Regra | Parâmetro | Teste mínimo |
|---|---|---|---|
| RN01 | Prazo de recurso à JARI conta da **ciência** (efetiva ou presumida) da NP, com comprovante registrado. | `prazos.recurso1a` | Peça no último dia é tempestiva; no dia seguinte, intempestiva. |
| RN02 | Na notificação eletrônica, a ciência presumida ocorre N dias após a inclusão no meio eletrônico (Res. 918/2022: 30). | `prazos.cienciaPresumidaEletronica` | Sem ciência efetiva, a contagem parte de inclusão + 30. |
| RN03 | Defesa da autuação não é julgada pela JARI; o sistema impede o encaminhamento e orienta a peça correta. | `pecas.*.decisor` | Tentar protocolar defesa como recurso à JARI é recusado com orientação. |
| RN04 | Indicação de real infrator: 15 dias do recebimento da NA. | `prazos.indicacaoCondutor` | Limite de prazo. |
| RN05 | Proibido exigir pagamento ou depósito para admitir qualquer recurso. | — (fixo) | Nenhum campo, regra ou tela condiciona protocolo a pagamento; teste de varredura no domínio. |
| RN09 | Peça recebida por órgão incompetente é registrada com data preservada e remetida ao competente; registro no RENAINF quando aplicável. | — | Data original preservada após remessa. |
| RN10 | Prazo de recurso à 2ª instância: 30 dias da publicação/notificação da decisão. | `prazos.recurso2a` | Limite de prazo. |
| RN15 | Indicação de condutor e transferência de pontuação não são decididas pela JARI; são roteadas e não admitidas como recurso. | `pecas.*` | Roteamento correto. |
| RN16 | Conversão em advertência é requerimento próprio decidido pela autoridade. | `pecas.*` | Não entra em pauta da JARI. |
| RN17 | Pagar não impede recorrer; deferimento posterior gera evento de restituição. | — | Recurso de multa paga é admitido; cancelamento gera `RestituicaoSolicitada`. |
| RN18 | Descontos são só exibidos; cálculo é do sistema de multas. | — | Nenhum cálculo de valor no SIREJ. |
| RN31 | Feriados e dias não úteis vêm do calendário configurado (nacional, estadual, municipal e pontos facultativos do órgão); vencimento em dia não útil prorroga para o próximo útil. | `calendario.*` | Vencimento em feriado municipal prorroga. |

### Processo e documentos

| Id | Regra | Parâmetro | Teste mínimo |
|---|---|---|---|
| RN11 | Nenhum documento juntado é excluído; correção por desentranhamento registrado ou nova juntada. | — (fixo) | Não existe operação de exclusão; desentranhado continua recuperável. |
| RN12 | Toda decisão que altera a penalidade dispara integração de cumprimento; nenhuma alteração manual de pontuação ou débito. | — (fixo) | Cancelamento gera evento de integração; não há campo de pontuação editável. |
| RN32 | O protocolo é atômico: número único, data/hora, hash SHA-256 de cada documento e recibo, ou nada. | — | Falha no meio não deixa número consumido sem processo. |
| RN33 | O sistema não pede ao cidadão documento que a administração já possui (Lei 14.129/2021); busca nas bases integradas. | `documentos.obtidosDeOficio` | Campo não é exigido quando a integração fornece o dado. |

### Composição, sessão e julgamento

| Id | Regra | Parâmetro | Teste mínimo |
|---|---|---|---|
| RN06 | Membro que lavrou o AIT está impedido; membro de JARI não pode integrar CETRAN. O sistema bloqueia. | — | Agente autuador como relator é bloqueado. |
| RN07 | Decisão por maioria, turma com número ímpar de membros, votos de igual peso, sempre fundamentada e vinculada a dispositivo normativo. Na exceção de 2 votos (RN14), os dois precisam concordar. | `votacao.*` | Voto sem fundamentação ou sem dispositivo é recusado. |
| RN13 | O relator recebe checklist de três eixos: regularidade do procedimento; força maior alegada; antecedentes do recorrente. | `relatoria.checklist` | Checklist presente e salvo com o voto. |
| RN14 | Cada recurso é decidido pela turma (relator, revisor, 3º membro); voto do relator obrigatoriamente motivado. Quórum mínimo de votos por decisão é parâmetro (SP: 3; exceção regimental: 2 com presidente ou vice presente). | `votacao.votosMinimos`, `votacao.excecaoMaioriaSimples` | Proclamação recusada abaixo do mínimo; exceção só com presidente presente. |
| RN19 | Distribuição entre juntas eletrônica, semanal e equitativa por posição, respeitando conexão por requerente ou veículo. | `distribuicao.*` | Doc 07, testes de propriedade. |
| RN20 | No modo sigiloso, ninguém conhece a designação antes da abertura da sessão. | `designacao.modo` | Doc 07 e 08. |
| RN21 | Turma com um membro de cada segmento; nenhum voto de qualidade, peso diferenciado ou desempate pelo órgão. | `composicao.segmentos` | Turma com dois membros do mesmo segmento é inválida. |
| RN22 | Sessão abre com no mínimo N membros de segmentos diferentes; deliberação excepcional exige presidente ou vice. | `sessao.quorumAbertura` | Abertura recusada sem quórum. |
| RN23 | Todo ato de distribuição e toda decisão ficam nos autos; relatório e votos assinados nominalmente. | — (fixo) | Não existe resultado sem votos assinados. |
| RN24 | Indisponibilidade suspende a distribuição; processos do período entram na semana seguinte; sem distribuição manual. | — (fixo) | Doc 07. |
| RN25 | Turmas se alternam; partição só se repete após esgotar as demais. | `turmas.rodizio` | Doc 07. |
| RN26 | Rol fechado de resultados; sem provimento parcial. | `resultados` | Enum + restrição no banco; valor fora do rol é recusado. |
| RN27 | Julgamento em ordem cronológica de interposição dentro da posição; redistribuição entre juntas só por força maior, impedimento ou suspeição, com motivo nos autos. | `distribuicao.redistribuicao` | Ordem da pauta; redistribuição sem motivo é recusada. |
| RN28 | Sem sustentação oral; diligência presencial exige ao menos dois membros de segmentos diferentes. | `diligencia.presencial` | Diligência presencial com um membro é recusada. |
| RN29 | Membros não fazem download dos autos; visualização com marca d'água e acesso registrado. | `autos.downloadMembros` | Endpoint de download recusa perfil de membro. |
| RN30 | O sistema mede e evidencia hipóteses de perda de mandato; a sanção depende de procedimento com ampla defesa. | `mandato.perda.*` | Nenhuma ação automática de perda de mandato. |
| RN34 | O membro declara impedimento ou suspeição com motivo tipificado; o sistema recompõe a turma e contabiliza declarações por membro. | `impedimento.motivos` | Declaração sem motivo é recusada; contador incrementa. |
| RN35 | Ausente é substituído por presente do mesmo segmento no 2º ou 3º voto; processos do relator ausente voltam à pauta seguinte. | `sessao.substituicao` | Doc 07. |
| RN36 | A presença registrada na sessão é cancelada quando o membro recusa imotivadamente suas atribuições (art. 14, §2). | `presenca.*` | Cancelamento registrado com motivo. |

### Prazo de julgamento

| Id | Regra | Parâmetro | Teste mínimo |
|---|---|---|---|
| RN08 | Recurso não julgado em 30 dias: o sistema propõe lista de elegíveis a efeito suspensivo; a concessão é ato da autoridade. | `prazos.julgamento` | Lista diária correta; nenhuma concessão automática. |
| RN37 | Alertas de prazo em T-10, T-5 e T-1 dias para todo estado com prazo-alvo. | `prazos.alertas` | Alertas gerados nos dias certos. |

## 4. Resultados da referência SP

| Código | Rótulo | Altera penalidade? | Texto ao cidadão (próximo passo) |
|---|---|---|---|
| `REJEICAO_ADMINISTRATIVA` | Rejeição administrativa do recurso | Não | Explica o motivo e o prazo de 2ª instância |
| `NAO_CONHECIMENTO_INTEMPESTIVIDADE` | Não conhecimento por intempestividade | Não | Explica a data-limite e a data da peça |
| `NAO_CONHECIMENTO_ILEGITIMIDADE` | Não conhecimento por ilegitimidade de parte | Não | Explica quem pode recorrer |
| `MANUTENCAO_PENALIDADE` | Manutenção da penalidade | Não | Prazo de 2ª instância |
| `CANCELAMENTO_PENALIDADE` | Cancelamento da penalidade | Sim | Informa cumprimento e restituição, se houver pagamento |

Todo resultado mostrado ao cidadão vem com motivo e próximo passo possível.
