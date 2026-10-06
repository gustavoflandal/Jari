# 19 — Operação e infraestrutura

Como o SIREJ é empacotado, instalado, observado, protegido contra perda de dados e atualizado em cada órgão. Este documento é a referência para o PT-01 (pipeline), o PT-27 (instalador) e para as etapas C0, C1 e C4 da implantação. A origem é `docs/fontes/arquitetura-infraestrutura-sirej.md`, seções 4, 6, 7 e 8, adaptada à decisão de uma instalação por órgão (ADR-0007).

## 1. Princípio: o mesmo artefato em qualquer hospedagem

A hospedagem é decidida pelo contrato de cada órgão, não pela engenharia. O produto é **portável por construção**: contêineres, PostgreSQL, armazenamento S3 compatível com Object Lock e OIDC. O único item que não é genérico é o cofre de chaves (HSM ou KMS), exigido pelo selo da distribuição. Ele fica sempre atrás da `CofreChavesPort` (doc 09).

| Cenário | Quando | Atenção |
|---|---|---|
| A. Nuvem pública em região brasileira | O órgão não tem empresa pública de TI com os serviços abaixo | Usar só serviços com equivalente nos outros cenários; nada proprietário fora das portas |
| B. Empresa pública de TI do ente | Oferece PostgreSQL gerenciado, S3 com Object Lock e serviço de chaves | Cenário preferido: rede próxima do sistema de multas |
| C. Datacenter do próprio órgão | Só se já houver equipe de operação 24×7 | Exige HSM físico e segundo site para DR |

Nenhum código ou script pode depender de um cenário específico. O que muda entre eles fica no inventário de implantação do órgão (seção 4).

## 2. Componentes

| Componente | Imagem/serviço | Estado | Observação |
|---|---|---|---|
| API / núcleo (monólito modular) | `sirej-api` | Sem estado | Spring Boot, virtual threads; escala horizontal |
| Serviços de apoio | `sirej-apoio` | Sem estado | ClamAV, renditions, PDF/A, assinatura (DSS) |
| Portal do recorrente | `sirej-portal` | Sem estado | Estáticos (ou SSR, ADR-0009) |
| Back-office | `sirej-backoffice` | Sem estado | Estáticos |
| Keycloak | Imagem oficial, versão fixada | Banco próprio | Federa gov.br, identidade estadual e AD do órgão |
| PostgreSQL 17+ | Gerenciado ou instalado | Com estado | Primário + réplica de leitura |
| Armazenamento de objetos | S3 compatível | Com estado | Versionamento + Object Lock em modo de conformidade (ADR-0005) |
| Redis | Gerenciado ou instalado | Cache | Sessão e cache; perda não perde dado de negócio |
| Cofre de chaves | HSM ou KMS | Crítico | Chave de envelope do selo; indisponível = distribuição suspensa e sessão não abre |

Orquestração: contêineres em 2 ou 3 VMs com orquestração simples (Docker Compose ou equivalente); Kubernetes só onde o órgão já o opera (ADR-0008). O instalador suporta os dois alvos.

**Relógio.** Todas as máquinas sincronizam com NTP de fonte confiável (ex.: NTP.br). A aplicação expõe a deriva em métrica e alerta acima de 1 s. Os prazos, a ordem cronológica e o carimbo do tempo dependem disso.

## 3. Ambientes

| Ambiente | Propósito | Dados | Chaves |
|---|---|---|---|
| Local / devcontainer | Desenvolvimento | Sintéticos (fixtures) | KMS simulado |
| CI | Build e testes | Sintéticos, efêmeros (Testcontainers) | KMS simulado |
| Demonstração / PoC | Vendas e prova de conceito | Sintéticos | KMS simulado ou de teste |
| Homologação do órgão | Validação e homologação de integrações | Produção mascarada e anonimizada | KMS próprio, **nunca** o de produção |
| Sandbox de treinamento | Capacitação de membros e secretaria | Sintéticos, relógio controlável | KMS próprio |
| Produção | — | Reais | HSM/KMS de produção |

Regras:
- Nenhum ambiente fora de produção recebe dado pessoal real sem mascaramento (doc 08).
- Nenhuma chave, credencial ou integração de produção é compartilhada com outro ambiente.
- Sandbox e demonstração mostram uma faixa visível "AMBIENTE SEM VALOR JURÍDICO" em todas as telas e documentos gerados.

## 4. Instalação (PT-27)

Infraestrutura como código, num repositório de implantação por órgão, separado do produto. Ele contém:
- o inventário (cenário, endereços, tamanhos, provedores);
- os segredos por referência (nunca o valor);
- o regimento inicial;
- a versão do produto.

Sequência do instalador, idempotente:
1. Verifica pré-requisitos: versões, conectividade com banco, objeto, cofre de chaves, provedor de identidade e sincronização de relógio.
2. Provisiona os componentes da seção 2.
3. Aplica as migrações Flyway.
4. Na primeira instalação, carrega o regimento como versão 1 (doc 18 §6) e cria o primeiro `ADMIN`.
5. Gera a chave de envelope no cofre e testa envelopar e desenvelopar um valor de teste.
6. Roda o smoke test: login interno, protocolo sintético num ambiente que não é produção, verificação da cadeia de auditoria e verificação de um selo de teste.
7. Registra a instalação na auditoria com versão, hash do pacote e autor.

## 5. Configuração e segredos

- Variáveis técnicas com prefixo `SIREJ_` (doc 13). Regras de negócio nunca são variáveis de ambiente: vêm do regimento.
- Segredos (senhas de banco, credenciais de integração, certificados) ficam no Vault ou no gerenciador de segredos do provedor. A aplicação lê o segredo na subida e nunca o grava em log, arquivo ou banco.
- Rotação de credenciais sem parada: a aplicação aceita a credencial nova e a antiga durante a janela de rotação.

## 6. Observabilidade

- **Logs** JSON com `traceId` (OpenTelemetry), sem dado proibido (doc 13).
- **Rastros** OpenTelemetry da borda ao banco.
- **Métricas** técnicas (latência por caso de uso, erros, pool de conexões, fila do outbox) e de negócio (protocolos por dia, backlog por situação, processos fora do prazo).
- **Verificações de saúde:** `liveness` só verifica o processo. `readiness` verifica banco, objeto e cofre de chaves; sem cofre, a instância fica pronta para o restante, mas a distribuição e a abertura de sessão recusam a operação (falha fechada).

Alertas obrigatórios (cada um com runbook, seção 9):

| Alerta | Severidade |
|---|---|
| Distribuição semanal suspensa (`DistribuicaoSuspensa`) | Crítica |
| Cofre de chaves indisponível | Crítica |
| Divergência na verificação da cadeia de auditoria | Crítica |
| Ancoragem diária da auditoria atrasada | Alta |
| Disponibilidade abaixo da meta na janela de sessão | Alta |
| Mensagem na fila de mortos de integração | Média |
| Configuração do pacote divergente da vigente (doc 18 §6) | Média |
| Deriva de relógio acima de 1 s | Alta |
| Certificado ou credencial vencendo em 30 dias | Média |

## 7. Continuidade: backup, restauração e DR

Metas: RPO ≤ 15 min, RTO ≤ 4 h (RNF07).

| Item | Como | Verificação |
|---|---|---|
| PostgreSQL | PITR contínuo (WAL arquivado) com retenção mínima de 35 dias e cópia diária fora do ambiente de produção | Restauração trimestral registrada |
| Objetos | Versionamento + Object Lock + replicação para segundo local | Amostragem trimestral: hash do objeto restaurado = hash registrado |
| Auditoria | Ancoragem diária exportada para WORM fora da produção (doc 08) | Verificação diária da cadeia |
| Regimento | Versões no banco + exportação YAML a cada publicação no repositório de implantação | — |
| Chave de envelope do selo | Ver abaixo | Ensaio semestral |

**Chave do selo: o ponto mais delicado.** Perder a chave torna ilegíveis as designações seladas: nenhuma sessão daquele lote abre. Uma cópia de segurança mal guardada, por outro lado, quebra o sigilo (invariante 1). Por isso:
- A chave só existe dentro do HSM/KMS e nunca sai em texto claro.
- A redundância vem do próprio serviço: cluster de HSM, réplica em outra zona ou região, ou backup cifrado do HSM restaurável só em outro HSM.
- Qualquer operação de recuperação da chave exige controle dual (duas pessoas), fica registrada no cofre e na auditoria do SIREJ, e é comunicada ao coordenador.
- Se a chave for perdida de verdade, os lotes afetados não são redistribuídos por nenhuma rota manual. Os processos voltam a `AGUARDANDO_DISTRIBUICAO` por evento auditado e entram no lote seguinte, como numa falha da distribuição (RN24). Ver D-20.

**Restauração não reescreve o passado.** Depois de restaurar o banco para um ponto anterior, a cadeia de auditoria é verificada contra a última âncora exportada. Atos perdidos na janela do RPO são registrados como ocorrência, nunca recriados à mão.

## 8. Atualização de versão

- Cadência: releases do produto com notas de versão e SBOM. Uma atualização de plataforma por ano (ADR-0002).
- Migrações sempre retrocompatíveis; mudanças destrutivas em duas fases (doc 12).
- Implantação rolling, sem parada, **fora da janela de sessão e fora da execução da distribuição semanal**. O instalador recusa atualizar enquanto houver sessão aberta ou lote em execução.
- Antes e depois da atualização, verifique a cadeia de auditoria e rode o smoke test (seção 4, passo 6).
- Rollback ensaiado em homologação a cada release; nunca presumido.
- A atualização nunca altera a configuração do órgão (ADR-0011).

**Pipeline de integração (PT-01).** Em todo push para `main` e em todo PR, o GitHub Actions roda, sem depender de segredo ou chave de API:

| Workflow | Etapa | Bloqueia o PR quando |
|---|---|---|
| `ci.yml` › backend | `./mvnw -B verify` (testes, `ApplicationModules.verify()`), SBOM CycloneDX do backend publicado como artefato `sbom-backend` e SCA com OSV-Scanner sobre esse SBOM (dependências diretas e transitivas) | algum teste ou verificação falha, ou há qualquer vulnerabilidade conhecida |
| `ci.yml` › frontend | `npm ci`, `npm run typecheck`, `npm test`, `npm run build`, `npm audit --audit-level=high` (SCA) e SBOM CycloneDX do frontend, publicado como artefato `sbom-frontend` | algo falha ou há vulnerabilidade alta ou crítica |
| `codeql.yml` | SAST CodeQL (`security-extended`) em Java, TypeScript e nos próprios workflows; também semanal | alerta de segurança, conforme a proteção do branch |

As actions são fixadas por SHA de commit. O Dependabot (`.github/dependabot.yml`) propõe atualizações semanais de Maven, npm e actions. O `actions/dependency-review-action` só funciona com o Dependency graph ligado nas configurações do repositório. Quando estiver ligado, ele pode ser somado aos PRs como SCA adicional. A varredura de imagem (Trivy) e a assinatura de imagens entram quando houver imagens de contêiner (PT-27).

## 9. Runbooks obrigatórios

Cada um em `docs/operacao/runbooks/`, escrito no PT-27 e revisado a cada release:
- distribuição suspensa;
- cofre de chaves indisponível ou chave perdida;
- divergência na cadeia de auditoria;
- restauração de banco e de objetos;
- indisponibilidade do portal em fim de prazo (gera proposta de suspensão de expediente, doc 18 §5);
- fila de mortos de integração;
- break-glass (doc 08);
- rotação de certificados e credenciais.

## 10. Dimensionamento de partida

Ajustar depois do teste de carga (PT-28).

| Componente | Porte grande (referência SP: 27 juntas) | Porte pequeno (ex.: Curitiba: 4 juntas) |
|---|---|---|
| API / núcleo | 3 × 4 vCPU / 8 GB | 2 × 2 vCPU / 4 GB |
| Serviços de apoio | 2 × 2 vCPU / 4 GB | 1 × 2 vCPU / 4 GB |
| Keycloak | 2 × 2 vCPU / 4 GB | 2 × 1 vCPU / 2 GB |
| PostgreSQL | Primário 8 vCPU / 32 GB + réplica 4 vCPU / 16 GB | Primário 4 vCPU / 16 GB + réplica 2 vCPU / 8 GB |
| Objetos | 5 TB iniciais, ~1 TB/ano | Proporcional ao volume; estimar no C0 |
| Cofre de chaves | HSM/KMS gerenciado ou appliance | Idem; não há porte que dispense |

Os números do porte pequeno são estimativa da equipe (proporção de juntas) e precisam de confirmação no C0 com o volume real do órgão.

## 11. O que a operação nunca faz

1. Escrever direto no banco de produção fora do break-glass com controle dual.
2. Ler conteúdo de processo ou designação ao operar (o papel de infraestrutura não tem acesso de aplicação).
3. Disparar ou refazer distribuição por script.
4. Restaurar apenas parte do banco ou apagar registros para "corrigir" estado.
5. Usar a chave ou as credenciais de produção em outro ambiente.
