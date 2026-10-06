# ADR-0009: SSR do portal condicionado a Node em produção

**Situação:** aceita (06/10/2026)

## Contexto
SSR melhora primeira pintura em rede ruim, mas exige runtime Node que nem todo órgão aceita operar.

## Decisão
SPA com pré-renderização estática das páginas informativas como padrão; Next.js com SSR onde o órgão aceitar Node.

## Consequências
Portal funciona nos dois modos; mais um eixo de configuração de implantação.
