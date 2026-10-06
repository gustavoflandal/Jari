# 17 — Comparação de regimentos

Compara, tema a tema do doc 06, o que a norma de cada órgão-alvo diz, com o artigo de origem de cada valor, e confere a configuração de referência (`config/regimentos/sp.yaml`) contra o texto. É insumo da parametrização (doc 06), das dúvidas (doc 16) e das propostas por edital.

Regras desta análise:
- Nenhum valor foi inventado. O que a norma não diz aparece como **omisso** e, quando é lacuna real, tem uma `D-xx` no doc 16 com opção provisória restritiva.
- Os valores do `sp.yaml` **não foram alterados**. Onde o texto diverge ou não sustenta o valor, isso está na coluna "sp.yaml confere?" e na seção 3.
- Regras do regimento que contrariem invariante do `CLAUDE.md` não são adaptadas: ficam descritas na seção 4.

## 1. Fontes e situação por órgão

| Órgão | Texto analisado | Situação |
|---|---|---|
| São Paulo (CET) — referência | Regimento das JARIs do Município de São Paulo, instituído pelo Comunicado nº 007/23 da Autoridade de Trânsito (Aviso Geral nº 016/23), Anexo III do Edital 001/2026-JARI/CET. Texto extraído em [`docs/fontes/regimentos/sp/regimento-jari-sp.md`](../fontes/regimentos/sp/regimento-jari-sp.md) (PDF `docs/fontes/JARI_CET.pdf`, páginas 11–33). Edital 001/2026 (itens 1 a 8) usado só onde citado. | **Analisado**: 12 temas |
| Curitiba (SMDT) | Nenhum. O repositório não tem o texto da Lei municipal 15.154/2017 nem o regimento interno. Lista do que falta em [`docs/fontes/regimentos/curitiba/README.md`](../fontes/regimentos/curitiba/README.md). | **Texto ausente**: análise parada (D-36) |

Normas citadas pelo regimento de SP e **ausentes do repositório** (não conferidas): Decreto municipal 60.982/2021 (organiza as JARIs e limita o número de juntas, arts. 2º e 5º, I); Lei 9.503/1997 (CTB); Resolução CONTRAN 357/2010 (diretrizes de regimento); Resolução CONTRAN 918/2022; Decreto municipal 56.130/2015 (Código de Conduta, art. 12, IX).

Legenda das colunas:
- **sp.yaml confere?** — `sim` (o artigo sustenta o valor), `parcial` (sustenta com ressalva), `sem artigo` (o regimento é omisso; valor é escolha do produto ou de outra norma), `diverge` (o texto aponta outro valor).
- **Doc 06 comporta?** — `sim` (há chave), `não` (proposta na seção 5).
- **Curitiba** — "texto ausente", salvo fato já registrado no `curitiba.yaml`, marcado "fonte: página institucional, a confirmar na norma".

## 2. Tabelas por tema

### 2.1 Composição

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Membros por junta | 6 membros (art. 6º, caput) | 6 posições em `composicao.posicoesPorJunta` | sim | sim | 6 titulares por junta — fonte: página institucional, a confirmar na norma |
| Número de juntas | Não fixado no regimento; criação e extinção propostas pela CET, "observado o limite" do Decreto 60.982/2021 (art. 5º, I). Enunciado e doc 19 citam 27 juntas | (dado de cadastro, não de regimento) | n/a | sim (cadastro, PT-12) | 4 juntas — fonte: página institucional, a confirmar na norma |
| Suplentes | "Poderão ser nomeados suplentes para cada segmento" (art. 6º, §4º); número não fixado | `suplentesPorSegmento: 0` | sem artigo (§4º permite, não fixa) — D-24 | sim | 3 suplentes por junta — fonte: página institucional, a confirmar na norma. O `suplentesPorSegmento: 1` do `curitiba.yaml` é inferência (3 suplentes ÷ 3 segmentos), não fato (D-11) |
| Presidente e vice | Qualquer membro, a critério da entidade executiva (art. 6º, §1º); vice substitui o presidente nas ausências (art. 26, I) | `presidenciaLivre: true` | sim | sim | texto ausente |
| Mandato | 1 ano, recondução por períodos sucessivos, para a mesma ou outra junta, a critério da entidade executiva (art. 11) | `mandato: { meses: 12, reconducao: true }` | sim (a recondução para outra junta não tem chave; é ato de cadastro) | sim | texto ausente |
| Posse | Termo de Responsabilidade e Termo de Posse antes da primeira reunião (art. 10) | — | n/a | sim (cadastro, PT-12) | texto ausente |
| Requisitos e vedações de membro | Reputação ilibada, idoneidade, conhecimento de trânsito, nível médio (art. 6º, caput e I–III); vedações do art. 8º, I–V (menor de idade; vínculo com CRT, CFC, despachante, escritório de recursos, médico/psicólogo credenciado; agente de fiscalização e chefias; CNH suspensa ou cassada; membro de CETRAN, CONTRANDIFE ou outra JARI) | — (RN06 cobre só agente autuador e membro de CETRAN) | n/a | **não** — proposta P10, D-33 | texto ausente |
| Credenciamento de entidades | Procedimento bienal; cadastro de entidades com 5 anos no Município e vínculo com trânsito; ordem de designação por sorteio publicado no DOC; perda da indicação (art. 9º, I–VI e §§1º–4º; art. 10, par. único). Edital 001/2026: até 60 entidades, validade 24 meses prorrogável até 48 (itens 1.1, 1.2, 8.4) | `modulos.credenciamento: true`, sem seção própria | n/a | **não** — proposta P11, D-33 | texto ausente |
| Coordenador-geral | Nomeado pela entidade executiva (art. 5º, II), que "atribuirá a um dos membros das Juntas" a coordenação (art. 28, caput); substituto designado pela entidade (art. 28, par. único) | `administracao.aprovadores.*: [COORDENADOR]`; doc 18 torna `COORDENADOR` incompatível com `MEMBRO`/`PRESIDENTE` (D-18) | **diverge** do art. 28, caput — D-27 | **não** — proposta P7 | texto ausente |
| Perda de mandato | Hipóteses do art. 12, I–XII; faltas: 3 seguidas ou 4 intercaladas em 1 ano "a partir da data da posse", contando reuniões "ordinárias/plenárias" (art. 12, II); incisos III a IX dependem de procedimento com ampla defesa (art. 12, par. único) | `mandato.perda: { faltasSeguidas: 3, faltasIntercaladasAno: 4, hipotesesComProcedimento: [III..IX] }` | parcial: valores conferem; a janela "a partir da posse" e a contagem de plenárias não têm chave | parcial — proposta P5, D-30 | texto ausente |

### 2.2 Segmentos

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Segmentos | Comunidade (art. 6º, I), entidade executiva municipal de trânsito (art. 6º, II), entidades da sociedade civil (art. 6º, III) | `[COMUNIDADE, ENTIDADE_EXECUTIVA, SOCIEDADE_CIVIL]` | sim | sim | texto ausente (D-11) |
| Origem de cada segmento | Comunidade: seleção conduzida pela CET (arts. 5º, IV, e 6º, I); entidade executiva: indicação da CET, empregados ou servidores da SMT (art. 6º, II); sociedade civil: indicação de associação credenciada (arts. 6º, III, e 9º) | — | n/a | **não** — proposta P11 (credenciamento) | texto ausente |
| Turma com um de cada segmento | 3 membros de diferentes representações (arts. 6º, §2º, e 17, caput) | `turmas.umPorSegmento: true` | sim | sim | texto ausente |

### 2.3 Posições

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Posições por segmento | 2 por segmento (art. 6º, I–III) | 2 por segmento | sim | sim | texto ausente |
| Letras A–F | O regimento não nomeia posições; fala em "membro" (arts. 16 e 22) | `posicoesPorJunta` A–F | sem artigo: convenção do produto. A distribuição por posição é o meio de cumprir "igual número de processos por membro" (art. 22) sem revelar o membro antes da sessão | sim | texto ausente |

### 2.4 Quórum

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Quórum de reunião da junta | Mínimo de 3 membros de diferentes representações (art. 13) | `sessao.quorumAbertura: { minimoMembros: 3, segmentosDistintos: 3 }` | parcial: confere com o art. 13, mas o art. 6º, §3º, diz que a **turma** pode "abrir a sessão e deliberar" com maioria simples e presença do presidente ou vice — D-22 | sim | texto ausente |
| Presidente ou vice para abrir | O presidente abre a reunião e executa a distribuição interna (arts. 15, I e III, e 25, II e III); o vice o substitui (art. 26, I); a omissão dos atos dos incisos III e IV do art. 15 "impede a reunião programada" (art. 15, par. único) | — | n/a | **não** — proposta P1, D-23 | texto ausente |
| Votos por decisão | 3 membros de diferentes representações (art. 17, caput) | `votacao.votosMinimos: 3` | sim | sim | texto ausente |
| Exceção de maioria simples | Turma pode deliberar com maioria simples, com presença obrigatória do presidente ou vice (art. 6º, §3º) ou "do presidente ou de seu suplente" (art. 17, §1º) | `excecaoMaioriaSimples: { permitida: true, minimo: 2, exigePresidenteOuVice: true }` | parcial: confere com art. 6º, §3º; o art. 17, §1º, fala em "suplente" do presidente, lido como vice (art. 26, I) — D-24; validade de 2 votos já é D-05 | sim | texto ausente |
| Turmas simultâneas | Até 2 por reunião (art. 17, §2º) | `turmas.maximoSimultaneas: 2` | sim | sim | texto ausente |
| Diligência presencial | No mínimo 2 membros de diferentes representações (art. 20, par. único) | `diligencia.presencial: { minimoMembros: 2, segmentosDistintos: 2 }` | sim | sim | texto ausente |

### 2.5 Prazos

O regimento de SP **não fixa** nenhum prazo processual do recurso. Os prazos do `sp.yaml` vêm do CTB e de resoluções do CONTRAN, cujo texto não está no repositório (D-21).

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Recurso à JARI | Omisso no regimento | `recurso1a: 30 dias da ciência da NP` (cita art. 285 CTB) | sem artigo no regimento; norma federal não conferida — D-21 | sim | texto ausente |
| Indicação de condutor | Omisso | `indicacaoCondutor: 15 dias` (cita art. 257, §7º, CTB) | sem artigo no regimento; norma federal não conferida — D-21 | sim | texto ausente |
| Recurso à 2ª instância | Omisso | `recurso2a: 30 dias` (cita arts. 288/289 CTB) | sem artigo no regimento; norma federal não conferida — D-21 | sim | texto ausente |
| Ciência presumida eletrônica | Omisso | `cienciaPresumidaEletronica: 30` (Res. CONTRAN 918/2022) | sem artigo no regimento; norma não conferida — D-21 | sim | texto ausente |
| Julgamento | Omisso | `julgamento: 30 dias` (art. 285 CTB) | sem artigo no regimento; norma federal não conferida — D-21 | sim | texto ausente |
| Exigência e informação do agente | Omisso | 10 dias, PROVISÓRIO | sem artigo — já é D-13 | sim | texto ausente |
| Relatoria | "Deixar de julgar ... dentro do prazo estabelecido" é hipótese de perda de mandato (art. 12, VI), mas o prazo não é estabelecido em lugar algum | `relatoria: { sessoes: 1 }` PROVISÓRIO | sem artigo — já é D-06 | sim | texto ausente |
| Convocação da plenária | Mínimo de 1 semana de antecedência (art. 21, caput) | — | n/a | **não** — proposta P4, D-30 | texto ausente |
| Cópia da ata da plenária anterior | Distribuída às juntas com no mínimo 2 semanas de antecedência (art. 21, II) | — | n/a | **não** — proposta P4, D-30 | texto ausente |
| Validade do credenciamento | 2 anos (art. 9º, caput); edital: 24 meses, prorrogável até 48 (itens 1.2 e 8.4) | — | n/a | **não** — proposta P11, D-33 | texto ausente |
| Alertas T-10/T-5/T-1 | Omisso | `alertas: [10, 5, 1]` | sem artigo: escolha do produto (RN37) | sim | texto ausente |

### 2.6 Roteiro de sessão

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Periodicidade das reuniões ordinárias | Semanal, em dia fixo, período matutino ou vespertino, conforme organização do Coordenador com a entidade executiva (art. 14, caput) | — (só `distribuicao.periodicidade`) | n/a | **não** — proposta P2, D-29 | texto ausente |
| Ordem dos trabalhos | "Poderão obedecer, a critério de cada Presidente": I abertura; II ata anterior; III verificação da pauta, distribuição interna e composição das turmas pelo sistema; IV distribuição aos membros presentes; V decisão pelas turmas; VI sugestões e proposições; VII encerramento (art. 15) | 7 passos na mesma ordem | sim | sim | texto ausente |
| Passos obrigatórios | III e IV são compulsórios; a omissão impede a reunião e cancela a presença de quem se omitiu (art. 15, par. único) | III e IV `obrigatorio: true`; também abertura, julgamento e encerramento | parcial: a obrigatoriedade de I, V e VII é escolha do produto (D-07); o cancelamento de presença do par. único não tem chave (só `presenca.cancelaPorRecusaImotivada`, do art. 14, §2º) | parcial — ver P1 | texto ausente |
| Executor da distribuição interna | Presidente, no horário de início, pelo sistema (arts. 15, III, e 25, II); a Secretaria assiste (art. 29, V) | `executorDistribuicaoInterna: PRESIDENTE` | sim | sim | texto ausente |
| Relatório da distribuição interna | Impresso pelo sistema à Secretaria ao final dos trabalhos (art. 16, par. único) | — (doc 07, seção 4.9) | n/a | sim (comportamento fixo do doc 07) | texto ausente |
| Substituição de ausente | Por membro presente de representação equivalente, no 2º ou 3º voto (art. 17, §4º) | `substituicaoAusente: MESMO_SEGMENTO_NO_2O_OU_3O_VOTO` | sim | sim | texto ausente |
| Processos não julgados | Secretaria recoloca na pauta os não julgados, retirados e os que voltam de diligência (art. 29, IV) | `relatorAusente: RETORNA_A_PAUTA` | parcial: a regra do relator ausente é leitura do art. 29, IV — já é D-10 | sim | texto ausente |
| Rodízio de turmas | Alternadas a cada reunião; composição só se repete depois de esgotadas as outras (art. 17, §3º) | `rodizio: SEM_REPETICAO_ATE_ESGOTAR` | sim | sim | texto ausente |
| Presidente e vice em turmas diferentes | Omisso (D-02). Observação: como a exceção de maioria simples exige presidente ou vice na turma (art. 6º, §3º), presidente e vice na mesma turma impedem a exceção na outra | `presidenteEViceEmTurmasDiferentes: false` | sem artigo — já é D-02 | sim | texto ausente |
| Sustentação oral | Não admitida (art. 20, caput) | `sustentacaoOral: false` | sim | sim | texto ausente |
| Modalidade | Omisso. O texto pressupõe reunião nas instalações: vedação de retirar processos (art. 19), livro de presença (art. 27, II), acesso às instalações fora das reuniões (art. 29, XIII) | `modalidades: [PRESENCIAL, HIBRIDA]` | sem artigo para `HIBRIDA` — D-25 | sim | texto ausente |
| Reuniões extraordinárias | Previstas: presença computada (art. 14, §1º); solicitadas por membro (art. 27, VIII); convocadas pelo Coordenador por aumento de recursos não julgados (art. 28, III); sessão especial de presidentes (art. 28, IV) | — | n/a | **não** — proposta P3, D-29 | texto ausente |
| Reunião plenária mensal | Convocada e dirigida pelo Coordenador, roteiro próprio (abertura e mesa; ata anterior; ordem do dia), presença computada (art. 21) | — | n/a | **não** — proposta P4, D-30 | texto ausente |
| Votação | Relator relata por escrito e motiva (arts. 16 e 27, VI); demais membros discutem e decidem, justificando se divergem ou acompanhando (art. 27, VII); autonomia de cada membro (art. 18) | `ordem: SEQUENCIAL`, `votoRelatorVisivelAposConclusao: true`, `exigeDispositivoNormativo: true` | parcial: ordem sequencial é D-04; o regimento exige **motivação**, e a vinculação a dispositivo normativo vem do enunciado (RN07), não do regimento | sim | texto ausente |

### 2.7 Resultados possíveis

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Rol | a) rejeição administrativa do recurso; b) não conhecimento por intempestividade; c) não conhecimento por ilegitimidade de parte; d) manutenção da penalidade; e) cancelamento da penalidade (art. 27, VI) | 5 códigos, mesma ordem | sim | sim | texto ausente |
| Altera penalidade | O regimento não qualifica os resultados | só `CANCELAMENTO_PENALIDADE` com `alteraPenalidade: true` | sem artigo, mas decorre do sentido de cada resultado | sim | texto ausente |
| Provimento parcial | Não previsto no rol (art. 27, VI) | ausente | sim | sim | texto ausente |

### 2.8 Publicidade

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Nomes dos julgadores após o julgamento | Omisso | `revelarNomesAposJulgamento: true` PROVISÓRIO | sem artigo — já é D-09 | sim | texto ausente |
| Edital de pauta | Omisso. O doc 07, seção 6, prevê "edital de pauta com antecedência mínima configurável", sem chave no doc 06 | — | n/a | **não** — proposta P13, D-35 | texto ausente |
| Estatísticas e relatórios | Coordenador apresenta estatísticas mensais e relatório anual à entidade executiva (art. 28, XII) | — | n/a | **não** — proposta P13, D-35 | texto ausente |
| Dever de reserva dos membros | Divulgar sem autorização informação interna ou de processo em tramitação é hipótese de perda de mandato (art. 12, VII) | — | n/a | sim (comportamento fixo: invariante 1, RN29) | texto ausente |
| Comunicação ao CETRAN | Criação, extinção de juntas e designação de membros comunicadas ao CETRAN/SP (art. 5º, par. único) | — | n/a | sim (evento de integração; fora do regimento do produto) | texto ausente |

### 2.9 Sigilo da designação

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Modo | A Secretaria não fornece, e zela para que não se forneçam, informações sobre os recursos e sua distribuição a membro, presidente, funcionário ou empregado antes da reunião da junta (art. 29, XII) | `designacao.modo: SIGILOSO` | sim. O regimento protege contra membros e servidores; o produto estende a todos os perfis (invariante 1), o que é mais restritivo e compatível | sim | `SIGILOSO`, marcado "A CONFIRMAR; padrão do produto" no `curitiba.yaml`: não é fato da norma; texto ausente |
| Acesso dos membros fora da reunião | A Secretaria não permite acesso imotivado de pessoas, inclusive membros e presidentes, às instalações fora dos dias e horários de reuniões, salvo autorização expressa do Coordenador (art. 29, XIII) | `autos.acessoMembrosForaDaSessao: MEDIANTE_AUTORIZACAO_COORDENADOR` (PT-03; antes `PERMITIDO`, D-01) | sim, desde o PT-03 (D-26) | sim (P6 feita no PT-03) | texto ausente |
| Retirada de autos | Vedada a retirada de processos das instalações (art. 19) | `autos.downloadMembros: false` | sim, por analogia (download equivale a retirada) | sim | texto ausente |
| Marca d'água | Omisso | `autos.marcaDagua: true` | sem artigo: controle do produto (ADR-0010) | sim | texto ausente |
| Verificação da distribuição pelos membros | Cada membro verifica anomalias na distribuição para sua turma antes de relatar (art. 27, II), a distribuição interna e as turmas (art. 27, III), a sequência de distribuição (art. 27, VI e VII), e comunica por escrito ao Coordenador, via Secretaria, anomalia não sanada (art. 27, XII) | — | n/a | **não** — proposta P9, D-31 | texto ausente |

### 2.10 Distribuição

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Periodicidade | Processamento eletrônico semanal (art. 22, caput); o período pode ser alterado excepcionalmente com justificativa técnica (art. 22, §2º) | `periodicidade: SEMANAL` | sim. A alteração do §2º é proposta da Administração com justificativa e vigência futura (doc 18; RN38, RN39), nunca rodada avulsa | sim | texto ausente |
| Equidade | Igual número de processos por membro (art. 22, caput); equitativa aos julgadores na distribuição interna (art. 16) | `equidade: POR_POSICAO` | parcial: o texto fala em membro; posição é o substituto do membro no modo sigiloso | sim | texto ausente |
| Conexão | Mesmo requerente ou mesmo veículo, decididos pela mesma turma e distribuídos ao mesmo membro (art. 22, caput); "por veículo ou recorrente" na distribuição interna (art. 16) | `conexao: [MESMO_REQUERENTE, MESMO_VEICULO]` | sim | sim | texto ausente |
| Conexo com pendente de semana anterior | Omisso | `conexaoComPendentes: MESMA_POSICAO` PROVISÓRIO | sem artigo — já é D-03 | sim | texto ausente |
| Ordem | Cronológica de interposição, obedecida a distribuição (art. 23) | `ordemNaPosicao: DATA_INTERPOSICAO` | sim | sim | texto ausente |
| Falha | Ocorrência excepcional ou indisponibilidade técnica suspende a distribuição; os recursos do período entram na semana subsequente (art. 22, §1º) | `falhaFechada: true` | sim | sim | texto ausente |
| Redistribuição entre juntas | Vedada, salvo força maior, impedimento ou suspeição dos membros (art. 24, caput), por critérios pré-estabelecidos pelo Coordenador, documentados e anotados nos autos (art. 24, par. único) | `redistribuicao: { motivosPermitidos: [FORCA_MAIOR, IMPEDIMENTO, SUSPEICAO], exigeCriterioDoCoordenador: true }` | sim | sim | texto ausente |
| Organização da distribuição | Coordenador organiza e supervisiona a distribuição pela Secretaria (art. 28, VII); Secretaria prepara e coloca os processos em distribuição "conforme orientações do Coordenador" (art. 29, III) | — | n/a: ver seção 4 (leitura compatível com a invariante 2) | sim (fixo: só o job distribui) | texto ausente |
| Planejamento mensal de distribuição interna | Condicionado à distribuição eletrônica (art. 30) | — | n/a — já é D-08 | sim (não implementar) | texto ausente |

### 2.11 Impedimentos

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Impedimento (dever) | "Deverá se declarar impedido" quando: a) é o apenado, ou o apenado ou condutor é seu parente; b) atuou como testemunha; c) foi perito ou produziu prova determinante; d) orientou, instruiu ou ajudou o recorrente a produzir provas (art. 27, §1º) | 4 motivos `tipo: IMPEDIMENTO` | sim | sim | texto ausente |
| Suspeição (faculdade) | "Poderá se declarar suspeito" quando: a) amigo ou inimigo íntimo do recorrente ou do proprietário; b) credor ou devedor deles (art. 27, §2º) | 2 motivos `tipo: SUSPEICAO` | sim | sim | texto ausente |
| Cláusula geral | Declarar impedimento ou suspeição em processos em que tenha, direta ou indiretamente, interesse (art. 27, X) | rol fechado dos §§1º–2º | parcial: não está claro se o inciso X abre motivo fora do rol — D-32 | sim | texto ausente |
| Alegação imotivada | Alegar imotivada e injustificadamente suspeição ou impedimento é hipótese de perda de mandato (art. 12, X) | — (RN34 contabiliza declarações por membro) | sim (o contador da RN34 é a evidência) | sim | texto ausente |
| Impedimento objetivo na distribuição | Não está no regimento; vem do enunciado (RN06) | — | n/a | sim (fixo) | texto ausente |

### 2.12 2ª instância

| Item | São Paulo (norma e artigo) | sp.yaml | sp.yaml confere? | Doc 06 comporta? | Curitiba |
|---|---|---|---|---|---|
| Órgão | CETRAN/SP (arts. 5º, par. único, e 25, IX) | `orgao.segundaInstancia: CETRAN-SP` | sim | sim | CETRAN-PR — fonte: página institucional, a confirmar na norma |
| Quem instrui a remessa | O presidente da junta instrui os recursos contra as decisões da junta, conforme procedimentos do CETRAN/SP normatizados pelo Coordenador (art. 25, IX) | `pecas.RECURSO_2A_INSTANCIA: { decisor: CETRAN }` | parcial: decisor confere; o instrutor não tem chave | **não** — proposta P12, D-34 | texto ausente |
| Ciência dos membros | Membros têm conhecimento dos recursos julgados em 2ª instância quando disponibilizados pelo CETRAN (art. 27, V) | — | n/a | **não** — proposta P12, D-34 | texto ausente |
| Prazo | Omisso (CTB) | `recurso2a: 30 dias` | sem artigo no regimento — D-21 | sim | texto ausente |

## 3. Conferência do `sp.yaml`: valores sem sustentação no regimento ou divergentes

Seções que o regimento de SP **não regula** (valores vêm de outra norma ou de decisão do produto, e não foram objeto de conferência artigo a artigo): `orgao` (salvo 2ª instância), `modulos`, `identidade`, `calendario`, `pecas` (salvo o decisor do recurso à JARI, art. 4º, I), `relatoria.checklist` (RN13, do enunciado), `documentos`, `retencao`, `administracao` (salvo o ponto abaixo), `temporalidade`.

| Chave | Situação | Encaminhamento |
|---|---|---|
| `autos.acessoMembrosForaDaSessao` | Era `PERMITIDO`, divergente do art. 29, XIII. **Resolvido no PT-03:** valor `MEDIANTE_AUTORIZACAO_COORDENADOR` | D-26 (revisão da D-01); P6 feita |
| `administracao.aprovadores` e doc 18 (`COORDENADOR` incompatível com `MEMBRO`) | **Diverge** do art. 28, caput (o coordenador é um dos membros das juntas) | D-27; proposta P7 |
| `administracao.aprovadores.REGIMENTO: [COORDENADOR]` | Sem artigo: o regimento é ato da autoridade de trânsito (preâmbulo, art. 1º) e os casos omissos são dela (art. 31) | D-28; proposta P8 |
| `sessao.modalidades` com `HIBRIDA` | Sem artigo; o texto pressupõe reunião nas instalações (arts. 19, 27, II, 29, XIII) | D-25; comentário no `sp.yaml` |
| `sessao.quorumAbertura.minimoMembros: 3` | Confere com o art. 13, mas o art. 6º, §3º, admite turma abrindo com maioria simples | D-22 |
| `composicao.suplentesPorSegmento: 0` | Sem artigo: o art. 6º, §4º, permite suplentes e não fixa número | D-24; comentário no `sp.yaml` |
| `prazos.*` legais (`recurso1a`, `indicacaoCondutor`, `recurso2a`, `cienciaPresumidaEletronica`, `julgamento`) | Sem artigo no regimento; citam CTB e CONTRAN, cujo texto não está no repositório | D-21 |
| `prazos.exigencia`, `prazos.informacaoAgente`, `prazos.relatoria`, `designacao.revelarNomesAposJulgamento`, `distribuicao.conexaoComPendentes`, `turmas.presidenteEViceEmTurmasDiferentes`, `votacao.ordem`, `autos.acessoMembrosForaDaSessao` | PROVISÓRIOS já registrados | D-13, D-06, D-09, D-03, D-02, D-04, D-01 |
| `votacao.exigeDispositivoNormativo: true` | Regimento exige motivação (arts. 16, 27, VI); o dispositivo normativo vem do enunciado (RN07) | Mantido; mais restritivo que o texto, sem conflito |
| `autos.marcaDagua`, `prazos.alertas`, letras A–F | Escolhas do produto | Mantidos |

Nenhum valor do `sp.yaml` foi alterado nesta análise. Foram acrescentados só comentários apontando para este documento e para as novas `D-xx`. Depois dela, o PT-03 mudou um valor (`autos.acessoMembrosForaDaSessao`, D-26) e completou os comentários: toda linha de valor cita artigo, norma externa ou `D-xx` (valores sem artigo nem dúvida própria citam a D-47).

## 4. Conflitos com invariantes

**Nenhum conflito direto** entre o regimento de SP e as invariantes do `CLAUDE.md`. Verificado:

| Invariante | O que o regimento diz | Conclusão |
|---|---|---|
| 3. Sem voto de qualidade | Nenhum artigo dá peso, desempate ou voto de minerva. A exceção de maioria simples exige a **presença** do presidente ou vice (arts. 6º, §3º, e 17, §1º), não peso maior | Sem conflito. Com 2 votos divergentes, o processo volta à pauta (doc 07, seção 7.3) |
| 2. Sem distribuição manual | Distribuição por processamento eletrônico (arts. 15, III, 16, 22, 25, II); suspensão em caso de falha (art. 22, §1º) | Sem conflito. Pontos de atrito abaixo |
| 7. Sem pagamento | O regimento não trata de pagamento | Sem conflito |
| 5. Nada é apagado | Não há previsão de exclusão; "numerando e rubricando as suas folhas" (art. 29, II) é prática de autos em papel | Sem conflito |
| 10. Sem decisão automática por IA | Nenhuma previsão de IA. O "sistema de processamento de dados" só distribui e forma turmas | Sem conflito |
| 1. Sigilo da designação | Art. 29, XII, impõe sigilo antes da reunião | Reforça a invariante |

**Pontos de atrito** (não adaptados; a leitura adotada preserva a invariante e deve ser confirmada com a CET):

1. **Art. 29, III, e art. 28, VII.** A Secretaria "prepara e coloca os processos em sua distribuição para as Juntas ... conforme orientações do Coordenador", e o Coordenador "organiza e supervisiona a distribuição". Lidos isoladamente, permitiriam colocação manual. Leitura adotada: a Secretaria prepara (instrução, admissão) e o job semanal distribui (art. 22, caput). O sistema não oferece rota de colocação manual (invariante 2, RN24).
2. **Art. 24, par. único.** Redistribuição "segundo critérios pré-estabelecidos pelo Coordenador". Leitura adotada (doc 07, seção 8): o critério é cadastrado antes, versionado e executado pelo sistema com nova semente; nunca escolha de destino por pessoa.
3. **Art. 22, §2º.** Alteração excepcional do período de distribuição. Leitura adotada: mudança de `distribuicao.periodicidade` por proposta da Administração, com justificativa e vigência futura (RN38, RN39); nunca distribuição avulsa.
4. **Art. 28, III.** Reuniões extraordinárias por acúmulo de recursos não julgados. Não podem antecipar a revelação de lote selado nem disparar distribuição fora do job (D-29, proposta P3).
5. **Art. 9º, VI, e art. 15, par. único.** "Cancelamento automático da indicação" e "cancelando a presença" são efeitos jurídicos, não decisões de sistema. O sistema registra o ato de uma pessoa identificada (RN30, RN36), sem ação automática.

## 5. Parâmetros novos propostos

Nenhum destes parâmetros foi incluído no doc 06 nem no `sp.yaml`, salvo a P6 (D-26 já mandava fazer no PT-03): dependem de decisão do orquestrador. Cada um tem `D-xx` no doc 16. Tipos seguem a notação do doc 06.

| Id | Chave proposta | Tipo e valores | Valor para SP (artigo) | D-xx |
|---|---|---|---|---|
| P1 | `sessao.quorumAbertura.exigePresidenteOuVice` | boolean | `true` (arts. 15, par. único, 25, II e III, 26, I) | D-23 |
| P2 | `sessao.agenda` | `{ periodicidade: SEMANAL \| QUINZENAL \| MENSAL, diaFixo: boolean, turnos: [MATUTINO, VESPERTINO, NOTURNO] }` | `{ SEMANAL, diaFixo: true, turnos: [MATUTINO, VESPERTINO] }` (art. 14, caput) | D-29 |
| P3 | `sessao.extraordinaria` | `{ permitida: boolean, convocadaPor: [papéis], pauta: SOMENTE_JA_REVELADOS \| LOTE_DA_SEMANA }` | `{ true, [COORDENADOR], SOMENTE_JA_REVELADOS }` (arts. 14, §1º, 27, VIII, 28, III); `pauta` sem artigo, provisório | D-29 |
| P4 | `plenaria` | `{ periodicidade, convocadaPor, antecedenciaConvocacao: {dias}, antecedenciaCopiaAta: {dias}, roteiro: [passos], contaPresenca: boolean }` | `{ MENSAL, COORDENADOR, 7, 14, [ABERTURA_E_MESA, APROVACAO_ATA_ANTERIOR, ORDEM_DO_DIA], true }` (art. 21; "uma semana" e "duas semanas" convertidas em 7 e 14 dias) | D-30 |
| P5 | `mandato.perda.janelaIntercaladas` e `mandato.perda.reunioesContadas` | `{ meses: int, inicio: POSSE \| ANO_CIVIL }`; lista de `ORDINARIA \| EXTRAORDINARIA \| PLENARIA` | `{ 12, POSSE }`; `[ORDINARIA, PLENARIA]` (art. 12, II) | D-30 |
| P6 (feita no PT-03) | novo valor em `autos.acessoMembrosForaDaSessao` | `PERMITIDO \| MEDIANTE_AUTORIZACAO_COORDENADOR \| PROIBIDO` | `MEDIANTE_AUTORIZACAO_COORDENADOR` (art. 29, XIII) | D-26 |
| P7 | `administracao.coordenadorAcumulaMandato` | boolean | texto: `true` (art. 28, caput); provisório: `false` (D-18) | D-27 |
| P8 | `administracao.exigeAtoNormativo` | lista de tipos de proposta | `[REGIMENTO]` (preâmbulo e art. 1º: o regimento é Comunicado da autoridade; art. 31) | D-28 |
| P9 | `relatoria.verificacaoDistribuicao` | `{ obrigatoriaAntesDeRelatar: boolean, canalAnomalia: SECRETARIA_PARA_COORDENADOR \| COORDENADOR }` | `{ true, SECRETARIA_PARA_COORDENADOR }` (art. 27, II, III, VI, VII e XII) | D-31 |
| P10 | `composicao.requisitosMembro` | `{ escolaridadeMinima: FUNDAMENTAL \| MEDIO \| SUPERIOR, vedacoes: [códigos] }` | `{ MEDIO, [MENOR_DE_IDADE, VINCULO_CRT_CFC_DESPACHANTE_OU_CREDENCIADO, AGENTE_FISCALIZACAO_OU_CHEFIA, CNH_SUSPENSA_OU_CASSADA, MEMBRO_CETRAN_CONTRANDIFE_OU_OUTRA_JARI] }` (arts. 6º, I–III, e 8º, I–V) | D-33 |
| P11 | `credenciamento` | `{ validadeProcedimento: {anos}, tempoMinimoNoMunicipio: {anos}, ordemDeDesignacao: SORTEIO_PUBLICADO, sorteioNoSistema: boolean }` | `{ 2, 5, SORTEIO_PUBLICADO, false }` (art. 9º, caput, II, a, e IV); `sorteioNoSistema` sem artigo, provisório | D-33 |
| P12 | `pecas.RECURSO_2A_INSTANCIA.instrutor` e `segundaInstancia.cienciaAosMembros` | papel (`PRESIDENTE_JUNTA \| SECRETARIA \| COORDENADOR`); boolean | `PRESIDENTE_JUNTA` (art. 25, IX); `true` (art. 27, V) | D-34 |
| P13 | `publicidade` | `{ editalPauta: { publicar: boolean, antecedencia: {dias}, incluiDesignacao: boolean }, estatisticas: { periodicidade, destinatario }, relatorioAnual: boolean }` | `editalPauta`: omisso, provisório `{ false, null, false }`; `estatisticas: { MENSAL, ENTIDADE_EXECUTIVA }`, `relatorioAnual: true` (art. 28, XII) | D-35 |

Regras de consistência sugeridas (numeradas a partir das 11 atuais do doc 06):

12. `publicidade.editalPauta.incluiDesignacao: true` só é aceito com `designacao.modo: ABERTO` (invariante 1).
13. Com `designacao.modo: SIGILOSO`, `sessao.extraordinaria.pauta` só aceita `SOMENTE_JA_REVELADOS` (nenhuma sessão antecipa a revelação de lote selado).
14. `votacao.excecaoMaioriaSimples.exigePresidenteOuVice: true` exige `sessao.quorumAbertura.exigePresidenteOuVice: true`.

## 6. Inconsistências internas do texto de SP

Registradas para não serem "corrigidas" em silêncio no código. Nenhuma muda valor do `sp.yaml`.

| Artigo | Texto | Observação |
|---|---|---|
| Art. 9º, caput | Remete ao "art.5º inciso III" para a indicação por associações | A indicação por associações está no art. 6º, III; o art. 5º, III, trata de nomeação |
| Art. 9º, §3º | Duração dos mandatos "prevista no artigo 10" | O mandato está no art. 11; o art. 10 trata da posse |
| Art. 29, caput | "Recursos humanos mencionados no art.4º, inc. V" | Os recursos humanos estão no art. 5º, V |
| Art. 17, §1º x art. 6º, §3º | "presidente ou de seu suplente" x "presidente ou vice-presidente" | Lido como vice (art. 26, I) — D-24 |
| Art. 13 x art. 6º, §3º | Junta se reúne com no mínimo 3; turma abre a sessão com maioria simples | D-22 |
| Art. 32 | Diz que Portarias foram revogadas por outra Portaria | Disposição declaratória; sem efeito no produto |

## 7. Curitiba: o que falta

Análise parada por falta do texto da norma (D-36). O que precisa entrar em `docs/fontes/regimentos/curitiba/` está no [README da pasta](../fontes/regimentos/curitiba/README.md). Fatos já registrados no `curitiba.yaml` (órgão, CETRAN-PR, 4 juntas, 6 titulares e 3 suplentes por junta, provedores de identidade do Paraná) vêm da página institucional e precisam ser confirmados na norma. Os valores do `curitiba.yaml` não foram alterados.

## 8. Como atualizar

- Ao receber o texto de um órgão, coloque-o em `docs/fontes/regimentos/<orgao>/` com cabeçalho de origem, preencha a coluna do órgão em cada tema e registre as `D-xx` novas.
- Ao decidir uma proposta da seção 5, siga "Como adicionar um parâmetro" (doc 06) e atualize a linha da `D-xx` correspondente.
