# Enunciado do Projeto — SIREJ
## Sistema Integrado de Recursos de Infrações de Trânsito (JARI)

**Versão:** 1.0 (rito modelado contra o Regimento das JARIs vigente — Comunicado 007/23 — e o Edital nº 001/2026-JARI/CET)
**Data:** 14/09/2026
**Natureza:** Sistema web de processo administrativo eletrônico para o rito recursal de infrações de trânsito

---

## 1. Contexto

A Junta Administrativa de Recursos de Infrações (JARI) é o órgão colegiado previsto no art. 16 do Código de Trânsito Brasileiro (Lei nº 9.503/1997), que funciona junto a cada órgão ou entidade executivo de trânsito ou rodoviário e julga, em primeira instância administrativa, os recursos interpostos contra as penalidades por eles aplicadas. É o mecanismo que materializa, na esfera administrativa de trânsito, as garantias constitucionais do contraditório e da ampla defesa (art. 5º, LV, da CF).

Na prática, o rito é composto por fases distintas e frequentemente confundidas pelo cidadão:

| Fase | Peça do cidadão | Quem julga | Prazo de interposição |
|---|---|---|---|
| Autuação | Defesa da autuação | Órgão/entidade autuador (não a JARI) — em SP, a Comissão de Defesa da Autuação (CDA) | Conforme prazo impresso na Notificação da Autuação; o prazo efetivamente divulgado varia entre órgãos e precisa ser **parâmetro**, não constante (ver Anexo A) |
| Identificação do condutor | Indicação do real infrator | Órgão autuador | 15 dias da NA (art. 257, §7º, CTB) |
| Penalidade — 1ª instância | Recurso à JARI | JARI | 30 dias do recebimento da Notificação de Penalidade (art. 285, CTB) |
| Penalidade — 2ª instância | Recurso ao CETRAN / CONTRANDIFE | Conselho estadual ou distrital | 30 dias da publicação/notificação da decisão da JARI (arts. 288 e 289, CTB) |

O processo é, hoje, majoritariamente **híbrido ou analógico** em boa parte dos órgãos: protocolo presencial ou por correio, autos físicos, distribuição manual a relatores, atas em papel ou planilha, publicação de pauta em PDF no portal do órgão e notificação da decisão por via postal. O cidadão raramente consegue responder a três perguntas simples: *em que fase está meu recurso, quem é o relator e quando ele será julgado*.

### 1.1 Escala do problema

O volume é significativo mesmo em uma única jurisdição: a JARI do Município de São Paulo, criada em 1973, opera hoje com **27 juntas que se reúnem semanalmente**, cada junta composta por seis membros organizados em escala de duas turmas de três, e historicamente processa na ordem de 18 mil recursos por mês. Cada processo envolve múltiplos documentos (AIT, imagens de fiscalização, comprovantes, procurações, laudos de aferição de equipamento), múltiplos atores e prazos legais peremptórios. Perder o prazo de julgamento tem consequência jurídica direta: não julgado o recurso em 30 dias por motivo de força maior, a autoridade que impôs a penalidade poderá conceder efeito suspensivo, o que viabiliza a regularização do veículo junto ao DETRAN enquanto o mérito não é decidido (art. 285, CTB).

### 1.2 Dores identificadas por camada

**Recorrente (cidadão, frotista, advogado):**
- Não sabe qual peça cabe em cada fase e protocola a peça errada, gerando não conhecimento por inadequação.
- Não tem visibilidade do andamento, do relator designado nem da data de sessão.
- Recebe a decisão por carta, com atraso, e frequentemente já com o prazo de 2ª instância correndo.
- Reenvia documentos que o próprio órgão já possui.

**Órgão autuador / autoridade de trânsito:**
- Retrabalho na juntada de informações do agente autuador e de provas do AIT.
- Dificuldade em cumprir prazos e em demonstrar a regularidade do procedimento quando judicializado.
- Controle de estoque e de backlog feito fora do sistema (planilhas).

**JARI (secretaria, relatores, presidentes, coordenação):**
- Distribuição manual, sem controle objetivo de carga, prevenção e impedimentos (é vedado julgar recurso quando o integrante lavrou o AIT).
- Produção de relatórios e votos sem base de precedentes pesquisável — decisões divergentes para casos idênticos.
- Elaboração de pauta, ata e resultado em processos paralelos e propensos a erro material (há casos públicos de redistribuição por erro na designação de relator).

**2ª instância (CETRAN / CONTRANDIFE):**
- Recebe autos incompletos ou desorganizados, gerando devolução em diligência.
- Não há trilha eletrônica única do processo entre instâncias.

---

## 2. Problema a resolver

> Não existe, para a maior parte dos órgãos do Sistema Nacional de Trânsito, uma plataforma única que conduza o processo administrativo de recurso de infração **de ponta a ponta** — do protocolo pelo cidadão até a decisão de segunda instância — com autos eletrônicos, controle automático de prazos legais, distribuição auditável, sessão de julgamento colegiada digital, publicidade ativa e notificação eletrônica integrada.

O resultado é um processo lento, opaco para o recorrente, caro para a administração e frágil quando levado ao Judiciário.

---

## 3. Objetivo

### 3.1 Objetivo geral

Desenvolver um **sistema web de processo administrativo eletrônico** que conduza integralmente o rito recursal de infrações de trânsito, atendendo simultaneamente ao recorrente, ao órgão autuador, à JARI e à instância superior, com autos 100% digitais, rastreabilidade completa e controle automatizado dos prazos previstos no CTB.

### 3.2 Objetivos específicos

1. Permitir que o recorrente protocole, acompanhe e seja notificado de todo o andamento por canal digital autenticado, sem deslocamento físico.
2. Automatizar o cálculo e a fiscalização de prazos (tempestividade da peça, prazo de julgamento, prazo recursal subsequente), com alertas antecipados.
3. Digitalizar o trabalho colegiado: triagem de admissibilidade, distribuição por relatoria, elaboração de relatório e voto, pauta, sessão, votação, ata e acórdão.
4. Garantir publicidade ativa (pautas, resultados e ementas) e produzir indicadores gerenciais confiáveis.
5. Padronizar a remessa e o retorno de processos entre 1ª e 2ª instâncias, e entre órgãos, em formato eletrônico.
6. Assegurar validade jurídica dos atos praticados no sistema (autenticação, assinatura eletrônica, carimbo de tempo, trilha de auditoria imutável).

### 3.3 Não objetivos (fora de escopo desta fase)

- Substituir o sistema de **processamento de multas** do órgão (lavratura de AIT, cálculo de valores, arrecadação, pontuação). O SIREJ consome e devolve dados a esse sistema, não o substitui.
- Fiscalização eletrônica, gestão de equipamentos medidores e aferição.
- Cobrança, parcelamento e emissão de guias de recolhimento.
- Peticionamento judicial ou integração com tribunais.
- Emissão de decisão por inteligência artificial. Recursos de IA, se existirem, ficam restritos a apoio (triagem, sugestão de precedentes, extração de dados), sempre com decisão humana e registro de quem decidiu.

---

## 4. Atores e camadas atendidas

### 4.1 Camada externa

| Ator | Descrição | Principais necessidades |
|---|---|---|
| **Recorrente pessoa física** | Proprietário do veículo ou condutor identificado | Protocolar, anexar provas, acompanhar, ser notificado, obter cópia dos autos |
| **Recorrente pessoa jurídica / frota** | Empresas com muitos veículos | Protocolo em lote, gestão por placa/CNPJ, delegação a prepostos, API |
| **Procurador / advogado** | Representante do recorrente | Cadastro de procuração, vinculação a múltiplos processos, painel de prazos |

### 4.2 Camada do órgão autuador

| Ator | Papel no fluxo |
|---|---|
| **Protocolo / atendimento** | Recepção de peças por canais não digitais, digitalização, autuação |
| **Setor de análise (defesa da autuação)** | Decide a defesa da autuação antes da imposição da penalidade |
| **Agente autuador** | Presta informações sobre as circunstâncias do AIT quando requisitado |
| **Autoridade de trânsito** | Concede efeito suspensivo, cumpre decisões, assina atos |

### 4.3 Camada JARI

| Ator | Papel no fluxo |
|---|---|
| **Secretaria da JARI** | Triagem de admissibilidade, saneamento, distribuição, pauta, ata, publicação, remessa |
| **Relator** | Lê os autos e elabora relatório e voto **motivado**; a decisão proposta é ou não acompanhada pelos demais |
| **Revisor** | Revê o processo relatado e profere o segundo voto da turma |
| **3º membro** | Completa a turma; seu voto define o resultado quando relator e revisor divergem (decisão por unanimidade ou 2×1) |
| **Membros da junta** | Composição mínima de 3 integrantes, com paridade entre representantes do órgão e de entidades da sociedade ligadas ao trânsito (Res. CONTRAN 357/2010). No modelo de São Paulo, cada turma reúne um membro de cada uma das três representações |
| **Entidade credenciada** | Associação ou órgão de classe habilitado por edital periódico a indicar membros para as juntas |
| **Presidente da junta** | Preside a sessão, proclama o resultado, assina a ata |
| **Coordenador das JARI** | Gestão administrativa do conjunto de juntas, parametrização, redistribuição |

### 4.4 Camada superior e sistêmica

| Ator | Papel |
|---|---|
| **CETRAN / CONTRANDIFE** | Julgamento em 2ª instância |
| **Outro órgão do SNT** | Recebe peça de competência alheia e a remete ao órgão competente (art. 287, CTB) |
| **Administrador do sistema** | Parametrização, perfis, auditoria, integrações |
| **Auditoria / controle interno / Ministério Público** | Acesso de consulta e extração de trilhas |

---

## 5. Escopo funcional

### 5.1 Módulo Portal do Recorrente

- Autenticação federada **gov.br** (níveis bronze/prata/ouro com exigência parametrizável por tipo de ato) e cadastro alternativo para não titulares de conta gov.br.
- Consulta de autuações e penalidades vinculadas ao CPF/CNPJ ou placa, com indicação **explícita da fase e da peça cabível** e do prazo restante.
- Assistente de peticionamento: formulário guiado por tipo de peça (defesa da autuação, indicação de real infrator, recurso 1ª instância, recurso 2ª instância, pedido de efeito suspensivo, juntada de documentos).
- Upload de anexos com validação de formato, tamanho, antivírus e conversão para PDF/A.
- Assinatura eletrônica da peça (assinatura avançada gov.br ou qualificada ICP-Brasil).
- Recibo de protocolo com número único, data/hora e hash dos documentos.
- **Linha do tempo do processo**: cada movimentação visível ao recorrente, com data, ato praticado e documento associado quando público.
- Notificações multicanal: painel, e-mail, push e, quando aderente, o sistema de notificação eletrônica (SNE/CDT).
- Vista integral dos autos e download do processo completo em PDF assinado.
- Área de procurações e representação, com validade e revogação.

### 5.2 Módulo Autuação e Instrução (órgão autuador)

- Recepção automática das peças protocoladas; recepção manual de peças em papel com digitalização e certificação de conformidade.
- Formação do **auto eletrônico**: juntada automática do AIT, imagens de fiscalização, certificado de aferição do equipamento, histórico de notificações e comprovantes de entrega.
- Fluxo de **informação do agente autuador** com prazo próprio e modelo estruturado.
- Análise e decisão da defesa da autuação (fase que precede a penalidade e não compete à JARI).
- Registro de concessão de efeito suspensivo pela autoridade, com reflexo imediato na situação do veículo/condutor no sistema de multas.
- Cumprimento de decisão: cancelamento da penalidade, conversão em advertência, restabelecimento, baixa de pontuação — sempre por integração com o sistema de multas, nunca por edição manual de saldo.

### 5.3 Módulo Secretaria da JARI

- **Triagem de admissibilidade assistida**: verificação automatizada de tempestividade (com base na data de ciência efetiva, incluindo a regra de ciência presumida em 30 dias após a inclusão no meio eletrônico, prevista na Res. CONTRAN 918/2022), legitimidade, representação, objeto e competência.
- Fluxo de **exigência/saneamento** com prazo, notificação e reinício automático da etapa.
- Remessa de peças de competência alheia ao órgão competente, com registro no RENAINF quando a infração for cometida fora da unidade de licenciamento do veículo.
- **Distribuição entre as juntas (art. 22 do Regimento)**: processamento eletrônico **semanal**, com igual número de processos por membro, respeitada a conexão de recursos do **mesmo requerente ou do mesmo veículo** — os conexos vão obrigatoriamente ao mesmo membro e à mesma turma.
- **Falha fechada, nunca manual**: havendo ocorrência excepcional ou indisponibilidade técnica, a distribuição é **suspensa**, e os recursos protocolados no período entram na ordem da semana seguinte. O sistema não pode oferecer distribuição manual de contingência — a indisponibilidade é um estado previsto pela norma, não um incidente a contornar.
- **Distribuição interna e formação das turmas (arts. 15 e 16)**: executadas pelo **presidente da junta, no início da reunião**, por meio do sistema. São atos compulsórios: sua inobservância impede a reunião. Ao final dos trabalhos, o sistema emite à Secretaria o relatório da distribuição interna e da formação das turmas, para controle e redação da ata.
- **Rodízio combinatório das turmas (art. 17, §3)**: as turmas são alternadas a cada reunião e a composição das duas turmas só pode se repetir depois de esgotadas todas as demais combinações possíveis. Com 6 membros (2 por segmento), há 4 partições distintas em duas turmas de um membro por segmento — logo, um ciclo mínimo de 4 reuniões antes de qualquer repetição. O algoritmo precisa manter esse histórico por junta.
- **Substituição por ausência (art. 17, §4)**: membro ausente é substituído por membro presente de representação equivalente na formulação do segundo ou do terceiro voto.
- **Ordem de julgamento (art. 23)**: cronológica por data de interposição, dentro da distribuição.
- **Redistribuição entre juntas (art. 24)**: vedada, salvo força maior, impedimento ou suspeição, por critérios pré-estabelecidos pelo Coordenador, documentados e anotados nos autos.
- **Selo de identificação do processo** gerado no cadastro, contendo número único no órgão, semana de interposição, junta sorteada, posição do membro e ordem na pauta (ex.: `2026-S22 / 14ª Junta / B / seq. 20`).
- **Sigilo da distribuição como dever regimental (art. 29, XII)**: é vedado à Secretaria fornecer informação sobre os recursos e sua distribuição a qualquer membro, presidente, funcionário ou empregado **antes da reunião**, sob pena de sanção funcional. No SIREJ isso é controle de acesso e trilha de auditoria sobre toda consulta à designação, não convenção de tela.
- Verificação automática de impedimento e suspeição, com registro do motivo de toda redistribuição.
- **Montagem da turma julgadora (art. 17)**: três membros de representações diferentes; excepcionalmente delibera com maioria simples, respeitada obrigatoriamente a presença do presidente ou de seu suplente; até duas turmas funcionam simultaneamente na mesma reunião.
- Montagem da **pauta** de sessão (ordinária e extraordinária), com prazo mínimo de publicação e geração do edital.
- Geração de **ata**, acórdão, ementa e certidão de julgamento.
- Controle de composição, quórum, mandatos, posses, substituições e suplências dos integrantes.
- Painel de backlog e alerta de processos próximos do prazo de 30 dias.

### 5.4 Módulo Relatoria e Julgamento

- Ambiente de trabalho do relator: autos navegáveis, visualizador de imagens do AIT, checklist de regularidade do procedimento de autuação e notificação.
- **Dossiê de contexto** apresentado junto aos autos, reproduzindo e ampliando o que hoje é impresso na capa do processo: cadastro do veículo, dados da autuação e da penalidade, histórico de defesas e recursos atrelados ao mesmo veículo, resultado do cálculo de tempestividade e ocorrências correlatas (remoção do veículo, por exemplo).
- Editor de **relatório e voto** com modelos, inserção de fundamentação normativa e vinculação obrigatória a um dispositivo legal e a uma tese de decisão.
- **Base de precedentes** pesquisável (por código de infração, tese, relator, resultado), para reduzir divergência entre juntas.
- Fluxo sequencial **relator → revisor → 3º membro**, com o voto do relator visível aos demais apenas após a relatoria estar concluída, e apuração automática do resultado (unanimidade ou 2×1).
- **Abertura de sessão** como ato formal que dispara o sorteio da etapa 2, registra presenças, verifica quórum e só então revela as designações aos membros.
- Assinatura individual do relatório pelo relator e do voto por cada um dos três membros — a responsabilização é nominal e o sistema não admite resultado sem as três manifestações assinadas.
- **Sessão de julgamento** em três modalidades: presencial, híbrida e virtual assíncrona (com prazo de votação), sempre com registro nominal de voto, declaração de voto divergente e proclamação do resultado.
- Resultados possíveis, conforme o rol do art. 27, VI, do Regimento — **não há provimento parcial**:
  1. rejeição administrativa do recurso;
  2. não conhecimento por intempestividade;
  3. não conhecimento por ilegitimidade de parte;
  4. manutenção da penalidade;
  5. cancelamento da penalidade.
- **Roteiro de reunião (art. 15)** implementado como fluxo guiado: abertura pelo presidente → leitura e aprovação da ata anterior → verificação da pauta, distribuição interna e composição das turmas pelo sistema → distribuição aos membros presentes → decisão pelas turmas → proposições → encerramento.
- **Declaração de impedimento e suspeição** pelo próprio membro, com os motivos tipificados no art. 27: impedimento quando for o apenado ou parente deste, quando tiver sido testemunha, quando tiver oficiado como perito ou produzido provas determinantes, ou quando tiver orientado o recorrente a produzir provas; suspeição por amizade ou inimizade íntima e por relação de crédito ou débito. O sistema registra a declaração, recompõe a turma e **contabiliza a frequência por membro**, já que alegação imotivada e reiterada é causa de perda de mandato.
- **Sem sustentação oral** (art. 20): o sistema não prevê audiência do recorrente ou de representante, e o peticionamento escrito é o único canal de manifestação.
- **Diligências** solicitadas pelo relator, com fluxo próprio e prazo. Diligência presencial para produção de prova exige no mínimo **dois membros de representações diferentes** — o sistema exige a designação da dupla e o registro conjunto do resultado.
- **Vedação de retirada dos autos das instalações da JARI** (art. 19): no meio digital, isso se traduz em visualização controlada, sem download dos autos pelos membros, com marca d'água identificadora e registro de cada acesso.
- Assinatura eletrônica da ata e do acórdão pelos membros e pelo presidente.
- Publicação automática do resultado no portal de transparência e disparo da notificação ao recorrente, abrindo o prazo de 2ª instância.

### 5.5 Módulo Segunda Instância

- Peticionamento do recurso ao CETRAN/CONTRANDIFE pelo mesmo portal, **sem exigência de comprovação de recolhimento do valor da multa** — o §2º do art. 288 do CTB foi revogado pela Lei nº 12.249/2010 e a exigência de depósito prévio para admissibilidade de recurso administrativo é inconstitucional (Súmula Vinculante 21 do STF).
- Juízo de admissibilidade e remessa eletrônica dos autos íntegros ao conselho, com índice de peças e verificação de integridade.
- Recepção do julgamento superior, cumprimento e encerramento do processo.
- Gestão de diligências determinadas pela instância superior.

### 5.6 Módulo Credenciamento e Composição

Fluxo hoje inteiramente em papel e presencial, mas que alimenta diretamente a capacidade de julgamento do órgão. As regras abaixo vêm do Edital nº 001/2026-JARI/CET e do art. 9º do Regimento:

- Publicação de **edital bienal de credenciamento**, com validade de 24 meses prorrogáveis até 48, ou encerramento antecipado caso se esgote a lista de classificação.
- Inscrição eletrônica da entidade com requerimento, ato constitutivo que comprove ligação com a área de trânsito, memorial circunstanciado, comprovação de regularidade perante o Município há no mínimo 5 anos e termo de responsabilidade assinado por dirigente qualificado.
- Verificação de habilitação com consulta ao CADIN Municipal: entidade inscrita não é credenciada, e a que passar a constar é **excluída automaticamente**, inclusive após a classificação.
- **Sorteio de classificação com regra de rateio**: o cadastro comporta até 60 entidades. Havendo mais de 60 credenciadas, sorteio público seleciona as 60 primeiras, cada uma indicando um postulante. Havendo menos de 60, cada entidade concorre a um número de postulações equivalente ao quociente de 60 pelo número de credenciadas (arredondado para cima), por sorteios sucessivos até completar as 60 postulações. A ordem resultante é publicada no Diário Oficial da Cidade.
- **Convocação por ofício** na ordem sorteada, à medida que surgem vagas. Não indicar no prazo equivale a desistência e implica **perda definitiva daquela posição**, sem direito a indicação tardia ou substitutiva.
- **Habilitação do postulante** com verificação automatizada dos requisitos e impedimentos: maioridade civil; ensino médio completo; certidão negativa de antecedentes criminais; ausência de vínculo com CRT, CFC, despachantes, escritórios de recursos administrativos e judiciais, médicos e psicólogos credenciados; não ser agente de fiscalização nem chefe imediato ou mediato destes; não ter direito de dirigir suspenso ou CNH cassada; não integrar CETRAN, CONTRANDIFE ou outra JARI; não exercer cargo ou função no Executivo ou Legislativo municipal; não constar do CADIN.
- **Duas provas como condição de posse**, com gestão de material didático, agendamento, aplicação e resultado:
  - prova de conhecimentos específicos de trânsito, com 10 questões de múltipla escolha sobre o CTB, o Regimento das JARIs e as responsabilidades da função, precedida de estudo autodidata com material disponibilizado pela autoridade de trânsito;
  - **teste prático de informática**, em que o candidato acessa o sistema com login e senha e redige, em editor de texto e com suas próprias palavras, um voto logicamente coerente de no mínimo dois parágrafos para um recurso fictício, a partir de um modelo.
- **Posse** condicionada à assinatura do Termo de Responsabilidade e do Termo de Posse, atos que devem preceder a primeira reunião da junta. Falta de assinatura ou desistência cancela a indicação e a entidade perde o direito à substituição.
- **Seleção dos representantes da comunidade** por processo de seleção conduzido pelo órgão, com inscrição pública, sorteio dos interessados e prova classificatória (o último ciclo em São Paulo teve 565 inscritos).
- Publicação obrigatória de nomeações e designações no diário oficial, e comunicação da criação, extinção de juntas e designação de membros ao CETRAN/SP.
- **Mandato de 1 ano**, com recondução a critério da autoridade, para a mesma ou outra junta. A duração do mandato independe da realização de novo procedimento bienal, e representantes de entidades não reinscritas permanecem até o fim do mandato — o sistema controla as duas linhas do tempo separadamente.
- **Perda de mandato** com as doze hipóteses do art. 12 parametrizadas, várias delas mensuráveis pelo próprio sistema: faltas injustificadas (3 seguidas ou 4 intercaladas em um ano), diligências despiciendas reiteradas, fundamentação reiteradamente incongruente, descumprimento de prazo de relatoria, divulgação não autorizada de informação sobre processos em tramitação e alegação imotivada de suspeição. As hipóteses que dependem de juízo exigem procedimento administrativo com ampla defesa — o sistema produz a evidência, nunca a sanção.
- **Capacitação continuada** (art. 28, XV): trilha de formação, atualização e reciclagem dos membros, com registro de participação.
- Registro de presença em reuniões semanais, plenárias e extraordinárias para efeito de gratificação, incluindo a regra de **cancelamento da presença** em caso de recusa imotivada do desempenho das atribuições.

### 5.7 Módulo Transparência e Indicadores

- Consulta pública de pautas, resultados e ementas, com dados pessoais minimizados.
- **Relatórios com cadência regimental (art. 28, XII)**: estatísticas de julgamento **mensais** à entidade executiva de trânsito e relatório anual de atividades das JARIs — gerados pelo sistema, não compilados à mão.
- **Retorno sistêmico à operação (art. 4º, III e art. 28, VI)** — funcionalidade de alto valor e ausente em praticamente todo sistema de recursos: quando um recurso aponta inadequação no registro da infração ou na sinalização viária, o relator marca o achado em campo estruturado (local, código de infração, tipo de falha). O sistema agrega esses achados, detecta **padrões recorrentes por ponto de fiscalização, trecho ou código de infração** e encaminha alerta à área de engenharia e à autoridade de trânsito. Uma JARI que cancela cem multas no mesmo cruzamento está reportando um defeito de sinalização, e hoje essa informação se perde processo a processo.
- Painéis gerenciais: estoque, tempo médio por etapa, taxa de tempestividade, taxa de provimento por código de infração, produtividade por junta e por relator, percentual julgado dentro do prazo legal, taxa de reforma em 2ª instância.
- Exportação para portais de dados abertos e relatórios periódicos ao CETRAN.

### 5.8 Módulo Demandas Judiciais e Informações Institucionais

Atribuições regimentais do presidente da junta e do coordenador que hoje tramitam por ofício e e-mail:

- Recebimento e controle de citações, intimações e determinações judiciais dirigidas à junta, com prazo, responsável e registro de cumprimento (art. 25, X).
- Produção assistida de **informações para subsídio de defesa judicial** do órgão, com extração automática do histórico completo do processo: notificações e comprovantes, tempestividade, distribuição, composição da turma, votos e assinaturas (arts. 25, XI e 27, XI).
- Instrução dos recursos de 2ª instância conforme os procedimentos do CETRAN (art. 25, IX).
- Consultas ao CETRAN e ao CONTRAN sobre interpretação da legislação, com registro das respostas em base consultável pelos membros (art. 28, IX).
- Divulgação aos membros dos atos editados pelos órgãos do Sistema Nacional de Trânsito (art. 28, X) e das decisões de 2ª instância disponibilizadas pelo CETRAN (art. 27, V) — insumo direto da base de precedentes.

### 5.9 Módulo Administração

- Perfis, papéis e permissões por escopo (junta, órgão, unidade).
- Parametrização de prazos, feriados, calendários de sessão, modelos de documento e regras de distribuição — **sem necessidade de deploy**.
- Trilha de auditoria imutável (append-only) de todo ato, com autor, IP, data/hora e hash.
- Gestão documental, temporalidade e arquivamento, respeitando o prazo prescricional quinquenal da pretensão punitiva.

---

## 6. Fluxo do processo (máquina de estados)

```
                 ┌──────────────┐
                 │  RASCUNHO    │  (peça iniciada pelo recorrente)
                 └──────┬───────┘
                        │ protocolar + assinar
                 ┌──────▼───────┐
                 │ PROTOCOLADO  │  número único + recibo + hash
                 └──────┬───────┘
                        │
                 ┌──────▼───────┐    incompleto    ┌──────────────┐
                 │  EM_TRIAGEM  ├─────────────────►│  EXIGÊNCIA   │
                 └──────┬───────┘◄─────────────────┴──────┬───────┘
          intempestivo/ │                 saneado         │ não atendida
          ilegítimo     │                                 ▼
                 ┌──────▼───────┐                  ┌──────────────┐
                 │  INADMITIDO  │                  │  ARQUIVADO   │
                 └──────────────┘                  └──────────────┘
                        │ admitido
                 ┌──────▼───────┐
                 │  INSTRUÇÃO   │  informação do agente + juntada de provas
                 └──────┬───────┘
                 ┌──────▼───────┐
                 │ DISTRIBUÍDO  │  relator designado (sorteio auditável)
                 └──────┬───────┘
                 ┌──────▼───────┐
                 │ EM_RELATORIA │  relatório + voto
                 └──────┬───────┘
                 ┌──────▼───────┐
                 │   PAUTADO    │  edital de pauta publicado
                 └──────┬───────┘
                 ┌──────▼───────┐    diligência    ┌──────────────┐
                 │EM_JULGAMENTO ├─────────────────►│ EM_DILIGÊNCIA│
                 └──────┬───────┘◄─────────────────┴──────────────┘
                        │ resultado proclamado
                 ┌──────▼───────┐
                 │   JULGADO    │  cancelamento / manutenção / não conhecimento
                 └──────┬───────┘  / rejeição administrativa
                 ┌──────▼───────┐
                 │  PUBLICADO   │  publicação + notificação → abre prazo 2ª inst.
                 └──────┬───────┘
              ┌─────────┴─────────┐
   recurso    │                   │  sem recurso no prazo
 ┌────────────▼──────┐     ┌──────▼───────┐
 │ REMETIDO_2A_INST. │     │  TRANSITADO  │
 └────────┬──────────┘     └──────┬───────┘
          │ decisão superior      │
 ┌────────▼──────────┐     ┌──────▼───────┐
 │ CUMPRIMENTO       ├────►│  ENCERRADO   │
 └───────────────────┘     └──────────────┘
```

Regra transversal: **todo estado registra data de entrada e prazo-alvo**. O motor de prazos recalcula o vencimento a cada movimentação e emite alertas em T-10, T-5 e T-1 dias, além de sinalizar processos que ultrapassaram 30 dias sem julgamento para avaliação de concessão de efeito suspensivo.

---

## 7. Regras de negócio essenciais

| # | Regra |
|---|---|
| RN01 | O prazo de recurso à JARI é de 30 dias contados do recebimento da Notificação de Penalidade (art. 285, CTB). A contagem parte da **ciência efetiva ou presumida**, registrada no processo com o respectivo comprovante. |
| RN02 | Na notificação por meio eletrônico, considera-se o interessado notificado 30 dias após a inclusão da informação no sistema eletrônico (Res. CONTRAN 918/2022). |
| RN03 | A defesa da autuação **não é julgada pela JARI**; o sistema deve impedir o encaminhamento indevido e orientar o cidadão sobre a peça correta. |
| RN04 | A indicação do real infrator observa o prazo de 15 dias contados do recebimento da NA (art. 257, §7º, CTB). |
| RN05 | É **vedado exigir pagamento ou depósito prévio** como condição de admissibilidade de qualquer recurso (SV 21/STF; revogação do art. 288, §2º, pela Lei 12.249/2010). |
| RN06 | Integrante que lavrou o AIT está impedido de julgar o respectivo recurso; integrante de JARI não pode compor CETRAN/CONTRANDIFE (Res. CONTRAN 357/2010). O sistema bloqueia a distribuição nesses casos. |
| RN07 | O colegiado decide por maioria, com composição mínima de 3 integrantes e número ímpar de votantes, cada voto com igual peso. Toda decisão é fundamentada e vinculada a dispositivo normativo. |
| RN08 | Não julgado o recurso em 30 dias por motivo de força maior, a autoridade que impôs a penalidade poderá conceder efeito suspensivo (art. 285, CTB). O sistema propõe a lista de processos elegíveis. |
| RN09 | Peça recebida por órgão incompetente é registrada, tem a data de recebimento preservada e é remetida ao órgão competente (art. 287, CTB), com registro no RENAINF quando aplicável. |
| RN10 | O prazo de recurso em 2ª instância é de 30 dias contados da publicação ou notificação da decisão da JARI (arts. 288 e 289, CTB). |
| RN11 | Nenhum documento juntado pode ser excluído; correções ocorrem por **desentranhamento registrado** ou juntada de nova peça, preservando a versão anterior. |
| RN12 | Toda decisão que altere a situação da penalidade dispara integração de cumprimento com o sistema de multas; não há alteração manual de pontuação ou débito dentro do SIREJ. |
| RN13 | O exame do recurso abrange três eixos, que o sistema apresenta ao relator como checklist: (a) regularidade do procedimento de registro e aplicação da penalidade; (b) existência de motivo de força maior ou necessidade alegado pelo recorrente; (c) antecedentes e comportamento do recorrente. |
| RN14 | Cada recurso é decidido por turma de três votos — relator, revisor e terceiro membro — resultando em unanimidade ou 2×1. O voto do relator é obrigatoriamente motivado. |
| RN15 | A **indicação do condutor** e a **transferência de pontuação** não são decididas pela JARI municipal; pessoa jurídica que deixa de indicar o condutor é passível da multa por não indicação (NIC), que se atrela ao veículo. O sistema roteia esses pedidos ao fluxo correto e não os admite como recurso. |
| RN16 | Pedido de **conversão da penalidade em advertência por escrito** é requerimento próprio, decidido pela autoridade, e não se confunde com defesa ou recurso. |
| RN17 | Pagamento e recurso são independentes: pagar não impede recorrer, e, havendo deferimento posterior, o sistema deve gerar o evento de **restituição** para o órgão fazendário, com acompanhamento visível ao recorrente. |
| RN18 | Descontos legais (pagamento antecipado até a data impressa na NP; percentual reduzido para quem adere ao SNE e renuncia a defesa e recurso) são apenas **exibidos** pelo sistema com o prazo correspondente; o cálculo permanece no sistema de multas. |
| RN19 | A distribuição entre juntas é **eletrônica, semanal e equitativa por membro**, respeitada a conexão de recursos do mesmo requerente ou do mesmo veículo, que vão ao mesmo membro e à mesma turma (art. 22). |
| RN20 | A identidade do relator, do revisor e do terceiro membro **não pode ser conhecida por ninguém antes da reunião** — inclusive pela Secretaria, que tem vedação expressa de fornecer essa informação sob pena de sanção funcional (art. 29, XII). Qualquer funcionalidade que antecipe a designação é falha de segurança, não recurso de transparência. |
| RN21 | A turma é composta por três membros de representações diferentes (comunidade, entidade executiva e sociedade civil), de modo que a representação do órgão de trânsito detém um único voto. Nenhum mecanismo de voto de qualidade, peso diferenciado ou desempate pelo órgão pode existir no sistema. |
| RN22 | A sessão abre com no mínimo três membros de representações diferentes; a deliberação excepcional por maioria simples exige obrigatoriamente a presença do presidente ou de seu suplente (arts. 13 e 17, §1). |
| RN23 | Todo ato de distribuição e toda decisão são registrados nos autos, e relatório e votos são assinados nominalmente. Não há decisão anônima nem resultado sem manifestação individual registrada. |
| RN24 | Indisponibilidade do sistema **suspende** a distribuição, e os recursos do período entram na semana seguinte (art. 22, §1). Não existe distribuição manual de contingência. |
| RN25 | As turmas se alternam a cada reunião e uma composição só se repete após esgotadas todas as demais combinações possíveis (art. 17, §3). O sistema mantém o histórico combinatório por junta. |
| RN26 | O rol de resultados é fechado: rejeição administrativa, não conhecimento por intempestividade, não conhecimento por ilegitimidade de parte, manutenção da penalidade e cancelamento da penalidade (art. 27, VI). **Não há provimento parcial.** |
| RN27 | Recursos são julgados em ordem cronológica de interposição (art. 23) e não podem ser redistribuídos entre juntas, salvo força maior, impedimento ou suspeição, sempre documentado nos autos (art. 24). |
| RN28 | Não se admite sustentação oral do recorrente ou de representante (art. 20). Diligência presencial para produção de prova exige no mínimo dois membros de representações diferentes. |
| RN29 | É vedada a retirada dos autos das instalações da JARI (art. 19). No meio digital: visualização controlada, sem download pelos membros, com registro de acesso. |
| RN30 | A alegação imotivada e injustificada de impedimento ou suspeição, o descumprimento de prazo de relatoria e a divulgação não autorizada de informação sobre processo em tramitação são hipóteses de perda de mandato (art. 12). O sistema **mede e evidencia**; a sanção depende de procedimento administrativo com ampla defesa. |

---

## 8. Requisitos não funcionais

**Volume e desempenho**
- Dimensionar para pico de 20 mil protocolos/mês por instalação, com folga de 3×.
- Resposta de consulta de andamento < 500 ms (p95); abertura dos autos com até 50 documentos < 2 s.
- Geração de pauta com 500 processos < 10 s.

**Disponibilidade e continuidade**
- Disponibilidade ≥ 99,5% em horário útil; janela de manutenção fora do horário de sessão.
- RPO ≤ 15 min, RTO ≤ 4 h. Backup com teste de restauração periódico.
- Degradação controlada: se a integração com o sistema de multas cair, o protocolo continua funcionando e a sincronização é enfileirada.

**Segurança e conformidade**
- LGPD (Lei 13.709/2018): minimização, base legal de tratamento (execução de política pública), registro de acesso a dados pessoais, anonimização na transparência ativa.
- Lei 14.063/2020 (assinaturas eletrônicas) e Lei 14.129/2021 (Governo Digital): vedação de exigir do cidadão documento que a administração já possui.
- Lei 9.784/1999 como norma subsidiária de processo administrativo.
- Trilha de auditoria imutável com encadeamento de hash; documentos com hash SHA-256 registrado no protocolo.
- Controle de acesso por papel e por escopo, MFA para perfis internos, segregação de funções entre secretaria e julgadores.

**Acessibilidade e usabilidade**
- WCAG 2.1 nível AA e eMAG; responsivo com uso majoritário em celular pelo cidadão.
- Linguagem simples no portal externo: o cidadão precisa entender a diferença entre defesa e recurso sem consultar advogado.
- **Perfil digital do back-office**: o requisito formal de informática exigido do membro de JARI é acessar o sistema com login e senha e redigir dois parágrafos em um editor de texto. Essa é a linha de base real do usuário interno — o ambiente de relatoria deve se aproximar de um editor de texto com apoio, não de um sistema processual denso. Nada de atalhos obrigatórios, fluxos multitela ou jargão de workflow.
- Sessão semanal em dia fixo, no período matutino ou vespertino, com dezenas de processos por membro: o caminho relatar → votar → próximo precisa ser o mais curto do sistema.

**Preservação**
- Documentos em PDF/A-2b, com política de temporalidade compatível com o prazo prescricional de 5 anos e com as normas de arquivo do ente.

---

## 9. Integrações

| Sistema | Direção | Conteúdo |
|---|---|---|
| **Sistema de multas do órgão** | bidirecional | AIT, notificações, penalidades, situação, cumprimento de decisão |
| **gov.br (OIDC)** | entrada | Autenticação e nível de confiabilidade da conta |
| **SNE / Carteira Digital de Trânsito** | saída/entrada | Notificação eletrônica e confirmação de ciência |
| **RENAINF** | bidirecional | Registro de recebimento de defesas e recursos e remessa ao órgão autuador competente em infrações fora da UF de licenciamento |
| **Diário Oficial eletrônico** | saída | Publicação de pauta, resultado e atos |
| **Assinador digital (ICP-Brasil / assinatura gov.br)** | saída | Assinatura de peças, atas e acórdãos |
| **Serviço de e-mail/push/SMS** | saída | Notificações não oficiais de cortesia |
| **Barramento do ente (ex.: SEI, ERP, BI corporativo)** | saída | Interoperabilidade documental e indicadores |

Princípio de integração: **antifrágil por padrão**. Toda chamada externa é assíncrona, idempotente, com retry exponencial, fila de mortos e reconciliação periódica. A indisponibilidade de terceiros nunca bloqueia o direito de petição do cidadão.

---

## 10. Arquitetura de referência sugerida

> Sugestão, não imposição. A decisão final depende do parque tecnológico do órgão contratante.

```
┌─────────────────────────────────────────────────────────────┐
│  Portal Recorrente (SPA)   │   Back-office JARI (SPA)       │
│  React + TypeScript, mobile-first, WCAG AA                  │
└───────────────┬─────────────────────────┬───────────────────┘
                │        API Gateway / BFF │  (OIDC gov.br, rate limit, WAF)
┌───────────────▼─────────────────────────▼───────────────────┐
│                     Núcleo de aplicação                     │
│  Go (ou Java/Spring) — Clean Architecture + DDD             │
│                                                             │
│  Contextos delimitados:                                     │
│   • Peticionamento   • Processo & Movimentação              │
│   • Prazos           • Distribuição                         │
│   • Julgamento       • Notificação                          │
│   • Documentos       • Publicidade & Indicadores            │
└───────┬───────────────┬──────────────┬──────────────────────┘
        │               │              │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────────────────────┐
│ PostgreSQL   │ │ Object      │ │ Broker de eventos          │
│ (dados +     │ │ Storage     │ │ (NATS / RabbitMQ / Kafka)  │
│  full-text)  │ │ (S3/MinIO)  │ │ integrações assíncronas    │
└──────────────┘ └─────────────┘ └────────────────────────────┘
        │
┌───────▼──────────────────────────────────────────────────────┐
│ Trilha de auditoria append-only + Redis (cache/sessão)       │
│ Observabilidade: OpenTelemetry, Prometheus, Grafana, Loki    │
└──────────────────────────────────────────────────────────────┘
```

**Decisões arquiteturais candidatas (a registrar como ADRs):**

- **Monólito modular antes de microsserviços.** O domínio é coeso e transacional; fragmentar cedo cria consistência eventual onde o processo exige atomicidade (protocolo + numeração + hash + recibo).
- **Event sourcing restrito ao processo.** A movimentação processual é naturalmente um log de eventos imutáveis; usar eventos como fonte de verdade do andamento e projeções para leitura resolve auditoria e linha do tempo de graça.
- **Motor de prazos como serviço isolado**, dirigido por calendário parametrizável (feriados nacionais, estaduais e municipais), pois é a regra que mais muda e a que mais gera litígio.
- **Documentos imutáveis e endereçados por hash**, com versionamento por juntada, nunca por sobrescrita.
- **Multi-tenant por órgão** desde o início: o mesmo produto deve servir a um município pequeno e a um DETRAN estadual, com isolamento lógico de dados e parametrização por tenant.

---

## 11. Modelo de dados macro

Entidades centrais (nomes indicativos):

- `orgao`, `junta`, `integrante`, `mandato`, `composicao_sessao`
- `veiculo`, `pessoa`, `procuracao`
- `infracao` (AIT), `notificacao` (NA/NP, com meio, data de envio e de ciência)
- `processo` (tipo, instância, origem, situação, prazos)
- `peca` (defesa, recurso, juntada, exigência) e `documento` (hash, mime, PDF/A, assinaturas)
- `movimentacao` (evento imutável: ator, ato, data/hora, documento)
- `distribuicao` (relator, critério, semente do sorteio, impedimentos avaliados)
- `sessao`, `pauta_item`, `voto`, `decisao`, `ementa`, `ata`
- `notificacao_saida` (canal, tentativa, comprovante, ciência)
- `integracao_evento` (payload, status, tentativas)
- `auditoria` (append-only encadeada)

---

## 12. Fases de entrega

| Fase | Escopo | Critério de conclusão |
|---|---|---|
| **F0 — Descoberta** | Mapeamento do rito real do órgão-piloto, regimento interno da JARI, inventário de integrações, levantamento de volumes | Fluxo validado e assinado pelos donos de processo |
| **F1 — MVP externo** | Portal do recorrente: consulta, protocolo assinado, anexos, recibo, linha do tempo, notificação | Cidadão protocola e acompanha um recurso real do início ao fim, sem papel |
| **F2 — Núcleo JARI** | Triagem, exigência, distribuição auditável, relatoria, pauta, sessão, votação, ata, acórdão, publicação | Uma junta julga uma sessão inteira exclusivamente no sistema |
| **F3 — Integrações e 2ª instância** | Sistema de multas, SNE, RENAINF, remessa ao CETRAN, cumprimento de decisão | Ciclo completo 1ª → 2ª instância → cumprimento, sem intervenção manual |
| **F4 — Escala e inteligência** | Multi-tenant, sessão virtual assíncrona, base de precedentes, painéis, dados abertos, apoio de IA à triagem | Segundo órgão em produção; indicadores publicados |

---

## 13. Critérios de aceite do projeto

1. Um recurso pode ser protocolado por celular, autenticado via gov.br, em menos de 5 minutos, sem envio de documento que a administração já possua.
2. O recorrente consegue responder, a qualquer momento e sem atendimento humano: fase atual, junta responsável, data prevista de sessão e prazo em curso. A identidade dos julgadores só é divulgada após o julgamento, conforme a política de publicidade do órgão.
3. Nenhuma distribuição ocorre por decisão humana: o sorteio é automático, a semente é selada e auditável a posteriori, e o resultado só se torna legível na abertura da sessão.
4. É impossível, por qualquer perfil do sistema, descobrir antecipadamente quem julgará um processo — e a tentativa de fazê-lo é detectável na trilha de auditoria.
5. Pauta, ata e acórdão são gerados pelo sistema, assinados eletronicamente e publicados sem redigitação.
6. O sistema identifica e reporta, diariamente, todos os processos que ultrapassaram o prazo legal de julgamento.
7. É possível reconstituir integralmente qualquer processo — inclusive quem viu, quem assinou e quando — a partir da trilha de auditoria.
8. Nenhuma tela ou regra exige pagamento como condição para recorrer.
9. Os autos completos podem ser exportados em pacote assinado, íntegro e verificável por terceiro.

---

## 14. Riscos e premissas

**Riscos**

| Risco | Impacto | Mitigação |
|---|---|---|
| Sistema legado de multas sem API | Alto | Camada anticorrupção com integração por arquivo/banco, isolada em adaptador substituível |
| Heterogeneidade regimental entre JARIs | Alto | Parametrização por tenant; regimento como configuração, não como código |
| Resistência cultural ao julgamento digital | Médio | Sessão híbrida na transição; piloto com uma junta voluntária |
| Judicialização por vício de notificação | Alto | Comprovante de ciência versionado e verificável em todo ato notificatório |
| Picos sazonais (fim de prazo, campanhas) | Médio | Fila de protocolo assíncrona com confirmação imediata de recebimento |
| Escopo invadir o processamento de multas | Médio | Fronteira explícita no contrato e nos ADRs |
| **Captura da distribuição** (tentativa de direcionar processo a julgador específico) | Crítico | Sorteio automático no cadastro, semente selada, revelação só na abertura da sessão, trilha de auditoria sobre toda consulta à designação |
| Funcionalidade de "conveniência" que fure o sigilo da designação | Alto | Requisito de segurança explícito em backlog e teste de regressão dedicado; revisão obrigatória de qualquer painel que exponha relator |
| Acervo histórico volumoso (em SP há processos digitalizados desde 1973) | Médio | Trilha de importação com metadados mínimos e busca; acervo legado em repositório separado, somente leitura |

**Premissas**

- Existe um órgão-piloto disposto a ceder acesso ao regimento interno da JARI e ao ambiente de homologação do sistema de multas.
- O ente possui ou pode aderir ao login gov.br e a um diário oficial eletrônico.
- O regimento interno da JARI do órgão-piloto está atualizado frente às diretrizes da Res. CONTRAN 357/2010.

---

## 15. Decisões em aberto (a definir com o patrocinador)

1. **Esfera de atuação:** municipal, estadual (DETRAN) ou órgão rodoviário? Isso define o volume, as integrações obrigatórias e a instância superior competente.
2. **Abrangência do rito:** o sistema cobre também a fase de defesa da autuação e a indicação de real infrator, ou apenas o recurso à JARI? A recomendação é cobrir todo o rito — é onde o cidadão mais erra.
3. **Relação com o sistema de multas:** integração ou substituição parcial? O enunciado assume integração.
4. **Sessão virtual:** no caso de referência, a resposta hoje é *não*. O Regimento vigente exige reunião semanal presencial em dia fixo, veda a retirada de processos das instalações da JARI (art. 19), restringe o acesso às instalações fora dos dias de reunião (art. 29, XIII) e atribui ao presidente a execução presencial da distribuição interna no início da reunião (art. 15). Sessão virtual assíncrona exige **alteração regimental prévia**, não apenas funcionalidade. A F4 deve começar por essa avaliação jurídica.
5. **Publicidade da designação:** a pauta publicada nomeia o relator (como faz o DETRAN-PB) ou preserva o sigilo da designação até a sessão (como faz São Paulo)? São duas leituras legítimas e incompatíveis do princípio da publicidade — ver Anexo A.5. É a decisão de maior impacto no desenho do sorteio e da transparência ativa.
6. **Produto ou solução interna:** haverá multi-tenant desde o início (produto vendável a vários órgãos) ou instalação única?

---

## 16. Anexo A — Diagnóstico do caso de referência (São Paulo)

Levantamento a partir dos canais públicos da Secretaria Municipal de Mobilidade Urbana e Transporte, da CET e do DSV. Serve como **linha de base do "como é hoje"** e como fonte dos requisitos acima.

### A.1 Estrutura do colegiado

- JARI criada em 1973; hoje **27 juntas com reunião semanal**.
- Cada junta: 6 membros, escalados em **2 turmas de 3**, cada turma com um representante de cada uma das três representações.
- Cada recurso recebe 3 votos: relator, revisor e terceiro membro. Decisão por unanimidade ou 2×1, sempre motivada pelo relator.
- Membros indicados por **entidades credenciadas por edital**, com processo de inscrição presencial em protocolo físico, janela de inscrição de cerca de um mês, sorteio de ordem de indicação e publicação no Diário Oficial da Cidade.
- Em 2ª instância julga o CETRAN-SP.
- **Inferência de dimensionamento:** o edital fixa o cadastro em até 60 entidades, cada uma indicando um postulante, e cada junta comporta 2 representantes de entidades. Isso sugere um teto da ordem de 30 juntas na estrutura atual — compatível com as 27 em operação. É um número a confirmar com o órgão, mas útil para dimensionar o sistema.

### A.2 Canais e sistemas existentes

| Sistema | Função atual | Limitação observada |
|---|---|---|
| **DSV Digital** | Peticionamento eletrônico da defesa da autuação e acompanhamento | Login próprio por CPF/CNPJ + RENAVAM, com ativação por e-mail; PJ depende de cadastro na Senha Web. Não usa gov.br |
| **Meu Veículo** | Consulta de infrações e 2ª via de notificação | Portal separado do peticionamento — o cidadão transita entre dois sistemas |
| **Correios / Caixa Postal** | Canal alternativo de protocolo | Autos físicos, digitalização posterior, data de recebimento controlada manualmente |
| **Portal institucional** | Publicação de editais, regimento, resultados | Conteúdo estático em PDF/XLS, sem consulta estruturada |

### A.3 Sintomas que o projeto deve eliminar

1. **Fragmentação de canais**: protocolar em um sistema, acompanhar em outro, consultar norma em um terceiro.
2. **Identidade proprietária**: cada órgão mantém seu login próprio em vez do gov.br, criando barreira de entrada e mais um cadastro a manter.
3. **Documentos que o Estado já possui**: exige-se do cidadão cópia de CRLV/CRV, documento com foto, CNPJ e contrato social — exatamente o que a Lei 14.129/2021 busca evitar. O SIREJ deve buscar esses dados nas bases oficiais e pedir apenas o que não puder obter.
4. **Formulário em planilha para download**: requerimento distribuído em `.xls` e alternativa de redação "de próprio punho" evidenciam ausência de peticionamento estruturado.
5. **Prazo de análise indefinido**: publica-se que a legislação não fixa prazo para avaliar a defesa da autuação. Mesmo sem prazo legal, o sistema deve estabelecer e publicar **prazo-meta interno** e medir o seu cumprimento.
6. **Vocabulário de resultado pouco compreensível**: categorias como "rejeitada administrativamente" chegam ao cidadão sem explicação acionável. O sistema deve acoplar a cada resultado o motivo e o próximo passo possível.
7. **Roteamento manual por competência**: solicitação de advertência, indicação de condutor e transferência de pontuação são frequentemente enviadas ao órgão errado. O assistente de peticionamento deve resolver isso na origem.
8. **Conteúdo desatualizado**: páginas de serviço ainda referenciam suspensão de atendimento presencial por pandemia e normas revogadas. Regras publicadas devem ser geradas a partir da **mesma parametrização** que o motor de prazos usa, e não redigidas à mão.

### A.4 Oportunidade de escopo confirmada

O rito completo vivido pelo cidadão em São Paulo inclui, além do recurso à JARI: defesa da autuação (CDA), indicação de condutor, multa NIC para pessoa jurídica, pedido de conversão em advertência, recurso ao CETRAN e restituição de valor pago em caso de deferimento. Tratar apenas o recurso à JARI resolveria menos da metade da jornada — o que reforça a recomendação do item 15.2.

### A.5 O mecanismo de impessoalidade — achado central da pesquisa

O documento técnico *"Segurança e transparência na JARI — Distribuição eletrônica de processos com sorteio da pauta"*, da própria coordenação da JARI-SP, descreve o desenho antifraude que sustenta o modelo. Ele merece ser tratado como **requisito, não como curiosidade histórica**, porque contraria o instinto de produto de "mostrar o máximo possível ao usuário".

**O problema que ele resolve.** A JARI tem poder de manter ou cancelar penalidade, e é justamente esse poder que a torna alvo de tráfico de influência e de tentativas de cancelamento indevido de multas. Qualquer sistema que digitalize a JARI herda esse alvo.

**Como o desenho responde.** A distribuição é eletrônica e aleatória no ato do cadastramento, distribuindo o recurso a uma das juntas e a uma posição de membro, com conexão entre recursos de penalidades atreladas a um mesmo veículo. Mas quem de fato julga — relator, revisor e terceiro membro — só é conhecido no início da reunião da junta, quando a pauta e a turma são sorteadas. O resultado prático: mesmo quem soubesse a junta de destino precisaria cooptar os seis membros daquela junta, e a rotatividade anual dos mandatos torna isso ainda menos viável.

**A capa do processo como estrutura de dados.** O sistema imprime na capa o número único do processo no órgão, a junta, uma letra de A a F correspondente à posição do membro e a ordem do processo na pauta daquela posição — no formato "22ª semana 2011 — 14ª Junta B sequência 20". Junto vêm os dados do veículo, da autuação e da penalidade, o histórico de defesas e recursos atrelados ao veículo, a aferição de tempestividade e ocorrências como remoção do veículo. É, na prática, a especificação do dossiê que o SIREJ deve montar.

**O equilíbrio de votos é deliberado — e estrutural, não protocolar.** O documento de 2011 registrava que presidente e vice eram o primeiro e o segundo membros da representação da comunidade. O Regimento vigente mudou isso: presidente e vice podem ser quaisquer membros, a critério da entidade executiva (art. 6º, §1). O equilíbrio que importa, porém, não depende da presidência — decorre da própria composição da turma, que reúne três membros de representações diferentes, deixando o órgão de trânsito com um voto contra dois da comunidade e da sociedade civil. Nenhuma funcionalidade do sistema pode desfazer esse arranjo.

**O sigilo deixou de ser boa prática e virou dever.** O que em 2011 era desenho operacional hoje está positivado: o Regimento proíbe expressamente a Secretaria de fornecer informação sobre os recursos e sua distribuição a qualquer membro, presidente, funcionário ou empregado antes da reunião, sob pena de sanção funcional (art. 29, XII), e torna compulsória a execução da distribuição interna pelo presidente no início da reunião, sob pena de a reunião não se realizar (art. 15, parágrafo único).

**Implicação de produto.** Há uma tensão real entre dois órgãos igualmente legítimos: o DETRAN-PB publica a pauta com o relator designado, em nome da publicidade e da transparência administrativa; São Paulo esconde essa informação até a abertura da sessão, em nome da impessoalidade. O SIREJ não deve escolher por eles: a **política de divulgação da designação precisa ser parâmetro do tenant**, e o modo sigiloso deve ser o padrão, por ser o mais restritivo e o que exige mais do desenho técnico (semente selada, controle de acesso, auditoria de consulta). Migrar do modo aberto para o sigiloso depois é caro; o contrário é trivial.

**Nota sobre o autor do desenho.** O próprio documento antecipava, já à época, que todo o procedimento de interposição, instrumentalização, informação, análise e decisão de recursos seria virtualizado, no mesmo caminho trilhado pelo Judiciário, e afirmava que a estrutura poderia ser transplantada para o novo formato. Este projeto é, em boa medida, a execução daquela previsão.

### A.6 Base normativa local vigente

| Norma | Conteúdo relevante |
|---|---|
| Decreto municipal nº 60.982/2021 | Designa a autoridade de trânsito e organiza as JARIs; composição de 6 membros em três segmentos; divisão em turmas de três; mandato de 1 ano com recondução; competências da CET; comunicação ao CETRAN/SP |
| **Comunicado nº 007/23, de 27/04/2023 (Aviso Geral nº 016/23) — Regimento das JARIs do Município de São Paulo** | Norma **em vigor**. Institui o regimento completo: atribuições, composição, impedimentos, indicação e posse, mandato e perda de mandato, reuniões, distribuição, turmas, resultados, competências de presidente, coordenador e Secretaria |
| Edital nº 001/2026-JARI/CET (Comunicado 018/26, Processo SEI 7410.2026/0008119-0) | Procedimento bienal de cadastramento e credenciamento de até 60 entidades; sorteio de classificação; requisitos e provas dos postulantes; posse e mandato |
| Decreto municipal nº 56.130/2015 e Código de Conduta e Integridade da CET | Conduta funcional exigida do membro; descumprimento é causa de perda de mandato |
| Lei federal nº 8.429/1992 | Expressamente invocada pelo Regimento quanto aos deveres do membro no exercício da função |

> **Correção relevante em relação à versão anterior deste enunciado.** O Regimento de 2005 (Portaria DSV.GAB nº 11/2005 e alterações de 2005 a 2011) **está revogado** desde 2023. O regimento vigente foi editado pela própria entidade executiva municipal de trânsito por Comunicado, e não por decreto ou portaria de secretaria — o que confirma, na prática, a tese central de parametrização: a norma que rege o rito pode ser substituída por ato infralegal de baixa formalidade, e já foi. **Regimento é configuração, não código.**

### A.7 Mapa de requisitos extraídos do Regimento vigente

Referência rápida para o backlog. Cada linha é um requisito verificável.

| Artigo | Requisito de sistema |
|---|---|
| Art. 4º, II | Solicitação de informações complementares à entidade executiva, com prazo e juntada automática |
| Art. 4º, III e 28, VI | Encaminhamento de achados sobre registro de infrações e sinalização viária; detecção de padrões recorrentes |
| Art. 6º, §2 e §3 | Turmas de três membros, um por segmento; abertura e deliberação com maioria simples e presença do presidente ou vice |
| Art. 8º e Edital 5.5 | Motor de verificação de impedimentos para ingresso, com fontes de comprovação |
| Art. 9º | Ciclo bienal de credenciamento com sorteio, convocação por ordem e perda por desistência |
| Art. 10 | Posse condicionada a dois termos assinados antes da primeira reunião |
| Art. 11 | Mandato de 1 ano com recondução; linha do tempo independente do ciclo bienal |
| Art. 12 | Doze hipóteses de perda de mandato, com métricas automatizáveis e procedimento com ampla defesa |
| Art. 13 e 17 | Quórum, composição da turma e até duas turmas simultâneas |
| Art. 14 | Reunião semanal em dia fixo; presença vinculada à gratificação; cancelamento de presença por recusa imotivada |
| Art. 15 | Roteiro de reunião como fluxo guiado; atos compulsórios do presidente |
| Art. 16 | Distribuição equitativa com conexão por veículo ou requerente; relatório de distribuição e turmas ao final dos trabalhos |
| Art. 17, §3 | Rodízio combinatório das turmas sem repetição até esgotar as possibilidades |
| Art. 17, §4 | Substituição de ausente por membro de representação equivalente |
| Art. 19 | Autos não saem das instalações: visualização controlada, sem download |
| Art. 20 | Sem sustentação oral; diligência presencial com dois membros de representações diferentes |
| Art. 21 | Plenária mensal com convocação em 1 semana e ata distribuída em 2 semanas |
| Art. 22 | Distribuição eletrônica semanal; suspensão em caso de indisponibilidade; sem contingência manual |
| Art. 23 e 24 | Ordem cronológica; vedação de redistribuição entre juntas, com exceções documentadas |
| Art. 25 | Painel do presidente: execução da distribuição, abertura e encerramento, assinatura de atas, justificativa de ausências, instrução de 2ª instância, demandas judiciais |
| Art. 27, VI | Rol fechado de cinco resultados |
| Art. 27, §1 e §2 | Tipificação de impedimento e suspeição declaráveis pelo membro |
| Art. 28, XII | Estatísticas mensais e relatório anual gerados pelo sistema |
| Art. 28, XV | Trilha de capacitação e reciclagem dos membros |
| Art. 29 | Funções da Secretaria: ordenamento e numeração de folhas, preparo da pauta, recolocação de processos retornados de diligência, registro de presenças, e o dever de sigilo do inciso XII |
| Art. 30 | O planejamento mensal de distribuição interna está **condicionado à implantação da distribuição eletrônica** — ou seja, a própria norma prevê função que só existe com o sistema em operação |

Observação de implementação: o texto regimental contém remissões internas imprecisas (o art. 9º remete ao "art. 5º, III" e o art. 29 ao "art. 4º, V", quando os dispositivos correspondentes são os arts. 6º e 5º). Vale registrar isso na modelagem para evitar interpretação equivocada e, se houver abertura, sugerir a correção ao órgão.

---

## 17. Glossário

| Sigla | Significado |
|---|---|
| **AIT** | Auto de Infração de Trânsito — documento que inicia o processo administrativo |
| **NA** | Notificação da Autuação — dá ciência ao proprietário da infração cometida |
| **NP** | Notificação da Penalidade — comunica a imposição da multa |
| **JARI** | Junta Administrativa de Recursos de Infrações — 1ª instância administrativa |
| **CETRAN / CONTRANDIFE** | Conselho Estadual de Trânsito / Conselho de Trânsito do DF — 2ª instância |
| **CONTRAN** | Conselho Nacional de Trânsito — normatiza o SNT |
| **SENATRAN** | Secretaria Nacional de Trânsito |
| **SNT** | Sistema Nacional de Trânsito |
| **SNE** | Sistema de Notificação Eletrônica |
| **CDT** | Carteira Digital de Trânsito |
| **RENAINF** | Registro Nacional de Infrações de Trânsito |
| **RENAVAM / RENACH** | Registros nacionais de veículos e de condutores habilitados |

---

## 18. Referências normativas e fontes consultadas

**Normas**
- Lei nº 9.503/1997 (CTB), arts. 16, 257, 280 a 290
- Lei nº 12.249/2010 — revoga o art. 288, §2º, do CTB
- Súmula Vinculante nº 21 do STF — depósito prévio em recurso administrativo
- Resolução CONTRAN nº 357/2010 — diretrizes para o regimento interno das JARI
- Resolução CONTRAN nº 918/2022 — procedimento de autuação, notificação e aplicação de penalidades
- Resolução CONTRAN nº 723/2018 — uniformização de procedimentos e prescrição
- Resolução CONTRAN nº 637/2016 — RENAINF
- Lei nº 9.784/1999 — processo administrativo
- Lei nº 13.709/2018 — LGPD
- Lei nº 14.063/2020 — assinaturas eletrônicas
- Lei nº 14.129/2021 — Governo Digital

**Fontes públicas consultadas**
- Resolução CONTRAN 918/2022 — https://www.gov.br/transportes/pt-br/assuntos/transito/conteudo-contran/resolucoes/Resolucao9182022.pdf
- Resolução CONTRAN 357/2010 — https://www.gov.br/transportes/pt-br/assuntos/transito/conteudo-contran/resolucao-contran-no-357-de-02-de-agosto-de-2010
- Decreto municipal (SP) nº 60.982/2021 — organiza as JARIs no Município de São Paulo
- **Comunicado nº 007/23, de 27/04/2023 (Aviso Geral nº 016/23) — Regimento das JARIs do Município de São Paulo** (norma vigente; revogou as Portarias DSV/SMT nºs 11/2005, 22/2005, 43/2006, 3/2008, 12/2008, 114/2008, 132/2009 e 43/2011)
- Edital nº 001/2026-JARI/CET — Comunicado 018/26, Aviso Geral nº 031/26, Processo SEI nº 7410.2026/0008119-0, com Anexos I (requerimento), II (termo de responsabilidade) e III (Regimento das JARIs)
- Decreto municipal (SP) nº 56.130/2015 — Código de Conduta Funcional dos Agentes Públicos
- Lei federal nº 8.429/1992 — improbidade administrativa, invocada pelo Regimento das JARIs
- RECHTER, Jaques Mendel. *Segurança e transparência na JARI — Distribuição eletrônica de processos com sorteio da pauta*. DSV/JARI/CET-SP — https://drive.prefeitura.sp.gov.br/cidade/secretarias/upload/chamadas/jari_1318612531.pdf
- Decreto nº 60.982/2021 (texto consolidado) — https://legislacao.prefeitura.sp.gov.br/leis/decreto-60982-de-30-de-dezembro-de-2021
- Regimento Interno da JARI-SP (página institucional) — https://www.prefeitura.sp.gov.br/cidade/secretarias/mobilidade/institucional/?p=5054
- Saiba como funciona a JARI — SMT/Prefeitura de São Paulo (atualizada em 31/07/2026) — https://prefeitura.sp.gov.br/web/mobilidade/w/saiba_como_e_e_como_funciona/junta_administrativa_de_recursos_de_infracoes_jari/3871
- Defesa da Autuação de Trânsito (CDA) — SMT/Prefeitura de São Paulo — https://prefeitura.sp.gov.br/web/mobilidade/w/saiba_como_e_e_como_funciona/comissao_de_defesa_da_autuacao_cda/5458
- Credenciamento de entidades para indicação de membros da JARI — Edital nº 001/2026-JARI/CET — https://www.cetsp.com.br/consultas/multas/inscricoes-para-credenciamento-de-entidades-para-indicacao-de-membros-da-jari.aspx
- DSV Digital (peticionamento eletrônico) — https://dsvdigital.prefeitura.sp.gov.br
- Portal Meu Veículo (acompanhamento) — https://meuveiculo.prefeitura.sp.gov.br
- JARI do Município de São Paulo (página institucional) — https://prefeitura.sp.gov.br/web/mobilidade/saiba_como_e_e_como_funciona/junta_administrativa_de_recursos_de_infracoes_jari
- JARI — CPTrans Petrópolis — https://www.petropolis.rj.gov.br/cptrans/index.php/jari-junta-administrativa-de-recursos-de-infracoes
- JARI — Prefeitura de Curitiba — https://transito.curitiba.pr.gov.br/institucional/junta-administrativa-de-recursos-de-infracoes-jari/20
- Publicação de pautas de sessão da JARI do DETRAN-PB — https://detran.pb.gov.br/infracoes/Jari/publicacao-da-sessao-de-julgamento-da-jari-16-05.pdf
- Sistema RENAINF — SENATRAN — https://www.gov.br/transportes/pt-br/assuntos/transito/conteudo-Senatran/sistema-renainf
- Súmula Vinculante 21 — STF — https://www.stf.jus.br/portal/jurisprudencia/menuSumario.asp?sumula=1255
