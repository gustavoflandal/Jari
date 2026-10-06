# 13 — Convenções de código e repositório

## Idioma e nomes

- Domínio em português sem acento: `Processo`, `PautaItem`, `registrarVoto()`, `situacao`.
- Sufixos e termos técnicos em inglês: `Controller`, `Service`, `Repository`, `Port`, `Adapter`, `Config`, `Exception`, `Event` não (eventos usam nome do domínio no passado: `PecaProtocolada`).
- Use somente termos do glossário (doc 02). Termo novo → adicione ao glossário no mesmo PR.

## Backend

- Java 25: `record` para objetos de valor, eventos e comandos; `sealed` para hierarquias fechadas; virtual threads habilitadas.
- Estrutura por módulo:
  ```
  br.com.sirej.<modulo>/
    <Api pública>.java          # interfaces e records expostos
    api/                        # se a API pública for grande
    dominio/                    # agregados, regras, eventos (sem Spring)
    aplicacao/                  # casos de uso, transações
    infraestrutura/ (internal)  # JPA, jOOQ, adaptadores locais
    web/ (internal)             # controllers
  ```
- Domínio sem dependência de Spring, JPA ou bibliotecas externas.
- Casos de uso transacionais em `aplicacao`; um caso de uso por comando.
- Exceções de domínio específicas (`TransicaoInvalidaException`, `PrazoVencidoException`), mapeadas para `problem+json` na camada web.
- Tempo sempre via `Relogio` injetável.
- Migrações: `V{yyyyMMddHHmm}__{modulo}_{descricao}.sql`, uma por mudança, nunca editar migração já mesclada.

## Frontend

- `strict: true`; sem `any`.
- Componentes funcionais; estado de servidor só em TanStack Query.
- Tipos gerados do OpenAPI; nunca escrever tipos de API à mão.

## Git

- Branch padrão `main`, protegida.
- Branches: `feat/PT-xx-descricao-curta`, `fix/...`, `docs/...`.
- Commits Conventional Commits em português: `feat(distribuicao): sorteio semanal com grupos de conexão (PT-11)`.
- PR pequeno: um PT por PR (ou parte claramente delimitada de um PT). Descrição com Antes/Depois, `PT-xx`, `RNxx` e como testar.
- Revisão obrigatória. PR que toca `distribuicao`, `auditoria`, `assinatura` ou `identidade` exige revisão de segurança.

## Configuração e segredos

- Nada de segredo no repositório. Variáveis com prefixo `SIREJ_`.
- `application.yaml` só com valores técnicos; regras de negócio vêm do regimento (doc 06).

## Logs e observabilidade

- Logs estruturados em JSON, com `traceId` (OpenTelemetry).
- Proibido logar: semente, sal, chaves, designação não revelada, CPF completo, tokens, conteúdo de votos antes da proclamação.
- Métricas por caso de uso (latência, erros) e de negócio (protocolos/dia, backlog, processos fora do prazo).
