# 02 — Glossário e linguagem ubíqua

Use exatamente estes termos em código, banco, API, eventos e interface. O nome de código fica na segunda coluna (sem acento).

## Termos do rito

| Termo | Nome no código | Definição |
|---|---|---|
| Auto de Infração de Trânsito | `Ait` | Documento que inicia o processo de penalidade. Vem do sistema de multas; o SIREJ não o cria. |
| Notificação da Autuação | `NotificacaoAutuacao` (NA) | Dá ciência da infração; abre prazo de defesa da autuação e de indicação de condutor. |
| Notificação da Penalidade | `NotificacaoPenalidade` (NP) | Comunica a multa imposta; abre prazo de recurso à JARI. |
| Ciência | `Ciencia` | Momento em que o interessado é considerado notificado: efetiva (comprovante) ou presumida (30 dias após inclusão no meio eletrônico, Res. CONTRAN 918/2022). |
| Peça | `Peca` | Manifestação escrita do interessado: defesa da autuação, indicação de condutor, recurso de 1ª instância, recurso de 2ª instância, pedido de efeito suspensivo, juntada. |
| Processo | `Processo` | Unidade que tramita. Um processo nasce de uma peça protocolada e acumula documentos e movimentações. |
| Protocolo | `Protocolo` | Ato de receber a peça: número único, data/hora, hash dos documentos, recibo. |
| Recorrente | `Recorrente` | Quem apresenta a peça (proprietário, condutor ou representante). |
| Procurador | `Procurador` | Representante com procuração registrada. |
| Movimentação | `Movimentacao` | Evento imutável na vida do processo (ator, ato, data/hora, documento). |
| Documento | `Documento` | Arquivo juntado, imutável, endereçado por hash SHA-256, normalizado em PDF/A. |
| Desentranhamento | `Desentranhamento` | Retirada lógica de documento dos autos, registrada; o arquivo original permanece. |
| Triagem de admissibilidade | `Triagem` | Verificação de tempestividade, legitimidade, representação, objeto e competência. |
| Exigência | `Exigencia` | Pedido de saneamento ao recorrente, com prazo. |
| Tempestividade | `Tempestividade` | Peça apresentada dentro do prazo. |
| Instrução | `Instrucao` | Juntada de informações do agente autuador e provas antes do julgamento. |
| Diligência | `Diligencia` | Pedido de informação ou prova feito durante a relatoria ou o julgamento. |
| Efeito suspensivo | `EfeitoSuspensivo` | Concedido pela autoridade quando o recurso não é julgado em 30 dias por força maior (art. 285 CTB). |
| Cumprimento | `Cumprimento` | Execução da decisão no sistema de multas. |
| Remessa | `Remessa` | Envio dos autos a outro órgão (incompetência) ou ao CETRAN (2ª instância). |

## Termos do colegiado

| Termo | Nome no código | Definição |
|---|---|---|
| JARI | `Jari` | Conjunto das juntas do órgão (a instalação). |
| Junta | `Junta` | Colegiado que julga; em SP, 6 membros. |
| Segmento (representação) | `Segmento` | Origem do membro. Em SP: `COMUNIDADE`, `ENTIDADE_EXECUTIVA`, `SOCIEDADE_CIVIL`. Lista configurável. |
| Membro | `Membro` | Pessoa com mandato ativo numa junta, num segmento. |
| Suplente | `Suplente` | Membro que substitui titular do mesmo segmento. |
| Mandato | `Mandato` | Período em que a pessoa é membro (SP: 1 ano, renovável). |
| Posição | `Posicao` | Vaga fixa de membro na junta, identificada por letra (A a F em SP). A distribuição semanal atribui processos a posições, não a pessoas. |
| Presidente / vice | `papelPresidencia` | Papel de um membro na junta. |
| Coordenador | `Coordenador` | Responsável pelo conjunto das juntas. |
| Secretaria | `Secretaria` | Apoio administrativo; não julga. |
| Turma de julgamento | `Turma` | Grupo de 3 membros de segmentos diferentes que decide um processo. Até 2 por reunião em SP. |
| Partição de turmas | `ParticaoTurmas` | Divisão dos membros presentes em turmas numa reunião. Em SP há 4 partições possíveis. |
| Rodízio combinatório | `Rodizio` | Regra de não repetir partição antes de esgotar as demais (art. 17, §3). |
| Relator | `Relator` | Membro da posição a que o processo foi distribuído; redige relatório e voto motivado. |
| Revisor | `Revisor` | Segundo voto da turma. |
| Terceiro membro | `TerceiroMembro` | Terceiro voto da turma. |
| Voto | `Voto` | Manifestação individual assinada, com resultado e fundamentação. |
| Resultado | `Resultado` | Valor do rol fechado configurado (SP: `REJEICAO_ADMINISTRATIVA`, `NAO_CONHECIMENTO_INTEMPESTIVIDADE`, `NAO_CONHECIMENTO_ILEGITIMIDADE`, `MANUTENCAO_PENALIDADE`, `CANCELAMENTO_PENALIDADE`). |
| Decisão | `Decisao` | Resultado apurado a partir dos votos (unanimidade ou maioria). |
| Acórdão | `Acordao` | Documento da decisão, com ementa, assinado. |
| Ementa | `Ementa` | Resumo público da decisão. |
| Pauta | `Pauta` | Lista ordenada de processos de uma reunião da junta. |
| Reunião / sessão | `Sessao` | Encontro da junta para julgar. Tipos: `ORDINARIA`, `EXTRAORDINARIA`, `PLENARIA`. |
| Abertura da sessão | `AberturaSessao` | Ato formal do presidente: presenças, quórum, distribuição interna, revelação. |
| Ata | `Ata` | Registro da sessão, assinado. |
| Impedimento / suspeição | `Impedimento` / `Suspeicao` | Motivos tipificados que afastam o membro de um processo. |

## Termos da distribuição

| Termo | Nome no código | Definição |
|---|---|---|
| Distribuição semanal | `DistribuicaoSemanal` | Job que atribui os processos admitidos na semana a junta + posição + sequência. |
| Distribuição interna | `DistribuicaoInterna` | Ato na abertura da sessão que associa posições a membros presentes e forma as turmas. |
| Conexão | `Conexao` | Vínculo entre processos do mesmo requerente ou do mesmo veículo; conexos vão à mesma posição e turma. |
| Grupo de conexão | `GrupoConexao` | Conjunto de processos ligados por conexão (fecho transitivo). |
| Selo da distribuição | `SeloDistribuicao` | Compromisso criptográfico (commit–reveal) que torna a designação ilegível e inalterável até a abertura. |
| Semente | `Semente` | Valor aleatório criptográfico que alimenta o sorteio; selada e verificável. |
| Selo do processo | `SeloProcesso` | Identificador impresso: `2026-S22 / 14ª Junta / B / seq. 20`. |
| Modo de designação | `ModoDesignacao` | `SIGILOSO` (padrão, SP) ou `ABERTO` (ex.: DETRAN-PB publica relator na pauta). |

## Siglas

AIT, NA, NP, JARI, CETRAN, CONTRANDIFE, CONTRAN, SENATRAN, SNT, SNE (Sistema de Notificação Eletrônica), CDT (Carteira Digital de Trânsito), RENAINF, RENAVAM, RENACH, CTB (Lei 9.503/1997), LGPD, PDF/A, PAdES, HSM, KMS, ICP-Brasil, NIC (multa por não indicação de condutor), CADIN.
