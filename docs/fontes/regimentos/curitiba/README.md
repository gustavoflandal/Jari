# Curitiba (SMDT) — fontes do regimento da JARI

**Situação em 06/10/2026: texto ausente.** Nenhuma norma da JARI de Curitiba está no repositório. A análise de Curitiba no `docs/dev/17-comparacao-regimentos.md` está parada (D-36 no doc 16) até que os documentos abaixo sejam colocados nesta pasta.

O que existe hoje sobre Curitiba vem só da página institucional (`transito.curitiba.pr.gov.br`) e está em `config/regimentos/curitiba.yaml` e no plano (seção 2.1): órgão (SMDT), 2ª instância (CETRAN-PR), 4 juntas com 6 titulares e 3 suplentes cada, protocolo pelas plataformas do Paraná. Esses fatos precisam ser confirmados na norma.

## O que falta

Cada item indica os temas do doc 06 que ele permite preencher.

| # | Documento | Temas que preenche |
|---|---|---|
| 1 | Texto integral e consolidado da **Lei municipal nº 15.154/2017**, com todas as alterações posteriores | Composição, segmentos, posições, mandato, 2ª instância |
| 2 | Decreto regulamentador da Lei 15.154/2017, se houver | Todos |
| 3 | **Ato que aprova o regimento interno** da JARI de Curitiba (decreto, portaria da SMDT ou resolução), com o texto do regimento vigente e o histórico de versões | Quórum, roteiro de sessão, turmas, votação, resultados possíveis, impedimentos, distribuição, sigilo da designação |
| 4 | Atos de criação das 4 juntas e portarias de nomeação vigentes (titulares, suplentes, presidente, vice, coordenação), indicando o segmento de cada membro | Composição, segmentos, posições, suplentes (D-11) |
| 5 | Normas e editais de indicação ou credenciamento dos representantes (comunidade, entidades, órgão), requisitos e vedações de membro | Composição, segmentos |
| 6 | Norma sobre a distribuição dos recursos entre juntas e membros: critério, periodicidade, conexão, sigilo, redistribuição, tratamento de falha | Distribuição, sigilo da designação |
| 7 | Norma sobre as sessões: periodicidade, quórum de abertura e de deliberação, turmas, substituição de ausentes, roteiro, modalidade (presencial, virtual, híbrida), sustentação oral, diligência | Quórum, roteiro de sessão |
| 8 | Rol de resultados de julgamento e modelos de decisão ou acórdão usados hoje | Resultados possíveis |
| 9 | Regras de impedimento e suspeição dos membros | Impedimentos |
| 10 | Regras de publicidade: publicação de pauta e de decisões (Diário Oficial do Município), divulgação de nomes dos julgadores | Publicidade, sigilo da designação |
| 11 | Prazos locais além do CTB (exigência, informação do agente, relatoria) e calendário de feriados municipais | Prazos |
| 12 | Normas e procedimentos do **CETRAN-PR** para remessa e instrução do recurso de 2ª instância | 2ª instância |
| 13 | Normas estaduais de identidade digital e protocolo eletrônico usadas hoje (identidade digital PR, PIÁ, Detran InteliGente) | Identidade (D-12) |
| 14 | Edital de Curitiba e termo de referência, quando publicados | Todos (matriz de aderência) |

## Como incluir

Para cada documento: arquivo original (PDF) mais o texto extraído em Markdown, com cabeçalho de origem (endereço ou meio de obtenção, data de acesso, ato e data de publicação), no mesmo formato de `docs/fontes/regimentos/sp/regimento-jari-sp.md`. Depois, preencha a coluna de Curitiba no doc 17 e ajuste o `curitiba.yaml` citando o artigo em cada valor.
