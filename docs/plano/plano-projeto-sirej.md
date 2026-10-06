# Plano do Projeto SIREJ (Sistema Integrado de Recursos de Infrações de Trânsito / JARI)

**Versão:** 0.3 (Curitiba como primeiro alvo)
**Data:** 06/10/2026
**Base documental analisada:**
- `enunciado-projeto-sistema-jari.md` (v1.0, 14/09/2026)
- `arquitetura-infraestrutura-sirej.md` (v1.0, 14/09/2026)
- `Estudo_JARI.pdf` (notas de pesquisa que originaram o enunciado)
- `JARI_CET.pdf` (Edital nº 001/2026-JARI/CET com Anexo III: Regimento das JARIs, Comunicado 007/23)

> Prazos, equipe e custos são **estimativas** feitas a partir dos documentos. Tudo que depende de resposta está listado na seção 15.

### Histórico
| Versão | Data | Mudança |
|---|---|---|
| 0.1 | 06/10/2026 | Primeira versão, assumindo a CET/SP como contratante |
| 0.2 | 06/10/2026 | Revisão com as respostas de Gustavo: empresa concorrente em licitações, clientes em outros estados, instalação única por órgão |
| 0.3 | 06/10/2026 | Respostas L1 a L5: editais em andamento, órgãos municipais e estaduais, Curitiba como primeiro alvo, produto partindo do zero com investimento da empresa |

---

## 1. Sumário executivo

O SIREJ é um produto que a empresa vai oferecer em licitações de órgãos de trânsito de vários estados. Ele conduz o rito recursal de infrações de ponta a ponta: protocolo pelo cidadão, triagem, distribuição, relatoria, sessão colegiada da JARI, publicação, 2ª instância (CETRAN) e cumprimento da decisão. A JARI de São Paulo (CET), com 27 juntas e 18 a 26 mil recursos por mês, serve como **referência normativa e de dimensionamento**, não como cliente.

O que define o produto é **integridade**: o modo sigiloso impede que alguém conheça relator, revisor e 3º membro antes da reunião, e a distribuição é suspensa (nunca contornada manualmente) quando o sistema falha. O controle central é um selo criptográfico (commit–reveal com chave em HSM/KMS).

Como cada órgão tem regimento, CETRAN, calendário e sistema de multas próprios, **parametrização deixa de ser boa prática e vira requisito de venda**. Uma única base de código atende a todos, com uma instalação e uma configuração por contratante.

**Proposta de execução em três trilhas:**
1. **Produto base** (investimento da empresa, cerca de 9 meses até estar pronto para prova de conceito).
2. **Proposta por edital** (2 a 6 semanas por licitação).
3. **Execução contratual por órgão** (cerca de 9 a 12 meses do contrato até todas as juntas operando).

---

## 2. Respostas recebidas e impacto no plano

| # | Pergunta | Resposta | Impacto |
|---|---|---|---|
| D1 | Papel da empresa | Concorrente na licitação | O plano cobre construção de produto, proposta e execução contratual |
| D2 | Contratante | Órgãos equivalentes em outros estados | SP vira referência; regimento, CETRAN, feriados e prazos são parâmetros por instalação |
| D3 | Abrangência do rito | O que cada edital licitar | Produto modular; cada proposta ativa os módulos pedidos no edital |
| D4 | Sigilo da designação | Configurável | Os dois modos (sigiloso e aberto) são implementados; sigiloso como padrão |
| D5 | Multi-tenant ou instalação única | Uma instalação por órgão contratante | Sai o multi-tenant em runtime; entra automação de implantação (infraestrutura como código, instalador, migração de configuração) |
| D6 | HSM/KMS | Provavelmente existirá | Mantém o selo com HSM/KMS; plano B documentado para edital que não ofereça |
| D7 | API do sistema de multas | Cada estado fornece a sua | Interface de integração padronizada no produto; adaptador por órgão vira item de implantação, com custo próprio |
| D8 | Finalidade deste plano | Proposta e concorrência licitatória | Inclui matriz de aderência ao edital, prova de conceito, precificação e riscos de licitação |

**Respostas de 06/10/2026 sobre estratégia (L1 a L5):**

| # | Pergunta | Resposta | Impacto |
|---|---|---|---|
| L1 | Há edital publicado? | Sim, vários em andamento | Trilha B começa já, em paralelo à Trilha A |
| L2 | Municipais ou estaduais? | Os dois | Parametrização precisa cobrir prefeituras e DETRANs (escala de poucas a dezenas de juntas) |
| L3 | Prioridade | Curitiba | Primeiro alvo; ver seção 2.1 |
| L4 | Há código existente? | Não, parte do zero | Não há produto para prova de conceito hoje; o prazo do edital de Curitiba decide a estratégia |
| L5 | A empresa investe antes do contrato? | Sim | Trilha A começa imediatamente |

**Mudanças nos documentos de base que isso provoca:**
- A arquitetura previa multi-tenant desde o início (enunciado 10 e 15.6). Passa a ser uma instalação por contratante, com o mesmo artefato.
- A "restrição fixa do cliente: Java/JavaScript" da arquitetura precisa ser confirmada como escolha da empresa ou exigência de edital (pergunta L10).
- Decisões que eram do patrocinador (enunciado 15) passam a ser parâmetros de instalação ou respostas por edital.

### 2.1 Primeiro alvo: Curitiba

O que as fontes públicas mostram em 06/10/2026:

| Item | Curitiba | São Paulo (referência) |
|---|---|---|
| Órgão | Secretaria Municipal de Defesa Social e Trânsito (SMDT) | CET |
| Norma da JARI | Lei municipal nº 15.154/2017 | Decreto 60.982/2021 + Comunicado 007/23 |
| Juntas | 4 juntas, cada uma com 6 titulares e 3 suplentes | 27 juntas de 6 membros |
| 2ª instância | CETRAN-PR | CETRAN-SP |
| Protocolo hoje | Plataformas do Estado do Paraná (Central de Segurança / identidade digital PR, PIÁ, Detran InteliGente para advogados com certificado da OAB), Correios e presencial com agendamento | DSV Digital, Correios |
| Assinatura exigida | Avançada ou qualificada ICP-Brasil | — |
| Notificação | SNE | SNE |

Fontes: [JARI — Trânsito Curitiba](https://transito.curitiba.pr.gov.br/institucional/junta-administrativa-de-recursos-de-infracoes-jari/20), [Recursos à JARI e ao CETRAN-PR](https://transito.curitiba.pr.gov.br/multas/recursos-de-multas-a-jari-e-ao-cetran-pr/68).

Não encontrei o edital do sistema de JARI de Curitiba nos portais públicos. Os pregões de 2026 localizados no portal de transparência tratam de outros objetos (PE 054/2026-SMDT, manutenção semafórica; PE 009/2026-SMATI, conectividade). O texto da Lei 15.154/2017 não pôde ser baixado automaticamente e precisa ser lido.

**O que isso muda:**
1. A escala é cerca de 7 vezes menor que a de SP (4 juntas contra 27). A primeira instalação pode ser enxuta, e o dimensionamento de SP fica como teto.
2. Suplentes fixos por junta (3 por junta) precisam entrar no modelo de composição e de substituição.
3. O protocolo hoje passa pela identidade digital do Paraná, não pelo gov.br. O produto precisa de um adaptador de identidade, além do gov.br.
4. O prazo do edital decide a estratégia, porque o produto parte do zero:

| Situação do edital de Curitiba | Estratégia recomendada |
|---|---|
| Proposta ou PoC em menos de 4 meses, exigindo produto pronto | Não disputar este edital; usá-lo para aprender o termo de referência e mirar o próximo |
| Proposta em menos de 4 meses, mas sem PoC e com implantação durante o contrato | Disputar, com o desenvolvimento cabendo no prazo de implantação do contrato; risco alto de prazo |
| PoC entre 4 e 6 meses | Trilha A acelerada com escopo de Curitiba: núcleo enxuto, equipe completa desde o mês 1, PoC por volta do mês 5 |
| Mais de 6 meses | Trilha A normal, com Curitiba parametrizado desde a A0 |

---

## 3. Entendimento do escopo

### 3.1 Objetivo
Conduzir integralmente o rito recursal com autos 100% digitais, controle automático de prazos do CTB, distribuição auditável, sessão colegiada digital, publicidade ativa e notificação eletrônica, com validade jurídica (assinatura, carimbo do tempo, trilha imutável).

### 3.2 Módulos do produto (ativáveis por edital)
| # | Módulo | Essência |
|---|---|---|
| M1 | Portal do Recorrente | gov.br, consulta por CPF/CNPJ/placa, assistente de peticionamento por tipo de peça, upload + PDF/A, assinatura, recibo com hash, linha do tempo, notificações, procurações |
| M2 | Autuação e Instrução (órgão autuador) | Auto eletrônico, informação do agente, defesa da autuação, efeito suspensivo, cumprimento de decisão via sistema de multas |
| M3 | Secretaria da JARI | Triagem de admissibilidade, exigência/saneamento, remessa por incompetência (RENAINF), distribuição semanal, selo do processo, pauta, ata, composição e quórum |
| M4 | Relatoria e Julgamento | Ambiente do relator, dossiê de contexto, editor de voto, precedentes, fluxo de votação, abertura de sessão com revelação, impedimento/suspeição, diligências, assinatura |
| M5 | Segunda Instância | Peticionamento ao CETRAN, admissibilidade, remessa íntegra, retorno e cumprimento |
| M6 | Credenciamento e Composição | Edital de credenciamento, inscrição de entidades, sorteio de classificação, convocação, habilitação, provas, posse, mandato, perda de mandato, capacitação, presença |
| M7 | Transparência e Indicadores | Consulta pública anonimizada, estatísticas regimentais, retorno sistêmico (padrões de falha de sinalização), painéis, dados abertos |
| M8 | Demandas Judiciais e Informações | Citações/intimações, subsídio de defesa judicial, consultas ao CETRAN/CONTRAN, divulgação de atos |
| M9 | Administração | Perfis por escopo, parametrização sem deploy (prazos, feriados, regimento), auditoria append-only, temporalidade |

M3, M4 e M9 formam o núcleo, presente em qualquer proposta. Os demais entram conforme o edital.

### 3.3 Fora do escopo
Processamento de multas (lavratura, cálculo, arrecadação, pontuação), fiscalização eletrônica, cobrança, peticionamento judicial, decisão por IA.

### 3.4 Restrições técnicas atuais
- Java 25 LTS, Spring Boot 4.1 + Spring Modulith, PostgreSQL 17+, S3 com Object Lock, Keycloak federando gov.br, React 19 + TypeScript, GovBR-DS (ou o design system do contratante), TipTap, PDF.js.
- Monólito modular, outbox transacional no Postgres, portabilidade de hospedagem, HSM/KMS para o selo da distribuição.
- Uma instalação por contratante, implantável em nuvem pública, empresa pública de TI ou datacenter do órgão.

### 3.5 Os três requisitos que mais moldam o produto
1. **Sigilo da designação** quando o modo sigiloso estiver ativo: commit–reveal, chave em HSM, auditoria de toda consulta, teste de regressão dedicado.
2. **Falha fechada da distribuição**: job que aborta sem estado parcial; não existe rota manual.
3. **Regimento como configuração**: cada órgão tem o seu, e o de SP foi trocado por um simples Comunicado em 2023.

---

## 4. Inconsistências encontradas na documentação

Cruzei o enunciado e a arquitetura com o texto integral do Regimento de SP (Anexo III do edital). Com o produto multiórgão, a maior parte se resolve tornando a regra configurável (coluna "Tratamento").

| # | Ponto | Onde | Tratamento |
|---|---|---|---|
| I1 | O enunciado exige três votos assinados, mas o art. 17, §1 permite deliberar por maioria simples com presidente ou vice | 5.4, RN14 x arts. 6º §3 e 17 §1 | Parâmetro: quórum mínimo de votos por decisão |
| I2 | Com 2 turmas simultâneas e presença obrigatória do presidente ou vice, não está claro se os dois ficam em turmas diferentes | art. 17 | Parâmetro de restrição na formação de turmas |
| I3 | Se a restrição de acesso fora dos dias de reunião valer para o sistema, o membro só relata durante a sessão | arts. 19 e 29, XIII | Parâmetro: janela de acesso dos membros aos autos |
| I4 | "Revisor" não existe no regimento; a ordem de voto vem da página institucional | art. 27, VII | Parâmetro: votação sequencial ou paralela |
| I5 | A F1 do enunciado previa consulta de autuações e acompanhamento até o fim, mas sistema de multas e julgamento vinham depois | Enunciado 12 | Resolvido: fases substituídas pelas trilhas da seção 6 |
| I6 | Credenciamento, demandas judiciais e retorno sistêmico não estavam em nenhuma fase | Enunciado 12 | Resolvido: entram no roadmap do produto (seção 6.1) |
| I7 | A prova de informática do edital de SP usa o Word, não o sistema | Edital 5.7 | Fora do núcleo; oferecer como opcional em M6 |
| I8 | A inscrição de entidades é presencial pelo edital de SP | Edital 2.1 | Depende de cada órgão; M6 suporta os dois canais |
| I9 | O art. 12 lista os incisos III a IX como dependentes de procedimento; o X fica de fora | art. 12 | Parâmetro por hipótese de perda de mandato; o sistema só evidencia |
| I10 | O roteiro de reunião é facultativo; só os incisos III e IV são compulsórios | art. 15 | Parâmetro: passos obrigatórios e opcionais do roteiro |
| I11 | Prazo de relatoria não definido em norma | art. 12, VI | Parâmetro |
| I12 | Volume de 18 mil/mês (enunciado) x 25 mil/mês (arquitetura) | 1.1 x arq. 2 | Dimensionamento por instalação, a partir do volume de cada edital |
| I13 | Remissões internas erradas no regimento de SP | arts. 9º e 29 | Só registro |
| I14 | O art. 30 cita "planejamento mensal de distribuição interna" sem defini-lo | art. 30 | Não implementar até haver definição de algum contratante |
| I15 | M2 já incluía a defesa da autuação, mas a abrangência estava em aberto | 5.2 x 15.2 | Resolvido por D3: ativado conforme o edital |

---

## 5. Premissas adotadas

| # | Premissa | Por quê |
|---|---|---|
| P1 | São Paulo é referência normativa e de dimensionamento; clientes são órgãos equivalentes de outros estados | Resposta D2 |
| P2 | Escopo modular; núcleo M3 + M4 + M9 sempre presente, demais módulos por edital | Resposta D3 |
| P3 | Integração com o sistema de multas de cada órgão por adaptador, sobre uma interface padronizada do produto | Resposta D7 |
| P4 | Política de designação configurável; modo sigiloso como padrão | Resposta D4; migrar do aberto para o sigiloso depois é caro |
| P5 | Sessão presencial ou híbrida por padrão; virtual só onde o regimento do contratante permitir | Enunciado 15.4 |
| P6 | Uma instalação por contratante, mesmo artefato, configuração própria, sem multi-tenant em runtime | Resposta D5 |
| P7 | Hospedagem e HSM/KMS fornecidos pelo contratante; plano B documentado | Resposta D6 |
| P8 | Migração de acervo legado é item opcional, precificado à parte | Maior risco de custo, não estimável sem inventário |
| P9 | Regras de votação, roteiro, turmas e acesso aos autos são parâmetros | Resolve I1 a I4 e I10 |
| P10 | A empresa constrói o produto base antes do primeiro edital | Editais costumam exigir prova de conceito em prazo curto |

---

## 6. Trilhas e fases

### 6.1 Trilha A: Produto base (investimento da empresa)

**Objetivo:** produto pronto para prova de conceito e para a primeira implantação.

| Etapa | Meses | Entregas | Critério de saída |
|---|---|---|---|
| A0 Fundação | 1–2 | Comparação de regimentos de 3 a 5 órgãos-alvo; modelo de parametrização; ADRs; especificação do algoritmo de distribuição e do selo; monorepo, CI/CD com SAST/SCA/SBOM; protótipos testados | Modelo de configuração cobre os regimentos comparados |
| A1 Núcleo demonstrável | 3–6 | Processo e movimentação por eventos, documentos por hash, auditoria encadeada, motor de prazos, protocolo assinado com recibo, distribuição semanal com selo e falha fechada, abertura de sessão com revelação, turmas com rodízio, ambiente do relator, votação, ata e acórdão, M9 | Uma sessão completa simulada, ponta a ponta, em ambiente de demonstração |
| A2 Pronto para PoC | 7–9 | M1 completo, assinatura gov.br e ICP-Brasil, interface padrão de integração com sistema de multas + adaptador de referência (arquivo e API simulada), instalador automatizado, sandbox de treinamento, roteiro de PoC ensaiado, pentest | PoC executada internamente dentro do prazo típico de edital |
| A3 Módulos complementares | 10–18 | M2, M5, integrações padrão (SNE/CDT, RENAINF, Diário Oficial), M6, M7, M8, precedentes, apoio de IA à triagem | Cada módulo com caderno de aderência pronto para propostas |

### 6.2 Trilha B: Proposta por edital (2 a 6 semanas)

1. Leitura do edital e do termo de referência; registro de pedidos de esclarecimento e, se cabível, impugnação.
2. Matriz de aderência: cada requisito do edital × módulo/parâmetro do produto × lacuna.
3. Estimativa das lacunas e do adaptador de multas do órgão.
4. Precificação (licença, implantação, adaptadores, migração, sustentação, conforme o modelo do edital).
5. Documentação de habilitação e atestados de capacidade técnica.
6. Preparação da prova de conceito com a parametrização do regimento do órgão.

### 6.3 Trilha C: Execução contratual por órgão (meses contados da assinatura)

| Etapa | Meses | Entregas | Critério de saída |
|---|---|---|---|
| C0 Descoberta e parametrização | 1–2 | Fluxo do órgão validado, regimento parametrizado, checklist da seção 15.3 respondido, ambiente provisionado | Configuração aprovada pelo órgão |
| C1 Integrações locais | 2–5 | Adaptador do sistema de multas, gov.br, SNE/CDT, Diário Oficial, assinador, HSM/KMS do órgão | Integrações homologadas |
| C2 Piloto | 4–6 | Uma junta voluntária julgando no sistema; portal do cidadão aberto | Sessão real com ata e acórdão assinados e publicados |
| C3 Expansão | 6–9 | Todas as juntas em ondas; 2ª instância e cumprimento | Ciclo 1ª → 2ª → cumprimento sem intervenção manual |
| C4 Operação assistida e sustentação | 9 em diante | Suporte, atualizações de plataforma, relatórios regimentais | Conforme níveis de serviço do contrato |

Prazos de C1 dependem diretamente da qualidade da API de multas de cada órgão.

---

## 7. Estrutura analítica do projeto (EAP)

1. Gestão: plano, cronograma, riscos, comunicação, mudanças.
2. Negócio e normas: comparação de regimentos, modelo de parametrização, validação jurídica.
3. Comercial e licitação: matriz de aderência, precificação, PoC, habilitação.
4. UX e acessibilidade: pesquisa, protótipos, linguagem simples, WCAG 2.1 AA/eMAG.
5. Desenvolvimento dos módulos M1 a M9.
6. Serviços transversais: motor de prazos, documentos, assinatura, auditoria, notificação.
7. Segurança: modelo de ameaças, selo, HSM/KMS, break-glass, ASVS nível 2, pentests.
8. Integrações: interface padrão + adaptadores por órgão (multas, gov.br, SNE/CDT, RENAINF, Diário Oficial, assinador).
9. Implantação automatizada: infraestrutura como código, instalador, migração de configuração entre versões.
10. Dados: modelo, migração de acervo (opcional), anonimização, dados abertos.
11. Qualidade: testes, carga, E2E de sessão, regressão do sigilo, testes por configuração de regimento.
12. Implantação por contrato: piloto, ondas, treinamento, suporte.
13. Sustentação: atualização anual da plataforma, correções, versões para todas as instalações.

---

## 8. Equipe estimada

### 8.1 Time de produto (Trilha A)
| Papel | Qtd. |
|---|---|
| Gerente de produto / projeto | 1 |
| Analista de negócio com base jurídica em trânsito | 1–2 |
| Arquiteto de software | 1 |
| Engenheiro de segurança/criptografia | 0,5–1 |
| Backend Java | 4–5 |
| Frontend React | 2 |
| UX/acessibilidade | 1 |
| QA/automação | 2 |
| DevOps/SRE | 1 |

Total: 13 a 16 pessoas.

### 8.2 Time de proposta (Trilha B, por edital)
Gerente comercial, analista de negócio, arquiteto e um especialista em licitações (parcial).

### 8.3 Time de implantação (Trilha C, por contrato)
| Papel | Qtd. |
|---|---|
| Gerente de projeto | 1 |
| Analista de negócio / parametrização | 1 |
| Desenvolvedor de integrações | 1–2 |
| DevOps | 0,5–1 |
| Suporte e treinamento | 1 |

Total: 4 a 6 pessoas por contrato, apoiadas pelo time de produto.

---

## 9. Backlog macro priorizado (épicos)

| Prioridade | Épico | Etapa |
|---|---|---|
| Crítico | Modelo de parametrização de regimento por instalação | A0–A1 |
| Crítico | Distribuição semanal com selo commit–reveal e falha fechada | A1 |
| Crítico | Abertura de sessão com quórum e revelação | A1 |
| Crítico | Motor de prazos com calendário parametrizável | A1 |
| Crítico | Auditoria append-only encadeada | A1 |
| Crítico | Protocolo assinado com recibo e hash | A1 |
| Alto | Instalador automatizado e atualização de instalações | A2 |
| Alto | Interface padrão de integração com sistema de multas | A2 |
| Alto | Assistente de peticionamento | A2 |
| Alto | Ambiente do relator (relatar → votar → próximo) | A1 |
| Alto | Modos de designação sigiloso e aberto | A1 |
| Alto | Remessa ao CETRAN e retorno | A3 |
| Médio | Credenciamento e ciclo de vida dos membros | A3 |
| Médio | Retorno sistêmico de falhas de sinalização | A3 |
| Médio | Painéis e relatórios regimentais | A3 |
| Médio | Demandas judiciais | A3 |
| Baixo | Precedentes com busca e apoio de IA | A3 |
| Condicionado | Sessão virtual assíncrona | onde o regimento permitir |

---

## 10. Marcos e critérios de aceite

| Marco | Quando (est.) | Critério |
|---|---|---|
| Plano aprovado | Produto, mês 1 | Empresa aprova trilhas, investimento e órgãos-alvo |
| Modelo de configuração validado | Produto, mês 2 | Regimentos de 3 a 5 órgãos cabem na parametrização |
| Núcleo demonstrável | Produto, mês 6 | Sessão completa simulada, com designação revelada só na abertura e verificável |
| Pronto para PoC | Produto, mês 9 | PoC ensaiada dentro do prazo típico de edital |
| Configuração aprovada | Contrato, mês 2 | Órgão aprova a parametrização |
| Primeira sessão real | Contrato, mês 6 | Junta-piloto julga sessão completa no sistema |
| Todas as juntas | Contrato, mês 9 | Ciclo completo sem intervenção manual |

Os nove critérios de aceite da seção 13 do enunciado viram critérios de aceite do produto e roteiro base da PoC.

---

## 11. Governança

- **Comitê de produto** (mensal): direção da empresa, gerente de produto, arquiteto. Decide roadmap, investimento e órgãos-alvo.
- **Comitê de regras** (quinzenal na A0, depois mensal): analista jurídico, analista de negócio, arquiteto. Mantém o modelo de parametrização e a comparação de regimentos.
- **Comitê de segurança** (por release): arquiteto, segurança, auditor independente. Aprova algoritmo, selo e pentests.
- **Por contrato:** comitê com o órgão contratante, conforme exigido no edital.
- Sprints de 2 semanas; ADRs no repositório; versionamento semântico do produto com notas de versão por instalação.

---

## 12. Riscos

| Risco | Prob. | Impacto | Mitigação |
|---|---|---|---|
| Produto não estar pronto quando sair o primeiro edital | Média | Crítico | Priorizar o núcleo demonstrável; acompanhar editais publicados |
| PoC com prazo curto e roteiro específico do órgão | Alta | Alto | Roteiro de PoC ensaiado; parametrização rápida do regimento |
| Edital exigir tecnologia ou requisito fora do produto | Média | Alto | Matriz de aderência; pedido de esclarecimento ou impugnação no prazo |
| Falta de atestados de capacidade técnica | Média | Crítico | Levantar atestados existentes; avaliar consórcio |
| Preço inexequível ou acima do mercado | Média | Alto | Modelo de custo por instalação e por adaptador |
| Regimentos muito diferentes entre estados | Média | Alto | Comparação de regimentos já na A0 |
| API de multas de cada órgão ruim ou inexistente | Alta | Alto | Interface padrão + adaptador por arquivo/banco; esforço precificado por edital |
| HSM/KMS não fornecido pelo contratante | Média | Alto | Plano B documentado; HSM em nuvem como item de proposta |
| Muitas instalações em versões diferentes | Alta | Médio | Instalador automatizado; política de suporte de versões |
| Atualização anual do Spring Boot não prevista nos contratos | Alta | Alto | Incluir na sustentação de cada contrato |
| Captura ou vazamento da designação | Média | Crítico | Selo, ArchUnit, auditoria de consulta, revisão independente, pentest |
| Acervo legado subdimensionado | Alta | Alto | Item opcional, precificado só após inventário |

---

## 13. Estratégia de qualidade

- Suíte obrigatória das regras RN19 a RN30, com casos positivos e negativos.
- Testes executados contra várias configurações de regimento (SP como base + regimentos de órgãos-alvo).
- Testes de propriedade da distribuição: equidade, conexão, rodízio sem repetição antes de esgotar as combinações.
- Regressão de sigilo: nenhum endpoint, relatório ou log expõe a designação antes da abertura, no modo sigiloso.
- E2E de sessão completa (Playwright), axe-core em pipeline, Testcontainers.
- Carga com o cenário de SP (27 juntas abrindo sessão no mesmo minuto + rajada de protocolos) como teto de referência.
- Teste de instalação e de atualização do zero em ambiente limpo a cada release.

---

## 14. Implantação, treinamento e transição (por contrato)

- Piloto com uma junta voluntária e suporte presencial nas primeiras sessões.
- Ondas de juntas conforme o porte do órgão.
- Treinamento no sandbox regimental; material ajustado ao regimento do órgão.
- Páginas de serviço ao cidadão geradas a partir da mesma parametrização do motor de prazos.
- Transferência de conhecimento e documentação conforme exigido no edital.

---

## 15. Perguntas em aberto

### 15.1 Respondidas em 06/10/2026
D1 a D8 (seção 2) e E1 (o plano serve para proposta e concorrência licitatória).

L1 a L5 foram respondidas em 06/10/2026 (seção 2).

### 15.2 Edital de Curitiba (CT)
- CT1. Qual é o número do edital de Curitiba, e você pode anexar o edital e o termo de referência aqui?
- CT2. Quais são as datas de entrega da proposta, da sessão de lances e da prova de conceito?
- CT3. Qual o prazo de implantação exigido após a assinatura do contrato?
- CT4. O edital exige produto pronto na prova de conceito ou aceita desenvolvimento durante o contrato?
- CT5. Quais outros editais em andamento a empresa acompanha (órgão e data)?

### 15.3 Estratégia de licitação e produto (L)
- L6. Os editais-alvo costumam exigir prova de conceito? Com que prazo?
- L7. Qual critério de julgamento é esperado: menor preço ou técnica e preço?
- L8. A empresa tem atestados de capacidade técnica em sistemas de processo administrativo ou trânsito?
- L9. A hospedagem será do órgão, da empresa (serviço gerenciado por instalação) ou de nuvem contratada pelo órgão?
- L10. A plataforma Java/JavaScript é escolha da empresa ou exigência de algum edital?
- L11. O código-fonte será cedido ao órgão, ou a empresa mantém a propriedade e licencia?
- L12. Qual modelo de remuneração a empresa prefere: licença + implantação + sustentação, UST, ou preço global?
- L13. Há orçamento e equipe definidos para a Trilha A, e quando a equipe pode começar?

### 15.4 Checklist de descoberta por órgão contratante (respondido na etapa C0)
Infraestrutura
- Onde será hospedado e se há HSM/KMS disponível.
- Se o órgão opera Kubernetes e aceita runtime Node em produção.
- Se há design system institucional a seguir.

Integrações
- Qual é o sistema de multas, quem o mantém, e se tem API ou integração por arquivo/banco.
- Se há ambiente de homologação do sistema de multas.
- Se o órgão aderiu ao gov.br e ao SNE/CDT.
- Qual Diário Oficial e se há API de publicação.
- Se há integração com SEI ou outro sistema de processo.
- Qual assinatura: gov.br avançada, ICP-Brasil ou as duas.
- Quais sistemas atuais o SIREJ substitui e quais continuam em paralelo.

Regras do regimento
- Se uma decisão com 2 votos é válida ou se sempre são exigidos 3.
- Se presidente e vice precisam ficar em turmas diferentes.
- Se o membro acessa os autos fora do dia e horário da reunião.
- Se a votação é sequencial ou paralela.
- Qual é o prazo de relatoria.
- Quais passos do roteiro de reunião são obrigatórios.
- Qual o prazo-meta interno para análise da defesa da autuação.
- Se os nomes dos julgadores aparecem após o julgamento.
- Como tratar recursos conexos em semanas diferentes.
- Como tratar membro que sai com processos conexos atribuídos à sua posição.

Credenciamento e composição
- Se a inscrição de entidades e as provas serão eletrônicas.
- Se a seleção dos representantes da comunidade entra no escopo.
- Se a gratificação por presença é calculada no sistema.
- Se a consulta ao CADIN tem API.

Dados e acervo
- Tamanho do acervo legado (processos e terabytes).
- Se processos em andamento migram ou terminam no sistema antigo.
- Qual política de temporalidade documental se aplica.

Segunda instância
- Procedimentos do CETRAN do estado para receber recursos.
- Se o CETRAN tem sistema para receber autos eletronicamente.

### 15.5 Sobre este plano
- E2. Precisa de estimativa de custo em reais e esforço em horas por trilha?
- E3. Há metodologia de plano exigida pelos editais-alvo (PMBOK, ágil)?

---

## 16. Próximos passos

1. Receber o edital e o termo de referência de Curitiba (CT1 a CT4) e escolher a estratégia da tabela da seção 2.1.
2. Montar a matriz de aderência do edital de Curitiba.
3. Ler a Lei municipal 15.154/2017 e incluí-la, com o regimento de SP e o de um DETRAN, na comparação de regimentos da A0.
4. Montar a equipe da Trilha A e começar a A0.
5. Fechar a especificação do algoritmo de distribuição e do selo, por ser o componente de maior risco.
