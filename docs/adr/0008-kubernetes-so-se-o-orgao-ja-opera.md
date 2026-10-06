# ADR-0008: Kubernetes só se o órgão já opera

**Situação:** aceita (06/10/2026)

## Contexto
São de 5 a 8 componentes. Kubernetes introduzido pelo projeto tende a virar o principal risco operacional.

## Decisão
Padrão: contêineres em 2 ou 3 VMs com orquestração simples. Kubernetes apenas onde o órgão já tem equipe que o opera.

## Consequências
Menor custo cognitivo; o instalador precisa suportar os dois alvos.
