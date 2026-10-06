# ADR-0004: Event sourcing restrito ao processo

**Situação:** aceita (06/10/2026)

## Contexto
A movimentação processual é naturalmente um log imutável e a reconstituição integral do processo é critério de aceite.

## Decisão
A tabela `movimentacao` é a fonte de verdade do andamento; situação e linha do tempo são projeções. Os demais agregados são CRUD com auditoria.

## Consequências
Auditoria e linha do tempo quase de graça. Projeções precisam ser reconstruíveis e testadas.
