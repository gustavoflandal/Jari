# SIREJ — instruções para agentes de desenvolvimento

O SIREJ (Sistema Integrado de Recursos de Infrações de Trânsito) é um produto para o processo administrativo eletrônico das JARIs. Ele será vendido por licitação a órgãos municipais e estaduais de trânsito, com **uma instalação por órgão contratante**. O regimento das JARIs de São Paulo é a **configuração de referência**; Curitiba é o primeiro alvo comercial.

Idioma: português do Brasil em documentação, mensagens de commit, nomes de domínio e textos de interface.

## Leia antes de qualquer tarefa

1. `docs/dev/00-indice.md` — mapa da documentação de referência.
2. `docs/dev/15-protocolo-agentes.md` — como um agente recebe, executa e entrega um pacote de trabalho.
3. O documento específico do contexto em que você vai mexer (o índice diz qual).

A fonte de verdade de requisitos é `docs/dev/`. Os arquivos em `docs/fontes/` são material de origem: consulte-os para contexto, mas, em caso de conflito, vale `docs/dev/` e os ADRs em `docs/adr/`.

## Invariantes que nenhuma mudança pode quebrar

Uma violação destas regras é bug crítico, mesmo que todos os testes passem. Detalhes e testes obrigatórios em `docs/dev/04-dominio-e-regras.md` e `docs/dev/07-distribuicao-e-selo.md`.

1. **Sigilo da designação.** Com `designacao.modo = SIGILOSO`, nenhum perfil (secretaria, administrador, DBA, suporte, fornecedor) consegue saber junta/posição/relator/revisor/3º membro de um processo antes da abertura da sessão. Toda leitura da designação passa pelo módulo `distribuicao` e gera registro de auditoria.
2. **Falha fechada.** A distribuição semanal é tudo-ou-nada. Se falhar, nada é distribuído e os processos entram na semana seguinte. **Não existe, e não pode ser criada, nenhuma rota de distribuição manual.**
3. **Sem voto de qualidade.** Cada voto tem o mesmo peso. Não existe desempate, peso diferenciado ou voto de minerva.
4. **Rol fechado de resultados.** Os resultados possíveis vêm da configuração do regimento (em SP: cinco). Não existe provimento parcial.
5. **Nada é apagado.** Documentos, movimentações e votos são imutáveis. Correção = novo registro (desentranhamento, retificação), nunca `UPDATE`/`DELETE` do original.
6. **Trilha de auditoria append-only e encadeada por hash.** Todo ato relevante gera registro; nenhum código altera ou remove registros de auditoria.
7. **Nenhum pagamento como condição para recorrer.** Nenhuma tela ou regra exige recolhimento da multa (Súmula Vinculante 21).
8. **Regimento é configuração.** Nenhuma regra que varie entre órgãos (prazos, quórum, composição, resultados, roteiro, publicidade) pode ficar fixa no código. Ver `docs/dev/06-parametrizacao.md`.
9. **O SIREJ não altera pontuação nem débito.** Mudanças de situação de penalidade saem como eventos de integração para o sistema de multas do órgão.
10. **Nenhuma decisão automática por IA.** Recursos de IA só apoiam; toda decisão tem autor humano identificado e assinatura.

## Stack

- Backend: Java 25 LTS, Spring Boot 4.1, Spring Modulith, PostgreSQL 17+, Flyway, JPA (CRUD) + jOOQ (consultas críticas), Spring Batch + ShedLock, Keycloak (OIDC), S3 compatível com Object Lock, DSS (assinatura PAdES), Apache PDFBox, ClamAV.
- Frontend: TypeScript estrito, React 19, Vite, TanStack Query, Zod, GovBR-DS (ou design system do órgão), TipTap, PDF.js.
- Testes: JUnit 5, ArchUnit, Testcontainers, jqwik (propriedades), Playwright, axe-core.

## Layout do repositório (alvo)

```
backend/          Maven multi-módulo, um módulo Spring Modulith por contexto
frontend/
  apps/portal/    Portal do recorrente
  apps/backoffice/ Back-office da JARI
  packages/tipos/ Tipos e schemas Zod compartilhados
config/regimentos/ Configurações de referência (sp.yaml, curitiba.yaml)
docs/dev/         Documentação de referência (fonte de verdade)
docs/adr/         Decisões de arquitetura
docs/fontes/      Material de origem
docs/plano/       Plano do projeto
```

## Convenções essenciais

Resumo; regras completas em `docs/dev/13-convencoes.md`.

- Nomes de domínio em português, sem acento (`Processo`, `Junta`, `pauta_item`); sufixos técnicos em inglês (`ProcessoRepository`, `PautaController`).
- Commits no padrão Conventional Commits, em português (`feat(distribuicao): ...`).
- Todo PR referencia o pacote de trabalho (`PT-xx`) de `docs/dev/14-backlog-pacotes.md` e as regras (`RNxx`) que implementa.
- Nenhum módulo acessa tabela de outro módulo; a comunicação é por API pública do módulo ou por evento.

## Definição de pronto

Um pacote de trabalho só está pronto quando: os critérios de aceite do pacote passam em teste automatizado; cada `RNxx` tocada tem teste positivo e negativo; `./mvnw verify` e os testes do frontend passam; ArchUnit passa; nenhuma invariante acima foi enfraquecida; a documentação em `docs/dev/` foi atualizada se o comportamento mudou.

## Quando houver dúvida

Não invente regra de negócio. Se a documentação não responde, registre a dúvida em `docs/dev/16-duvidas-abertas.md` (com o pacote, a pergunta e a opção que você adotou provisoriamente) e siga com a opção mais restritiva, que preserva sigilo, auditoria e falha fechada.
