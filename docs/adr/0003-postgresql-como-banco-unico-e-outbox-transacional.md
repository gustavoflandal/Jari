# ADR-0003: PostgreSQL como banco único e outbox transacional

**Situação:** aceita (06/10/2026)

## Contexto
Volume cabe folgadamente num PostgreSQL; um broker a mais é uma superfície de operação a mais.

## Decisão
PostgreSQL 17+ para dados, full-text e outbox. Broker (RabbitMQ) só se surgir necessidade comprovada.

## Consequências
Menos componentes. Publicador do outbox precisa de retry, fila de mortos e monitoramento.
