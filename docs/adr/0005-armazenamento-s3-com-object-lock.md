# ADR-0005: Armazenamento S3 com Object Lock

**Situação:** aceita (06/10/2026)

## Contexto
Documentos são imutáveis por norma (RN11) e o volume de objetos é o maior custo.

## Decisão
Objetos em armazenamento S3 compatível, endereçados por SHA-256, com versionamento e Object Lock em modo de conformidade.

## Consequências
Imutabilidade garantida fora da aplicação. Exige provedor com Object Lock em todos os cenários de hospedagem.
