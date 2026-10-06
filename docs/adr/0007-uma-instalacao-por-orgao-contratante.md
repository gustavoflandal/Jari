# ADR-0007: Uma instalação por órgão contratante

**Situação:** aceita (06/10/2026)

## Contexto
O produto é vendido por licitação a órgãos distintos, cada um com seu contrato, hospedagem e dados. Decisão de Gustavo em 06/10/2026.

## Decisão
Sem multi-tenant em runtime. Mesmo artefato para todos; configuração do regimento por instalação (doc 06); instalação e atualização automatizadas.

## Consequências
Isolamento total de dados e simplicidade de código. Custo: automação de implantação e gestão de várias instalações em versões possivelmente diferentes.
