# 01 — Visão e escopo

## O produto em uma frase

Um sistema web que conduz o recurso contra penalidade de trânsito do protocolo pelo cidadão até a decisão de 2ª instância e o cumprimento, com autos 100% digitais, prazos do CTB controlados automaticamente, distribuição impessoal e auditável, sessão colegiada digital e validade jurídica dos atos.

## Contexto comercial que afeta o código

- **Vários clientes, uma base de código.** Cada órgão contratante (prefeitura ou DETRAN) recebe a sua própria instalação, com configuração própria. Não há multi-tenant em runtime: uma instalação atende um órgão. Ver ADR-0007.
- **Regimentos diferentes.** Cada órgão tem regimento, CETRAN, feriados, prazos e sistema de multas próprios. Tudo o que varia é configuração (doc 06).
- **Escopo por edital.** O produto é modular. Núcleo sempre presente: Secretaria (M3), Relatoria e Julgamento (M4), Administração (M9). Demais módulos são ativados por configuração (`modulos.*`).
- **Referência:** a configuração `config/regimentos/sp.yaml` (JARI do Município de São Paulo) é a referência. Tudo deve funcionar primeiro nela. A segunda configuração, `curitiba.yaml`, prova a parametrização.

## O que a escala exige

| Dimensão | Referência SP | Alvo de projeto |
|---|---|---|
| Recursos por mês | ~25 mil | 75 mil |
| Juntas | 27 (6 membros cada) | 80 |
| Membros simultâneos em sessão | ~162 | ~500 |
| Processos por membro por sessão | ~38 | ~110 |
| Documentos por processo | 6 a 12 | 20 |

Curitiba: 4 juntas, 6 titulares e 3 suplentes por junta. Um DETRAN estadual pode ter outra escala; a instalação é dimensionada por contrato.

O sistema **não é de alto throughput**. Ele é de **altíssima exigência de integridade** e **pesado em documentos**. Prioridade de decisão: integridade > disponibilidade; documentos > transações; simplicidade operacional > elasticidade.

## Módulos

| Id | Módulo | Ativação |
|---|---|---|
| M1 | Portal do Recorrente | opcional |
| M2 | Autuação e Instrução (órgão autuador) | opcional |
| M3 | Secretaria da JARI | núcleo |
| M4 | Relatoria e Julgamento | núcleo |
| M5 | Segunda Instância | opcional |
| M6 | Credenciamento e Composição | opcional (cadastro mínimo de membros é núcleo) |
| M7 | Transparência e Indicadores | opcional |
| M8 | Demandas Judiciais | opcional |
| M9 | Administração | núcleo |

O mapeamento de módulo de produto para módulo de código está no doc 03.

## Fora do escopo (não implementar)

- Lavratura de AIT, cálculo de multa, arrecadação, pontuação, cobrança, parcelamento, guias.
- Fiscalização eletrônica e gestão de equipamentos.
- Peticionamento judicial e integração com tribunais.
- Decisão automática por IA.
- Sustentação oral do recorrente (proibida pelo regimento de referência, art. 20).

## Critérios de aceite do produto

1. Recurso protocolado por celular, autenticado, em menos de 5 minutos, sem exigir documento que a administração já possui.
2. O recorrente sabe, sem atendimento humano: fase atual, junta responsável (quando a configuração permitir), data prevista de sessão e prazo em curso.
3. Nenhuma distribuição ocorre por decisão humana; o sorteio é automático, a semente é selada e verificável depois; no modo sigiloso o resultado só fica legível na abertura da sessão.
4. É impossível, por qualquer perfil, descobrir antecipadamente quem julgará um processo no modo sigiloso, e a tentativa fica registrada.
5. Pauta, ata e acórdão são gerados, assinados e publicados sem redigitação.
6. O sistema reporta diariamente os processos que ultrapassaram o prazo legal de julgamento.
7. Qualquer processo pode ser reconstituído integralmente a partir da trilha de auditoria (quem viu, quem assinou, quando).
8. Nenhuma tela ou regra exige pagamento para recorrer.
9. Os autos completos podem ser exportados em pacote assinado e verificável por terceiro.
