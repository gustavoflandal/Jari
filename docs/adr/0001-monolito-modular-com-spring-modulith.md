# ADR-0001: Monólito modular com Spring Modulith

**Situação:** aceita (06/10/2026)

## Contexto
O domínio é coeso e transacional: protocolo, numeração, hash e recibo precisam ser atômicos, assim como o lote de distribuição e a abertura da sessão. A carga é baixa (poucas requisições por segundo).

## Decisão
Um único deployable de backend, dividido em módulos Spring Modulith com fronteiras verificadas em build (doc 03).

## Consequências
Consistência transacional simples; operação barata. Exige disciplina de fronteiras (ArchUnit). Extrair um módulo para serviço só com motivo medido.
