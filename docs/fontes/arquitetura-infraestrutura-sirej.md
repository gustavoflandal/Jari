# Arquitetura Técnica e Infraestrutura — SIREJ
## Sistema Integrado de Recursos de Infrações de Trânsito (JARI)

**Versão:** 1.0
**Data:** 14/09/2026
**Documento companheiro de:** `enunciado-projeto-sistema-jari.md` (v1.0)
**Restrição fixa do cliente:** plataforma Java / JavaScript

---

## 1. O que este sistema realmente é, do ponto de vista técnico

Antes de escolher qualquer componente, vale nomear a natureza da carga, porque ela é contraintuitiva e leva a decisões erradas quando não é explicitada.

O SIREJ **não é um sistema de alto throughput**. Na jurisdição de referência são cerca de 18 a 26 mil recursos por mês, 27 juntas reunindo-se uma vez por semana, cerca de 38 processos por membro por reunião. Isso dá uma média de poucas requisições por segundo. Dimensionar como se fosse um e-commerce é desperdício.

O SIREJ **é um sistema de altíssima exigência de integridade**. O bem que ele protege não é a disponibilidade nem a latência: é a impossibilidade de alguém saber ou alterar quem vai julgar um processo antes da hora. O regimento chega a criar sanção funcional para o servidor que revelar a distribuição antes da reunião, e determina que a distribuição seja **suspensa** — não contornada — quando o sistema está indisponível. Em termos de engenharia, isso significa que o principal adversário do sistema **está dentro dele**: membro, secretário, administrador, DBA, fornecedor.

E o SIREJ **é pesado em documentos, não em transações**. O volume real está nas imagens do AIT, nos anexos do recorrente e no acervo histórico. É lá que vão o armazenamento, a banda e o custo.

Três frases que devem guiar toda decisão daqui em diante:

> **Integridade acima de disponibilidade. Documentos acima de transações. Simplicidade operacional acima de elasticidade.**

---

## 2. Perfil de carga dimensionado

| Dimensão | Valor de referência | Alvo de projeto (3×) |
|---|---|---|
| Recursos protocolados | ~25.000/mês | 75.000/mês |
| Processos por membro por sessão | ~38 | ~110 |
| Membros ativos simultâneos | ~162 (27 juntas × 6) | ~500 |
| Sessões simultâneas | até 54 turmas (2 por junta) | 160 |
| Documentos por processo | 6 a 12 | 20 |
| Tamanho médio de documento | ~350 KB (imagens de AIT dominam) | 1 MB |
| Cidadãos concorrentes no portal | dezenas, com picos em fim de prazo | milhares |

**Derivações de armazenamento**

- Documentos novos: ~25k × 8 × 350 KB ≈ **70 GB/mês** → ~850 GB/ano; com renditions derivadas, ~1 TB/ano.
- Retenção mínima ligada à prescrição quinquenal: **~5 TB** em regime.
- Acervo legado: em São Paulo há processos digitalizados desde 1973. Pode ser uma ordem de grandeza maior que o acervo corrente. **Não estimar sem inventário** — é o maior risco de custo do projeto.
- Banco relacional: ~30 M linhas em 5 anos nas tabelas de negócio; a tabela de eventos/auditoria é a maior, na casa de 100 a 200 M linhas, exigindo particionamento mensal.

**Onde estão os picos reais**

1. **Manhã de sessão**: 162 membros abrindo autos simultaneamente. Pico de leitura de objetos, não de CPU. Mitigação: renditions pré-geradas e cache de borda.
2. **Véspera de vencimento de prazo**: rajada de protocolos com upload. Mitigação: recebimento assíncrono com recibo imediato, processamento posterior.
3. **Job semanal de distribuição**: janela curta, cara e crítica. Roda uma vez, com trava, idempotente, auditada.

---

## 3. Stack — decisões e justificativas

A restrição Java/JavaScript é compatível com tudo que este sistema precisa. Ela custa algo em densidade de recursos, e ganha muito em disponibilidade de mão de obra e em maturidade das bibliotecas de assinatura digital e documentos — que aqui pesam mais.

### 3.1 Backend

| Camada | Escolha | Por quê |
|---|---|---|
| Runtime | **Java 25 LTS** (Eclipse Temurin) | LTS vigente; virtual threads maduras, decisivas para a carga de fan-out de I/O deste sistema |
| Framework | **Spring Boot 4.1** (Spring Framework 7, Jakarta EE 11) | Ecossistema de segurança, batch e integração mais completo do mundo Java; maior oferta de profissionais no mercado brasileiro, o que importa em sistema público de ciclo longo |
| Modularização | **Spring Modulith** | Impõe fronteiras entre contextos dentro do monólito modular e traz registro de eventos com outbox transacional nativo |
| Persistência | **PostgreSQL 17+**, Flyway, JPA para CRUD e **jOOQ** para consultas quentes e analíticas | ORM resolve bem o cadastro; a distribuição e os relatórios precisam de SQL explícito e previsível |
| Lote | **Spring Batch** + trava distribuída (ShedLock) | A distribuição semanal é um job que não pode rodar duas vezes nem pela metade |
| Mensageria | **Outbox transacional em Postgres** primeiro; RabbitMQ apenas se surgir necessidade real | Um broker a menos é uma superfície de operação a menos. Nesse volume, o Postgres dá conta |
| Busca | Full-text nativo do Postgres (`tsvector`) | A base de precedentes cabe folgadamente; OpenSearch só se a busca virar produto |
| Objetos | **S3-compatível** (MinIO on-premises ou serviço equivalente na nuvem) com versionamento e **Object Lock** | Imutabilidade documental é requisito, não conveniência |
| Identidade | **Keycloak** como broker, federando **gov.br** (OIDC) para cidadãos e LDAP/AD para internos | Centraliza papéis, MFA e sessão; é Java e amplamente usado no setor público brasileiro |
| Documentos | **Apache PDFBox** (PDF/A), **libvips** via binding Java para renditions, **ClamAV** para antivírus | PDF/A-2b e derivadas de imagem rápidas e baratas |
| Assinatura | **DSS (esig)** para PAdES + provedor ICP-Brasil e assinatura avançada gov.br; carimbo do tempo de ACT credenciada | Validade jurídica dos atos é requisito do enunciado |
| Cache | **Redis** para sessão e dados de referência; Caffeine em processo | |
| Observabilidade | **OpenTelemetry** (agente Java), Prometheus, Grafana, Loki, Tempo | |

**Alternativa considerada e descartada:** Quarkus com compilação nativa entrega inicialização em milissegundos e consumo de memória bem menor, o que reduz custo de infraestrutura. Foi descartado como padrão porque o ganho de custo é pequeno nesta escala e o ecossistema de contratação e manutenção é significativamente menor no Brasil — em sistema público que sobrevive a trocas de fornecedor, isso pesa mais que RAM.

**Risco que precisa entrar no contrato, não só no backlog:** o Spring Boot **não tem versão LTS**. Cada minor recebe cerca de 12 meses de suporte aberto e há um novo minor a cada seis meses, com apenas duas linhas suportadas simultaneamente. Um sistema público que fica cinco anos sem atualizar acaba rodando versão sem correção de segurança. A consequência prática: **prever contratualmente uma janela de atualização de plataforma a cada 12 meses**, ou orçar suporte estendido de terceiros. Ignorar isso é como o projeto envelhece mal.

### 3.2 Frontend

Duas aplicações distintas, com públicos e requisitos opostos.

**Portal do Recorrente** — cidadão, majoritariamente em celular, muitas vezes em rede ruim, com exigência de acessibilidade.

- **TypeScript estrito + React 19**, empacotado com Vite.
- **Renderização no servidor** (Next.js) se o órgão puder operar um runtime Node em produção; caso contrário, SPA com pré-renderização estática das páginas informativas. O ganho de SSR aqui é primeira pintura em 3G e indexação das páginas de serviço — real, mas não vale impor Node à operação de quem não quer operá-lo.
- **Design System do Governo (GovBR-DS)** como base de componentes e tokens — `@govbr-ds/core` mais o wrapper React. Entrega acessibilidade e familiaridade visual sem reinventar. Se o ente tiver design system próprio (como a Prefeitura de São Paulo tem), ele prevalece.
- Acessibilidade verificada em pipeline com **axe-core**, mirando WCAG 2.1 AA e eMAG.

**Back-office da JARI** — membros, secretaria, coordenação.

- React 19 + TypeScript, SPA pura, sem SSR.
- **Editor de relatório e voto com TipTap** (ProseMirror), com salvamento automático, modelos e formatação restrita. Justificativa direta do edital: o requisito formal de informática do membro é acessar o sistema e redigir dois parágrafos em um editor de texto. O ambiente precisa se parecer com isso, não com um sistema processual.
- **Visualizador de autos com PDF.js**, marca d'água com identificação do usuário e horário, sem botão de download para o perfil de membro.
- **TanStack Query** para estado de servidor, **Zod** para validação compartilhada com o backend via schema.
- **Playwright** para testes ponta a ponta, incluindo o roteiro completo de uma sessão.

> Sobre o "sem download": no cliente, isso é dissuasão e rastreabilidade, nunca DRM. Quem quer copiar, copia — inclusive com a câmera do celular. O controle real é a marca d'água identificadora somada ao registro de cada acesso, que transformam a cópia em algo atribuível. Vender isso ao cliente como bloqueio técnico seria desonesto.

---

## 4. Topologia de hospedagem

### 4.1 O princípio que evita a briga

A escolha de hospedagem, em órgão público, raramente é uma decisão técnica — é uma decisão de contratação. Por isso o desenho é **portável por construção**: contêineres, PostgreSQL, armazenamento S3-compatível e OIDC. Nada proprietário no caminho crítico, com uma exceção justificada (o serviço de chaves, seção 5.3).

Com essa disciplina, os três cenários abaixo executam o mesmo artefato.

### 4.2 Cenários

**Cenário A — Nuvem pública em região brasileira**

Serviços gerenciados de banco, objeto, chaves e WAF; múltiplas zonas de disponibilidade; elasticidade para os picos de fim de prazo.

*A favor:* menor esforço de operação, DR entre zonas quase de graça, KMS/HSM gerenciado, maturidade de backup e observabilidade.
*Contra:* custo recorrente indexado em dólar, processo de contratação mais complexo, e risco de lock-in se a equipe usar serviços proprietários sem abstração.

**Cenário B — Empresa pública de TI / nuvem de governo**

Para o caso de referência, a candidata natural é a companhia de tecnologia do próprio município; em outras esferas, as congêneres estadual e federal.

*A favor:* contratação direta e mais simples, aderência às políticas de segurança do ente, proximidade de rede com os sistemas legados de multas — que é onde está a integração mais pesada.
*Contra:* catálogo de serviços gerenciados geralmente limitado, tempo de provisionamento maior, e dependência da cadência de terceiro para mudanças de infraestrutura.

**Cenário C — On-premises no datacenter do órgão**

*A favor:* controle total, aderência máxima a política de dados, capital já investido.
*Contra:* exige equipe de operação 24×7, HSM físico, plano de DR com segundo site, e é onde mais frequentemente o backup nunca foi testado.

### 4.3 Critérios de decisão

| Critério | Peso | Observação |
|---|---|---|
| Proximidade de rede com o sistema de multas legado | Alto | A integração é síncrona em pontos críticos; latência e VPN atravessada custam caro |
| Disponibilidade de HSM ou KMS gerenciado | Alto | Requisito direto do selo criptográfico da distribuição (seção 5.3) |
| Capacidade de operação da equipe do órgão | Alto | Determina se Kubernetes é viável ou contraproducente |
| Modelo de contratação disponível | Alto | Quase sempre decide sozinho |
| Custo de saída e portabilidade | Médio | Cláusula contratual de reversibilidade e exportação integral |
| Elasticidade | Baixo | A carga é previsível e sazonal por calendário, não por surpresa |

**Recomendação:** Cenário B quando a empresa pública de TI do ente oferecer PostgreSQL gerenciado, armazenamento S3-compatível e serviço de chaves; Cenário A quando não oferecer. O Cenário C só se houver equipe de operação dedicada já existente — não se deve criar uma para este projeto.

### 4.4 Topologia lógica (idêntica nos três cenários)

```
                        Internet
                            │
                  ┌─────────▼─────────┐
                  │  WAF + CDN + DDoS │  cache de estáticos e renditions
                  └─────────┬─────────┘
                            │
                  ┌─────────▼─────────┐
                  │  Balanceador TLS  │  TLS 1.3, mTLS interno
                  └────┬─────────┬────┘
                       │         │
         ┌─────────────▼──┐   ┌──▼──────────────┐
         │ Portal Cidadão │   │  Back-office    │   (estáticos + SSR opcional)
         └─────────────┬──┘   └──┬──────────────┘
                       │         │
                  ┌────▼─────────▼────┐
                  │   API / BFF       │  Spring Boot, virtual threads
                  │   Keycloak (OIDC) │  federa gov.br e AD
                  └────┬─────────┬────┘
                       │         │
   ┌───────────────────▼──┐   ┌──▼──────────────────────────┐
   │ Núcleo SIREJ         │   │ Serviços de apoio           │
   │ (monólito modular)   │   │ • ClamAV                    │
   │ • Peticionamento     │   │ • Renditions (libvips)      │
   │ • Processo           │   │ • PDF/A + assinatura (DSS)  │
   │ • Prazos             │   │ • Integrações (outbox)      │
   │ • Distribuição ⚠     │   └─────────────────────────────┘
   │ • Julgamento         │
   │ • Notificação        │        ⚠ módulo de segurança reforçada
   │ • Publicidade        │
   └───┬──────────┬───────┘
       │          │
┌──────▼───┐ ┌────▼──────────┐ ┌──────────────┐ ┌─────────────────┐
│PostgreSQL│ │Object Storage │ │ Redis        │ │ HSM / KMS       │
│ primário │ │ S3 + Object   │ │ sessão/cache │ │ chaves do selo  │
│ + réplica│ │ Lock (WORM)   │ │              │ │ da distribuição │
└──────────┘ └───────────────┘ └──────────────┘ └─────────────────┘
       │
┌──────▼────────────────────────────────────────────────────────┐
│ Trilha de auditoria encadeada (append-only) → exportação WORM  │
└────────────────────────────────────────────────────────────────┘
```

**Sobre Kubernetes:** só se o órgão já opera Kubernetes com equipe própria. São de 5 a 8 componentes, não 200 microsserviços. Em caso contrário, contêineres em duas ou três VMs com orquestração simples entregam os mesmos SLOs com uma fração do custo cognitivo. Kubernetes introduzido por este projeto costuma virar o principal risco operacional dele.

---

## 5. Segurança

### 5.1 Modelo de ameaças, na ordem certa

| # | Ameaça | Agente | Por que importa aqui |
|---|---|---|---|
| 1 | **Direcionamento da distribuição** | Secretaria, administrador, DBA, fornecedor | É o ataque que o regimento inteiro foi escrito para impedir |
| 2 | Vazamento antecipado da designação | Qualquer perfil interno | Configura sanção funcional expressa; viabiliza o ataque 1 |
| 3 | Adulteração de decisão, voto ou ata | Interno com acesso a banco | Destrói a validade jurídica do ato |
| 4 | Intermediação fraudulenta | Escritórios de recursos, despachantes | O edital lista impedimentos exatamente para esse perfil |
| 5 | Vazamento de dados pessoais | Externo e interno | LGPD; os autos têm CPF, endereço, imagens de veículo |
| 6 | Indisponibilidade em fim de prazo | Externo | Perda de prazo por falha do Estado gera nulidade e litígio |
| 7 | Ataques web convencionais | Externo | OWASP Top 10, cadeia de suprimentos |

A inversão em relação a um sistema corporativo comum é deliberada: aqui o insider vem antes do atacante externo.

### 5.2 Controles por camada

**Identidade e acesso**
- Cidadão via gov.br, com nível de confiabilidade exigido por tipo de ato (consulta em nível básico; protocolo e assinatura em nível superior, parametrizável).
- Internos via Keycloak com MFA obrigatório, papéis por escopo (junta, órgão, unidade) e sessão curta.
- Segregação de funções: quem parametriza não julga; quem opera infraestrutura não lê conteúdo de processo.
- Acesso a banco de produção somente por **break-glass** com aprovação de duas pessoas, tempo limitado e gravação de sessão.

**Dados**
- Cifra em trânsito (TLS 1.3, mTLS entre serviços) e em repouso (disco e objeto).
- Cifra em nível de aplicação para os campos da designação (seção 5.3) e para dados pessoais sensíveis.
- Mascaramento e anonimização obrigatórios em todos os ambientes que não sejam produção.

**Documentos**
- Antivírus em toda entrada, normalização para PDF/A, hash SHA-256 registrado no protocolo.
- Object Lock em modo de conformidade com retenção alinhada à prescrição.
- Marca d'água identificadora na visualização e registro de cada acesso a autos.

**Aplicação**
- OWASP ASVS nível 2 como linha de base contratada.
- SAST, SCA e varredura de imagem em pipeline (SonarQube, Dependency-Check ou equivalente, Trivy), com bloqueio de build em severidade alta.
- SBOM por release e assinatura de imagens de contêiner.
- Pentest independente antes do go-live e anualmente depois.

**Borda**
- WAF com regras OWASP, limitação de taxa por CPF/CNPJ e por IP, proteção de bot no formulário de protocolo.
- Capacidade reservada para picos de fim de prazo — indisponibilidade nesse momento é a mais cara que existe neste domínio.

### 5.3 O controle central: selo criptográfico da distribuição

Este é o mecanismo que traduz o art. 29, XII do regimento em engenharia. Sem ele, a promessa de sigilo depende de as pessoas com acesso ao banco se comportarem bem — o que não é um controle.

**Esquema de compromisso e revelação (*commit–reveal*)**

1. **Distribuição (job semanal)**
   O motor calcula o mapeamento processo → junta → posição de membro, respeitando conexão por veículo ou requerente, equidade por membro e o histórico combinatório de turmas.
2. **Selo**
   O mapeamento é serializado, salgado e cifrado com uma chave de dados gerada para aquela sessão. A chave de dados é envelopada por uma chave mestra que **vive no HSM ou KMS e nunca sai dele**. O texto cifrado vai para o banco; a chave em claro não existe em lugar nenhum.
3. **Compromisso público**
   O hash do mapeamento com o sal é gravado na trilha de auditoria encadeada e carimbado no tempo por ACT credenciada. Opcionalmente, publicado no diário oficial junto com a pauta.
4. **Revelação**
   Somente a transação de **abertura de sessão** — autenticada pelo presidente da junta, com registro de presenças e verificação de quórum — autoriza o KMS a desenvolvar a chave. Só então o mapeamento se torna legível.
5. **Verificação**
   Qualquer pessoa, inclusive o Ministério Público ou o controle interno, pode recalcular o hash do mapeamento revelado e compará-lo ao compromisso carimbado antes da sessão.

**O que isso garante, e o que não garante.** Garante que ninguém — nem o DBA, nem o administrador do sistema, nem o fornecedor — consegue **ler** a designação antes da hora ou **alterá-la** depois sem que a divergência entre compromisso e revelação apareça. Não garante que o próprio motor de distribuição seja justo: isso depende de código auditável, semente de aleatoriedade de qualidade criptográfica, e revisão independente do algoritmo. Os dois controles são complementares e ambos são necessários.

**Consequência de infraestrutura:** o projeto **exige** um HSM ou serviço de chaves gerenciado com controle de política por operação. Esse é o requisito técnico que mais restringe a escolha de hospedagem.

### 5.4 Trilha de auditoria

- Append-only, com encadeamento por hash — cada registro inclui o hash do anterior.
- Ancoragem diária: o hash do dia é assinado com carimbo do tempo e exportado para armazenamento WORM externo ao ambiente de produção.
- Registro obrigatório de: toda consulta à designação, todo acesso a autos, toda alteração de parâmetro, todo break-glass, toda exportação de dados pessoais.
- Retenção alinhada ao prazo prescricional, com verificação periódica de integridade da cadeia.

---

## 6. Performance

### 6.1 Metas (SLO)

| Operação | Meta p95 | Meta p99 |
|---|---|---|
| Consulta de andamento pelo cidadão | 500 ms | 1,2 s |
| Abertura de autos com até 50 documentos | 2 s | 4 s |
| Primeira imagem de AIT visível | 1,5 s | 3 s |
| Registro de voto | 400 ms | 1 s |
| Geração de pauta com 500 processos | 10 s | 20 s |
| Job semanal de distribuição completo | 5 min | 15 min |
| Disponibilidade em janela de sessão | 99,9% | — |
| Disponibilidade geral em horário útil | 99,5% | — |

A disponibilidade tem duas metas de propósito: a janela de sessão é mais exigente que o resto, porque 162 pessoas estão paradas esperando e a reunião não pode ser remarcada sem custo regimental.

### 6.2 Estratégias

**Virtual threads no caminho de I/O.** A abertura de autos dispara dezenas de buscas paralelas a objetos e a serviços externos. Virtual threads entregam esse fan-out sem programação reativa, mantendo o código legível — o que importa em sistema que será mantido por equipes sucessivas. Cuidado clássico: revisar bibliotecas que usam `synchronized` em bloco de I/O e pools de conexão dimensionados para threads de plataforma.

**Renditions pré-geradas.** As imagens de AIT são o item mais pesado. Na ingestão, gerar miniatura e versão web; servir sempre a derivada, com o original acessível sob demanda. Cache de borda com URL assinada de validade curta.

**Réplica de leitura para tudo que não decide.** Painéis, transparência ativa, relatórios mensais e anuais consultam réplica. O primário atende exclusivamente o rito processual.

**Particionamento por período** nas tabelas de eventos e auditoria, com arquivamento frio a partir de determinada idade.

**Job de distribuição como lote isolado.** Executa fora da janela de sessão, com trava, checkpoint e idempotência. Se falhar, não distribui pela metade: cancela e a norma já diz o que fazer — os recursos entram na semana seguinte.

**Teste de carga com o cenário certo.** O roteiro de carga que importa não é "mil usuários navegando", e sim: 27 juntas abrindo sessão no mesmo minuto, 162 membros percorrendo 38 processos cada, mais uma rajada de protocolos em véspera de prazo. Modelar exatamente isso.

---

## 7. Ambientes, desenvolvimento e entrega

### 7.1 Ambientes

| Ambiente | Propósito | Dados |
|---|---|---|
| Local / devcontainer | Desenvolvimento | Sintéticos, gerados por fixture |
| CI | Build e testes automatizados | Sintéticos, efêmeros via Testcontainers |
| Homologação | Espelho de produção, validação do órgão | Produção **mascarada e anonimizada** |
| Sandbox regimental | Simulação de sessões e treinamento de membros | Sintéticos, com calendário acelerado |
| Produção | — | Reais |

O sandbox regimental não é luxo: o regimento prevê capacitação e reciclagem periódica dos membros (art. 28, XV), e o perfil digital do usuário interno exige treino em ambiente que não gere ato jurídico.

### 7.2 Padrões de desenvolvimento

- Monorepo com `backend/` (Maven multi-módulo, um módulo por contexto do Spring Modulith) e `frontend/` (workspaces com os dois apps e um pacote de tipos compartilhado).
- **ArchUnit** para impedir, em tempo de build, que código fora do módulo de distribuição acesse a designação — a regra de segurança vira teste.
- **Testcontainers** para testes de integração contra PostgreSQL, MinIO e Keycloak reais.
- Cobertura mínima negociada, mas com exigência absoluta de suíte específica para as regras RN19 a RN30 do enunciado.
- Conventional Commits, revisão obrigatória por par, branch protegida.

### 7.3 Pipeline

```
commit → build + testes unitários → SAST + SCA → testes de integração
      → build de imagem + SBOM + assinatura → varredura de imagem
      → deploy automático em homologação → E2E (Playwright) + axe-core
      → aprovação humana → deploy em produção (rolling) → smoke test
```

- Migrações de banco sempre retrocompatíveis, em duas fases para mudanças destrutivas.
- Rollback ensaiado, não presumido.
- Nenhum segredo no repositório; injeção via Vault ou KMS.

### 7.4 Continuidade

- PITR contínuo do PostgreSQL com retenção alinhada ao RPO de 15 minutos.
- Backup do armazenamento de objetos com versionamento e cópia fora do ambiente de produção.
- **Restauração testada trimestralmente, com registro** — backup não testado é backup que não existe.
- Plano de DR com RTO de 4 horas, incluindo procedimento específico para a chave mestra no HSM, que é o ponto único de falha mais delicado da arquitetura.

---

## 8. Dimensionamento inicial sugerido

Ponto de partida para produção, a ajustar após o teste de carga.

| Componente | Configuração | Observação |
|---|---|---|
| API / núcleo | 3 instâncias, 4 vCPU / 8 GB | Escala horizontal; heap ~4 GB, G1 ou Generational ZGC |
| Serviços de apoio | 2 instâncias, 2 vCPU / 4 GB | ClamAV, renditions, PDF |
| Keycloak | 2 instâncias, 2 vCPU / 4 GB | Alta disponibilidade |
| PostgreSQL primário | 8 vCPU / 32 GB / SSD NVMe | |
| Réplica de leitura | 4 vCPU / 16 GB | Painéis e transparência |
| Redis | 2 GB, com réplica | |
| Object storage | 5 TB iniciais, crescimento ~1 TB/ano | Sem contar acervo legado |
| HSM / KMS | Serviço gerenciado ou appliance | Requisito da seção 5.3 |

**Principais vetores de custo, em ordem:** armazenamento de objetos e sua banda de saída; acervo legado; ambiente de homologação espelhado; HSM. Computação é o menor deles — o que reforça que elasticidade não deve guiar a escolha de hospedagem.

---

## 9. Rastreabilidade: requisito → controle técnico

| Requisito (enunciado v1.0) | Controle nesta arquitetura |
|---|---|
| RN19 — distribuição eletrônica semanal e equitativa | Spring Batch com trava distribuída, idempotente, com checkpoint |
| RN20 — sigilo da designação até a reunião | Selo criptográfico com commit–reveal e chave em HSM (5.3) |
| RN21 — equilíbrio de votos na turma | Invariante validada no domínio e coberta por teste; sem voto de qualidade no modelo |
| RN22 — quórum e presença do presidente | Transação de abertura de sessão como pré-condição da revelação |
| RN23 — assinatura nominal de relatório e votos | PAdES por membro, com carimbo do tempo |
| RN24 — falha fechada na indisponibilidade | Job que aborta sem estado parcial; ausência deliberada de rota manual |
| RN25 — rodízio combinatório das turmas | Histórico de composições por junta, com verificação de exaustão antes de repetir |
| RN26 — rol fechado de resultados | Enumeração no domínio, com restrição também no banco |
| RN27 — ordem cronológica e vedação de redistribuição | Ordenação no lote; redistribuição só por rota auditada com motivo obrigatório |
| RN29 — autos não saem das instalações | Visualização com marca d'água, sem download para o perfil de membro, acesso registrado |
| RN30 — métricas de perda de mandato | Projeções de eventos por membro; o sistema evidencia, nunca sanciona |
| RNF — acessibilidade | GovBR-DS, axe-core em pipeline, meta WCAG 2.1 AA |
| RNF — validade jurídica | PDF/A, hash no protocolo, PAdES, carimbo do tempo, auditoria encadeada |

---

## 10. ADRs a formalizar na abertura do projeto

1. Monólito modular com Spring Modulith, em vez de microsserviços.
2. Java 25 LTS e Spring Boot 4.1, com janela de atualização anual contratada.
3. PostgreSQL como banco único, com outbox transacional no lugar de broker dedicado.
4. Armazenamento S3-compatível com Object Lock para imutabilidade documental.
5. Keycloak como broker de identidade, federando gov.br.
6. Selo criptográfico da distribuição por commit–reveal com chave em HSM.
7. Portabilidade de hospedagem como restrição de projeto; nenhum serviço proprietário no caminho crítico, exceto o serviço de chaves.
8. Kubernetes condicionado à existência prévia de operação com equipe própria.
9. Renderização no servidor do portal do cidadão condicionada à aceitação de runtime Node em produção.
10. Ausência de DRM: controle de cópia por marca d'água e auditoria, declarado abertamente ao cliente.

---

## 11. Decisões que dependem do cliente

1. **Onde hospedar** — e, antes disso, se existe HSM ou serviço de chaves disponível no cenário pretendido. Sem isso, a seção 5.3 precisa de plano alternativo, que será necessariamente mais fraco.
2. **O órgão opera Kubernetes hoje, com equipe própria?** Define a topologia de execução.
3. **Runtime Node em produção é aceitável?** Define a arquitetura do portal do cidadão.
4. **Qual o tamanho real do acervo legado** a migrar, em processos e em terabytes? É o maior risco de custo em aberto.
5. **Existe design system institucional do ente** a ser seguido em lugar do GovBR-DS?
6. **O sistema de multas legado expõe API**, ou a integração será por arquivo e banco? Define o esforço da camada anticorrupção e influencia a escolha de hospedagem pela proximidade de rede.
7. **Quem assume a operação após o go-live** — o órgão, a empresa pública de TI ou o fornecedor? Isso muda o nível aceitável de complexidade operacional em toda a arquitetura.
