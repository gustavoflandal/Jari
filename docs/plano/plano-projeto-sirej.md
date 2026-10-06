# Plano do Projeto SIREJ (Sistema Integrado de Recursos de Infrações de Trânsito / JARI)

**Versão:** 0.1 (rascunho para validação)
**Data:** 06/10/2026
**Base documental analisada:**
- `enunciado-projeto-sistema-jari.md` (v1.0, 14/09/2026)
- `arquitetura-infraestrutura-sirej.md` (v1.0, 14/09/2026)
- `Estudo_JARI.pdf` (notas de pesquisa que originaram o enunciado)
- `JARI_CET.pdf` (Edital nº 001/2026-JARI/CET com Anexo III: Regimento das JARIs, Comunicado 007/23)

> Este plano é um rascunho. Prazos, equipe e custos são **estimativas** feitas a partir dos documentos, sem informação do contratante. Tudo que depende de resposta está marcado e listado na seção 14.

---

## 1. Sumário executivo

O SIREJ é um sistema web de processo administrativo eletrônico que conduz o rito recursal de infrações de trânsito de ponta a ponta: protocolo pelo cidadão, triagem, distribuição, relatoria, sessão colegiada da JARI, publicação, 2ª instância (CETRAN) e cumprimento da decisão. O caso de referência é a JARI do Município de São Paulo (CET), com 27 juntas, cerca de 162 membros e de 18 a 26 mil recursos por mês.

O que define o projeto não é volume, e sim **integridade**: o regimento proíbe que alguém conheça a designação de relator/revisor/terceiro membro antes da reunião e obriga a suspender (nunca contornar manualmente) a distribuição quando o sistema falha. O controle central da arquitetura é um selo criptográfico (commit–reveal com chave em HSM/KMS).

**Proposta de execução:** 5 fases (F0 a F4), em cerca de **24 meses** até o segundo órgão em produção, com um primeiro ciclo completo de julgamento digital em uma junta-piloto por volta do **mês 12**. Equipe estimada de 14 a 18 pessoas no pico.

**Pontos que mais pedem decisão agora:** quem é o contratante e qual o papel de vocês (fornecedor, órgão, proposta comercial); esfera e abrangência do rito; política de sigilo da designação; disponibilidade de HSM/KMS e da API do sistema de multas; tamanho do acervo legado.

---

## 2. Entendimento do escopo

### 2.1 Objetivo
Conduzir integralmente o rito recursal com autos 100% digitais, controle automático de prazos do CTB, distribuição auditável e sigilosa, sessão colegiada digital, publicidade ativa e notificação eletrônica, com validade jurídica (assinatura, carimbo do tempo, trilha imutável).

### 2.2 Dentro do escopo (9 módulos)
| # | Módulo | Essência |
|---|---|---|
| M1 | Portal do Recorrente | gov.br, consulta por CPF/CNPJ/placa, assistente de peticionamento por tipo de peça, upload + PDF/A, assinatura, recibo com hash, linha do tempo, notificações, procurações |
| M2 | Autuação e Instrução (órgão autuador) | Auto eletrônico, informação do agente, defesa da autuação (CDA), efeito suspensivo, cumprimento de decisão via sistema de multas |
| M3 | Secretaria da JARI | Triagem de admissibilidade, exigência/saneamento, remessa por incompetência (RENAINF), distribuição semanal, selo do processo, pauta, ata, composição e quórum |
| M4 | Relatoria e Julgamento | Ambiente do relator, dossiê de contexto, editor de voto, precedentes, fluxo relator → revisor → 3º membro, abertura de sessão com revelação, impedimento/suspeição, diligências, assinatura |
| M5 | Segunda Instância | Peticionamento ao CETRAN, admissibilidade, remessa íntegra, retorno e cumprimento |
| M6 | Credenciamento e Composição | Edital bienal, inscrição de entidades, CADIN, sorteio de classificação, convocação, habilitação, provas, posse, mandato, perda de mandato, capacitação, presença |
| M7 | Transparência e Indicadores | Consulta pública, estatísticas mensais e relatório anual, retorno sistêmico (padrões de falha de sinalização), painéis, dados abertos |
| M8 | Demandas Judiciais e Informações | Citações/intimações, subsídio de defesa judicial, consultas ao CETRAN/CONTRAN, divulgação de atos |
| M9 | Administração | Perfis por escopo, parametrização sem deploy (prazos, feriados, regimento), auditoria append-only, temporalidade |

### 2.3 Fora do escopo
Processamento de multas (lavratura, cálculo, arrecadação, pontuação), fiscalização eletrônica, cobrança, peticionamento judicial, decisão por IA.

### 2.4 Restrições técnicas já fixadas
- Plataforma Java/JavaScript: Java 25 LTS, Spring Boot 4.1 + Spring Modulith, PostgreSQL 17+, S3 com Object Lock, Keycloak federando gov.br, React 19 + TypeScript, GovBR-DS, TipTap, PDF.js.
- Monólito modular, outbox transacional no Postgres (sem broker inicial), portabilidade de hospedagem, HSM/KMS obrigatório para o selo da distribuição.

### 2.5 Os três requisitos que mais moldam o projeto
1. **Sigilo da designação até a abertura da sessão** (RN20, art. 29, XII): commit–reveal, chave em HSM, auditoria de toda consulta, teste de regressão dedicado.
2. **Falha fechada da distribuição** (RN24, art. 22, §1): job que aborta sem estado parcial; não existe rota manual.
3. **Regimento como configuração** (o regimento vigente foi trocado por um simples Comunicado em 2023): prazos, rol de resultados, regras de turma e de distribuição parametrizáveis por tenant.

---

## 3. Inconsistências e lacunas encontradas na documentação

Cruzei o enunciado e a arquitetura com o texto integral do Regimento (Anexo III do edital). Pontos que precisam de decisão ou correção:

| # | Ponto | Onde | Impacto |
|---|---|---|---|
| I1 | **Quórum x três votos.** O enunciado (5.4 e RN14) diz que o sistema "não admite resultado sem as três manifestações assinadas", mas o art. 17, §1 e o art. 6º, §3 permitem, excepcionalmente, deliberar por maioria simples (2 membros) com presença do presidente ou suplente. | 5.4, RN14, RN22 x arts. 6º §3 e 17 §1 | Define o motor de votação e a apuração do resultado |
| I2 | **Presença do presidente na turma.** Funcionam até 2 turmas simultâneas, mas a deliberação excepcional exige o presidente ou vice. Não está claro se presidente e vice precisam ficar em turmas diferentes, o que restringe o rodízio combinatório de 4 partições. | art. 17 §§1–3 | Algoritmo de formação das turmas |
| I3 | **Acesso aos autos só no dia da reunião.** Art. 29, XIII restringe acesso às instalações fora dos dias de reunião e art. 19 veda retirar processos. Se isso se aplicar ao sistema, o membro só lê e relata ~38 processos durante a própria reunião. | arts. 19 e 29, XIII | Janela de acesso, carga no horário de sessão, UX do relator |
| I4 | **"Revisor" não existe no regimento.** O regimento fala em relator e "demais membros da turma" (art. 27, VII). Os papéis de revisor e 3º membro vêm da página institucional. Falta confirmar se a ordem de voto é fixa. | 4.3, RN14 x art. 27 | Fluxo de votação sequencial |
| I5 | **F1 depende do que só existe em F3.** O MVP externo (F1) prevê consulta de autuações por CPF/placa e o critério "cidadão acompanha um recurso real do início ao fim", mas a integração com o sistema de multas está em F3 e o julgamento em F2. | Seção 12 | Ordem das fases (proposta de ajuste na seção 5) |
| I6 | **Módulos sem fase.** Credenciamento (M6), Demandas Judiciais (M8) e o retorno sistêmico (M7) não aparecem em nenhuma fase. O cadastro de membros, mandatos e presença é pré-requisito de F2. | Seção 12 | Escopo e cronograma |
| I7 | **Teste de informática do edital usa Word, não o sistema.** O enunciado diz que o candidato "acessa o sistema", mas o edital (5.7) manda abrir o Word for Windows. Usar o SIREJ na prova é uma mudança de edital. | 5.6 x Edital 5.7 | Escopo de M6 |
| I8 | **Inscrição de entidades é presencial pelo edital vigente** (protocolo físico na CET). Digitalizar exige ajuste no próximo edital. | Edital 2.1 | Quando M6 pode ser usado de fato |
| I9 | **Perda de mandato.** O enunciado diz que "hipóteses que dependem de juízo exigem procedimento"; o regimento lista exatamente os incisos III a IX. O inciso X (suspeição imotivada) não está na lista, embora dependa de juízo. | 5.6, RN30 x art. 12, par. único | Regra de evidência x sanção |
| I10 | **Roteiro da reunião é facultativo** ("poderão obedecer, a critério de cada Presidente"), só os incisos III e IV são compulsórios. O enunciado o trata como fluxo guiado obrigatório. | 5.4 x art. 15 | Rigidez do fluxo de sessão |
| I11 | **Prazo de relatoria** (art. 12, VI) não está definido em norma. | RN30 | Parâmetro a definir |
| I12 | **Volume divergente:** 18 mil/mês (enunciado) x 25 mil/mês (arquitetura). | 1.1 x arq. 2 | Dimensionamento (diferença pequena; o alvo 3× cobre os dois) |
| I13 | **Remissões internas erradas no regimento** (art. 9º → "art. 5º, III"; art. 29 → "art. 4º, V"), já apontadas no enunciado. | Regimento | Registrar na modelagem; sugerir correção ao órgão |
| I14 | **Art. 30** condiciona o "planejamento mensal de distribuição interna" à distribuição eletrônica, mas não diz o que é esse planejamento. | art. 30 | Requisito não especificado |
| I15 | **Defesa da autuação (CDA) e assinaturas.** M2 já inclui a análise da defesa da autuação, mas a decisão 15.2 (cobrir ou não todo o rito) segue aberta. | 5.2 x 15.2 | Tamanho de M1/M2 |

---

## 4. Premissas adotadas neste rascunho (padrões até haver resposta)

| # | Premissa | Por quê |
|---|---|---|
| P1 | Órgão-piloto é a CET/São Paulo (municipal), 2ª instância CETRAN-SP | Toda a documentação normativa é de SP |
| P2 | O rito completo do cidadão entra (defesa da autuação, indicação de condutor, recurso JARI, 2ª instância), mas indicação de condutor e advertência só como **roteamento** | Recomendação do próprio enunciado (A.4) |
| P3 | Integração com o sistema de multas, sem substituí-lo | Enunciado 3.3 |
| P4 | Designação sigilosa como padrão; modo aberto como parâmetro de tenant | Enunciado A.5 |
| P5 | Sessão presencial ou híbrida; **sem sessão virtual** até alteração regimental | Enunciado 15.4 |
| P6 | Produto multi-tenant no modelo de dados desde o início, mas com um único tenant em produção até F4 | Barato agora, caro depois |
| P7 | Hospedagem no Cenário B (empresa pública de TI do município) se ela oferecer Postgres, S3 e KMS/HSM; senão Cenário A | Recomendação da arquitetura |
| P8 | Acervo legado entra apenas como repositório somente leitura com metadados mínimos, em trilha separada | Maior risco de custo, não estimável sem inventário |
| P9 | Deliberação por maioria simples (2 votos) suportada como exceção registrada, nunca como padrão | Resolve I1 a favor do texto regimental |

---

## 5. Fases e entregas (proposta revisada)

Ajustes em relação ao enunciado: consulta ao sistema de multas sobe para F1 (somente leitura), cadastro de membros e mandatos entra em F2, e credenciamento e demandas judiciais ganham fase própria.

### F0 — Descoberta e fundação (meses 1 a 3)
**Objetivo:** fluxo validado e assinado pelos donos de processo, e base técnica pronta.

Entregas:
- Mapeamento do rito real da CET (as-is e to-be) por fase: autuação, CDA, JARI, CETRAN, cumprimento.
- Matriz regimento → requisito → regra parametrizável (a partir da tabela A.7 do enunciado), incluindo resolução das inconsistências I1 a I15.
- Inventário de integrações: sistema de multas (API ou arquivo/banco?), gov.br, SNE/CDT, RENAINF, Diário Oficial, assinador, SEI.
- Inventário do acervo legado (quantidade de processos e terabytes).
- Decisão de hospedagem e confirmação do HSM/KMS.
- ADRs 1 a 10 da arquitetura formalizados.
- Especificação formal do algoritmo de distribuição e do selo (com revisão independente).
- Protótipo navegável do portal e da tela do relator, testado com cidadãos e com 2 ou 3 membros de JARI.
- Fundação técnica: monorepo, pipeline CI/CD com SAST/SCA/SBOM, ambientes local, CI e homologação, Keycloak + gov.br em homologação.

Critério de saída: fluxo to-be assinado; decisões D1 a D8 (seção 14) respondidas; pipeline rodando.

### F1 — MVP externo (meses 4 a 7)
**Objetivo:** cidadão protocola recurso sem papel e acompanha o andamento.

Entregas:
- M1: login gov.br, consulta de autuações/penalidades (leitura do sistema de multas), assistente de peticionamento com indicação de fase e peça cabível, upload com antivírus e PDF/A, assinatura gov.br, recibo com hash, linha do tempo, notificação por e-mail.
- Motor de prazos (serviço isolado com calendário de feriados) com tempestividade e ciência presumida da Res. 918/2022.
- Processo, movimentação (eventos imutáveis), documentos endereçados por hash, auditoria encadeada.
- M9 básico: perfis, parametrização de prazos e feriados.
- Recepção manual de peças em papel (digitalização e certificação).
- Projeção de "situação" para a Secretaria atual trabalhar os protocolos fora do sistema até F2.

Critério de saída: recurso real protocolado pelo celular em menos de 5 minutos, com recibo e linha do tempo; a peça chega à Secretaria.

### F2 — Núcleo JARI (meses 6 a 12)
**Objetivo:** uma junta-piloto julga uma sessão inteira exclusivamente no sistema.

Entregas:
- M6 mínimo: cadastro de juntas, membros, segmentos, mandatos, presidente/vice, suplentes, termos de posse.
- M3: triagem assistida, exigência/saneamento, remessa por incompetência, distribuição semanal com conexão por veículo/requerente, selo criptográfico, selo do processo (ex.: `2026-S22 / 14ª Junta / B / seq. 20`), pauta e edital de pauta.
- M4: abertura de sessão (quórum, presenças, revelação), formação de turmas com rodízio combinatório, distribuição interna, ambiente do relator (autos, dossiê, checklist dos três eixos), editor de voto, votação sequencial, impedimento/suspeição, diligência, assinatura PAdES, ata, acórdão, ementa, certidão.
- Visualizador sem download para membros, com marca d'água e registro de acesso.
- Publicação do resultado e notificação ao recorrente.
- Sandbox regimental para treinamento dos membros.
- Suíte de testes obrigatória das regras RN19 a RN30 e regras ArchUnit protegendo a designação.
- Pentest independente focado em distribuição e sigilo.

Critério de saída: junta-piloto realiza sessão real, com ata e acórdão assinados e publicados sem redigitação.

### F3 — Integrações, 2ª instância e cumprimento (meses 11 a 17)
**Objetivo:** ciclo 1ª → 2ª instância → cumprimento sem intervenção manual.

Entregas:
- Integração bidirecional com o sistema de multas (camada anticorrupção, outbox, reconciliação): efeito suspensivo, cumprimento, restituição.
- SNE/CDT (notificação e ciência), RENAINF, Diário Oficial eletrônico.
- M5: peticionamento ao CETRAN, admissibilidade, remessa íntegra com índice e verificação, retorno e cumprimento.
- Painel de backlog, alertas T-10/T-5/T-1 e lista de elegíveis a efeito suspensivo.
- Expansão gradual para todas as 27 juntas (ondas de 5 a 7 juntas).
- Estatísticas mensais e relatório anual (art. 28, XII).

Critério de saída: 27 juntas operando no sistema; um processo percorre 1ª e 2ª instância e é cumprido no sistema de multas sem intervenção manual.

### F4 — Composição, transparência e escala (meses 16 a 24)
**Objetivo:** ciclo de vida completo dos membros, transparência ativa e segundo órgão.

Entregas:
- M6 completo: edital bienal, inscrição de entidades, consulta CADIN, sorteio de classificação com rateio, convocação, habilitação, provas, posse, perda de mandato (evidência, nunca sanção), capacitação, presença e gratificação.
- M7: consulta pública anonimizada, retorno sistêmico de falhas de sinalização, painéis gerenciais, dados abertos.
- M8: demandas judiciais, subsídio à defesa, base de consultas ao CETRAN/CONTRAN.
- Base de precedentes pesquisável; apoio de IA à triagem (sem decisão automatizada).
- Onboarding do segundo tenant.
- Avaliação jurídica da sessão virtual (somente se houver alteração regimental).

Critério de saída: segundo órgão em produção; indicadores publicados.

### Cronograma macro (estimativa)

```
Mês:        1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
F0          ██ ██ ██
F1                   ██ ██ ██ ██
F2                         ██ ██ ██ ██ ██ ██ ██
F3                                        ██ ██ ██ ██ ██ ██ ██
F4                                                       ██ ██ ██ ██ ██ ██ ██ ██ ██
Piloto 1 junta                                   ▲ (mês 12)
27 juntas                                                        ▲ (mês 17)
2º órgão                                                                          ▲ (mês 24)
```

---

## 6. Estrutura analítica do projeto (EAP)

1. Gestão do projeto: plano, cronograma, riscos, comunicação, mudanças, relatórios ao patrocinador.
2. Negócio e normas: mapeamento de processo, matriz regimento → regra, parametrização por tenant, validação jurídica.
3. UX e acessibilidade: pesquisa com cidadãos e membros, protótipos, design system, linguagem simples, WCAG 2.1 AA/eMAG.
4. Desenvolvimento por módulo: M1 a M9.
5. Serviços transversais: motor de prazos, documentos (PDF/A, renditions, antivírus), assinatura e carimbo do tempo, auditoria encadeada, notificação.
6. Segurança: modelo de ameaças, selo criptográfico, HSM/KMS, break-glass, ASVS nível 2, pentests.
7. Integrações: multas, gov.br, SNE/CDT, RENAINF, DOC, assinador, SEI/BI.
8. Infraestrutura e operação: ambientes, pipeline, observabilidade, backup e DR, runbooks.
9. Dados: modelo, migração do acervo legado, anonimização para homologação, dados abertos.
10. Qualidade: estratégia de testes, carga (cenário "27 juntas abrem sessão no mesmo minuto"), E2E de sessão completa, regressão do sigilo.
11. Implantação: piloto, ondas de expansão, treinamento (sandbox regimental), suporte assistido, comunicação ao cidadão.
12. Transição: documentação, repasse à equipe de sustentação, contrato de atualização anual da plataforma.

---

## 7. Equipe estimada

| Papel | Qtd. | Fases |
|---|---|---|
| Gerente de projeto | 1 | todas |
| Product owner (do órgão) | 1 | todas |
| Analista de negócio com base jurídica (trânsito/processo administrativo) | 1–2 | todas |
| Arquiteto de software | 1 | todas |
| Engenheiro de segurança / criptografia | 0,5–1 | F0, F2, pentests |
| Desenvolvedores backend Java | 4–6 | F1 a F4 |
| Desenvolvedores frontend React | 2–3 | F1 a F4 |
| Designer UX / acessibilidade | 1 | F0 a F2, F4 |
| QA / automação de testes | 2 | F1 a F4 |
| DevOps / SRE | 1 | todas |
| DBA (parcial) | 0,5 | F0, F3 |
| Especialista em integrações | 1 | F1, F3 |
| Pessoas-chave do órgão: Secretaria da JARI, coordenador, 1 presidente de junta, TI do sistema de multas | sob demanda | F0, homologações |

Pico estimado: 14 a 18 pessoas (F2/F3).

---

## 8. Backlog macro priorizado (épicos)

| Prioridade | Épico | Fase |
|---|---|---|
| Crítico | Distribuição semanal com selo commit–reveal e falha fechada | F2 |
| Crítico | Abertura de sessão com quórum e revelação | F2 |
| Crítico | Motor de prazos com calendário parametrizável | F1 |
| Crítico | Auditoria append-only encadeada e ancorada | F1 |
| Crítico | Protocolo assinado com recibo e hash | F1 |
| Alto | Assistente de peticionamento (peça certa na fase certa) | F1 |
| Alto | Ambiente do relator com editor simples e caminho relatar → votar → próximo | F2 |
| Alto | Formação de turmas com rodízio combinatório e substituição por ausência | F2 |
| Alto | Ata, acórdão e certidão gerados e assinados | F2 |
| Alto | Integração com sistema de multas (leitura em F1, cumprimento em F3) | F1/F3 |
| Alto | Remessa ao CETRAN e retorno | F3 |
| Médio | Credenciamento de entidades e ciclo de vida dos membros | F2 (mínimo) / F4 |
| Médio | Retorno sistêmico de falhas de sinalização | F4 |
| Médio | Painéis, estatísticas mensais e relatório anual | F3/F4 |
| Médio | Demandas judiciais | F4 |
| Baixo | Base de precedentes com busca e apoio de IA | F4 |
| Condicionado | Sessão virtual assíncrona | após alteração regimental |

---

## 9. Marcos e critérios de aceite

| Marco | Mês (est.) | Critério |
|---|---|---|
| M0 – Plano aprovado | 1 | Patrocinador aprova escopo, premissas e decisões abertas |
| M1 – Fluxo to-be assinado | 3 | Donos de processo assinam o fluxo e a matriz de regras |
| M2 – Primeiro protocolo real | 7 | Recurso protocolado por celular em < 5 min, com recibo verificável |
| M3 – Primeira sessão digital | 12 | Junta-piloto julga sessão completa no sistema; designação revelada só na abertura e verificável contra o compromisso |
| M4 – Todas as juntas | 17 | 27 juntas operando; relatório diário de processos fora do prazo |
| M5 – Ciclo completo | 17 | 1ª → 2ª instância → cumprimento sem intervenção manual |
| M6 – Escala | 24 | Segundo órgão em produção; indicadores publicados |

Os nove critérios de aceite da seção 13 do enunciado valem como critérios finais do projeto e serão rastreados até casos de teste.

---

## 10. Governança

- **Comitê executivo** (mensal): patrocinador, coordenador das JARIs, GP, arquiteto. Decide escopo, prazos e mudanças.
- **Comitê de regras** (quinzenal na F0, mensal depois): coordenador, representante jurídico, analista de negócio. Mantém a matriz regimento → regra e aprova parametrizações.
- **Comitê de segurança** (por marco): segurança do órgão, arquiteto, auditor independente. Aprova algoritmo de distribuição, selo e resultados de pentest.
- **Cadência de entrega:** sprints de 2 semanas, revisão com o órgão ao fim de cada sprint, homologação por fase.
- **Registro de decisões:** ADRs no repositório e decisões de negócio em log versionado.
- **Gestão de mudanças:** toda mudança de escopo passa pelo comitê executivo com análise de impacto em prazo e custo.

---

## 11. Riscos (complementa a seção 14 do enunciado)

| Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|
| Sistema de multas sem API | Alta | Alto | Camada anticorrupção; prova de conceito de integração já na F0 |
| HSM/KMS indisponível no cenário de hospedagem | Média | Crítico | Decidir na F0; plano B documentado (mais fraco) e aceito formalmente |
| Captura ou vazamento da designação | Média | Crítico | Selo, ArchUnit, auditoria de consulta, revisão independente do algoritmo, pentest dirigido |
| Atualização anual do Spring Boot não contratada | Alta | Alto | Cláusula contratual de atualização de plataforma |
| Acervo legado subdimensionado | Alta | Alto | Inventário na F0; trilha e orçamento separados |
| Resistência dos membros ao julgamento digital | Média | Médio | Junta voluntária, sandbox, editor simples, suporte presencial nas primeiras sessões |
| Mudança do regimento durante o projeto | Alta | Médio | Regras parametrizáveis; comitê de regras |
| Interpretações divergentes do regimento (I1 a I15) | Alta | Médio | Resolver e registrar formalmente na F0 |
| Dependência de mudança de edital para digitalizar o credenciamento | Média | Baixo | Planejar M6 alinhado ao próximo edital bienal (2028) |
| Equipe do órgão sem disponibilidade para validação | Média | Alto | Agenda fixa de validação acordada no M0 |
| Pico de protocolos em fim de prazo | Média | Médio | Recebimento assíncrono com recibo imediato; capacidade reservada |

---

## 12. Estratégia de qualidade

- Testes unitários e de integração (Testcontainers com Postgres, MinIO e Keycloak reais).
- **Suíte obrigatória das regras RN19 a RN30**, cada regra com casos positivos e negativos.
- Testes de propriedade para a distribuição (equidade por membro, conexão, rodízio sem repetição antes de 4 reuniões).
- Regressão de sigilo: nenhum endpoint, relatório ou log expõe a designação antes da abertura.
- E2E (Playwright) de uma sessão completa, do protocolo à publicação.
- Acessibilidade com axe-core em pipeline e avaliação manual com usuários.
- Teste de carga com o cenário real: 27 juntas abrindo sessão no mesmo minuto + rajada de protocolos em véspera de prazo.
- Restauração de backup testada trimestralmente; ensaio de DR incluindo a chave mestra.

---

## 13. Implantação, treinamento e transição

- **Piloto:** uma junta voluntária, com sessões em paralelo ao processo atual nas 2 primeiras semanas (somente se o regimento permitir; senão, piloto direto com suporte presencial).
- **Ondas:** 5 a 7 juntas por onda, a cada 3 ou 4 semanas.
- **Treinamento:** sandbox regimental com calendário acelerado; trilha para membros, Secretaria, presidentes e coordenador; material integrado à capacitação do art. 28, XV.
- **Comunicação ao cidadão:** páginas de serviço geradas a partir da mesma parametrização do motor de prazos.
- **Transição:** runbooks, documentação de arquitetura, repasse à equipe de sustentação definida em D7.

---

## 14. Perguntas em aberto

### Decisões que bloqueiam o plano (D)
- D1. Qual é o papel de vocês no projeto: fornecedor contratado, equipe interna do órgão, ou proposta comercial a ser apresentada?
- D2. O contratante e órgão-piloto é a CET/Prefeitura de São Paulo, ou outro órgão?
- D3. A esfera é municipal, estadual (DETRAN) ou rodoviária?
- D4. O sistema cobre todo o rito (defesa da autuação, indicação de condutor, JARI, CETRAN) ou só o recurso à JARI?
- D5. A designação dos julgadores será sigilosa até a sessão (modelo SP) ou publicada na pauta (modelo DETRAN-PB)?
- D6. Será produto multi-tenant para vários órgãos ou instalação única?
- D7. Quem opera o sistema após o go-live: o órgão, a empresa pública de TI ou o fornecedor?
- D8. Existe prazo-limite (contratual, eleitoral, orçamentário) para a entrada em produção?

### Prazo, orçamento e equipe (P)
- P1. Há orçamento definido ou teto de custo para o projeto?
- P2. Há equipe já disponível, ou a equipe será montada/contratada?
- P3. O modelo de contratação é por fábrica de software, preço fixo, ou alocação?
- P4. Quem será o product owner do lado do órgão e quanto tempo ele terá por semana?

### Infraestrutura (I)
- I-1. Onde o sistema será hospedado (nuvem pública, empresa pública de TI como a PRODAM, ou datacenter próprio)?
- I-2. Existe HSM ou serviço de chaves gerenciado disponível nesse ambiente?
- I-3. O órgão já opera Kubernetes com equipe própria?
- I-4. É aceitável operar runtime Node em produção (SSR do portal)?
- I-5. Existe design system institucional da Prefeitura a seguir no lugar do GovBR-DS?

### Integrações (N)
- N1. Qual é o sistema de multas atual e quem o mantém?
- N2. Esse sistema expõe API, ou a integração será por arquivo/banco?
- N3. Existe ambiente de homologação do sistema de multas disponível para o projeto?
- N4. O órgão já aderiu ao login gov.br e ao SNE/CDT?
- N5. Qual Diário Oficial eletrônico será integrado e há API de publicação?
- N6. O sistema deve integrar com o SEI (processo SEI do edital indica uso)?
- N7. Qual provedor de assinatura será usado: gov.br avançada, ICP-Brasil, ou os dois?
- N8. DSV Digital e Meu Veículo serão substituídos pelo SIREJ ou continuarão existindo em paralelo?

### Regras do regimento (R)
- R1. Uma decisão com só 2 votos (maioria simples, art. 17 §1) é válida no sistema, ou sempre são exigidos 3 votos assinados?
- R2. Presidente e vice precisam ficar em turmas diferentes quando há 2 turmas simultâneas?
- R3. O membro pode acessar os autos fora do dia e horário da reunião, ou só durante a sessão (arts. 19 e 29, XIII)?
- R4. A ordem de voto relator → revisor → 3º membro é fixa e sequencial, ou os dois outros votam em paralelo?
- R5. Qual é o prazo de relatoria a ser medido (art. 12, VI)?
- R6. O que é o "planejamento mensal de distribuição interna" do art. 30?
- R7. O roteiro de reunião do art. 15 deve ser obrigatório no sistema ou apenas sugerido?
- R8. O prazo-meta interno para análise da defesa da autuação (sem prazo legal) já existe? Qual é?
- R9. Após o julgamento, os nomes dos julgadores aparecem para o recorrente e na consulta pública?
- R10. Quando o mesmo veículo tem recursos conexos em semanas diferentes, eles devem esperar para ir juntos ao mesmo membro?
- R11. Como tratar membro que muda de junta ou sai no meio do ciclo, com processos conexos já atribuídos à sua posição?
- R12. O órgão aceita propor a correção das remissões internas do regimento (arts. 9º e 29)?

### Credenciamento e composição (C)
- C1. O próximo edital bienal pode prever inscrição eletrônica e prova de informática no próprio sistema?
- C2. A seleção dos representantes da comunidade (565 inscritos no último ciclo) entra no escopo?
- C3. A gratificação por presença é calculada pelo SIREJ ou só a presença é enviada à folha?
- C4. A consulta ao CADIN Municipal tem API ou é manual?

### Dados e acervo (A)
- A1. Quantos processos e quantos terabytes existem no acervo legado a migrar?
- A2. Processos em andamento no momento da implantação migram para o SIREJ ou terminam no sistema antigo?
- A3. Qual é a política de temporalidade documental do município a respeitar?

### Segunda instância (S)
- S1. Quais são os procedimentos do CETRAN-SP para receber recursos (art. 25, IX)? Existe documento?
- S2. O CETRAN-SP tem sistema próprio para receber os autos eletronicamente, ou a remessa será por pacote/arquivo?

### Expectativa sobre este plano (E)
- E1. Este plano deve servir para gestão interna, para proposta comercial, ou para termo de referência de licitação?
- E2. Precisa de estimativa de custo em reais e de esforço em horas por fase?
- E3. Há modelo ou metodologia de plano exigida (PMBOK, ágil, padrão do órgão)?

---

## 15. Próximos passos

1. Responder as decisões D1 a D8 e a pergunta E1, que mudam a forma do plano.
2. Com essas respostas, ajustar fases, equipe e cronograma, e adicionar estimativa de custo se pedida.
3. Agendar a F0 com o órgão-piloto: entrevistas com Secretaria, coordenador, um presidente de junta e a TI do sistema de multas.
4. Fechar a especificação do algoritmo de distribuição e do selo, por ser o componente de maior risco.
