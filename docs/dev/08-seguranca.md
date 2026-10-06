# 08 — Segurança e auditoria

## Modelo de ameaças (em ordem de prioridade)

| # | Ameaça | Agente | Controle principal |
|---|---|---|---|
| 1 | Direcionamento da distribuição | Secretaria, admin, DBA, fornecedor | Job automático, semente selada, ausência de rota manual (doc 07) |
| 2 | Vazamento antecipado da designação | Qualquer perfil interno | Commit–reveal com HSM; consulta única auditada |
| 3 | Adulteração de voto, decisão ou ata | Interno com acesso a banco | Tabelas imutáveis, assinatura PAdES, auditoria encadeada |
| 4 | Intermediação fraudulenta | Despachantes, escritórios | Procuração registrada, limitação de taxa, auditoria de acesso |
| 5 | Vazamento de dados pessoais | Externo e interno | LGPD, cifra de campos, mascaramento fora de produção |
| 6 | Indisponibilidade em fim de prazo | Externo | Recebimento assíncrono com recibo imediato; capacidade reservada |
| 7 | Ataques web convencionais | Externo | OWASP ASVS nível 2, WAF, SAST/SCA |

O insider vem antes do atacante externo. Isso é deliberado.

## Identidade e acesso

- Cidadão: OIDC via Keycloak federando gov.br (e outros provedores configurados, ex.: identidade digital do PR). Nível mínimo por ato vem de `identidade.nivelMinimoPorAto`.
- Internos: Keycloak + LDAP/AD do órgão, MFA obrigatório, sessão curta (15 min de inatividade).
- Autorização por **papel + escopo** (órgão, junta, sessão). Checagem sempre no servidor, por método de serviço (`@PreAuthorize` + checagem de escopo no domínio).
- **Segregação de funções**: quem parametriza não julga; quem opera infraestrutura não lê conteúdo de processo; Secretaria não vê designação não revelada. Tabela de incompatibilidades, dupla aprovação e papéis derivados de mandato no doc 18, seção 7 (RN42).
- Acesso ao banco de produção só por break-glass: aprovação de duas pessoas, tempo limitado, sessão gravada, registro na auditoria.

### Papéis

| Papel | Pode |
|---|---|
| `RECORRENTE` | Protocolar, acompanhar, ver autos públicos do próprio processo |
| `PROCURADOR` | O mesmo, para processos cobertos pela procuração |
| `PROTOCOLO` | Receber peça em papel, digitalizar, certificar |
| `ANALISTA_AUTUADOR` | Instrução, informação do agente, defesa da autuação |
| `AUTORIDADE` | Efeito suspensivo, cumprimento, conversão em advertência |
| `SECRETARIA` | Triagem, exigência, pauta, ata, presença; **nunca** designação não revelada |
| `MEMBRO` | Relatar e votar nos processos da sua turma, na sessão aberta |
| `PRESIDENTE` | O de membro + abrir/encerrar sessão, executar distribuição interna, proclamar |
| `COORDENADOR` | Gestão das juntas, critérios de redistribuição, relatórios, aprovação de propostas de alteração (doc 18) |
| `ADMIN` | Propor parametrização, calendário e modelos; conceder e revogar papéis (sensíveis com dupla aprovação); **nunca** conteúdo de processo, dado de recorrente nem designação (RN45) |
| `AUDITOR` | Leitura da trilha de auditoria e verificação de selos |
| `CONSULTA_PUBLICA` | Anônimo: pautas, resultados e ementas anonimizados |

## Trilha de auditoria

- Módulo `auditoria`, tabela `registro_auditoria` append-only, particionada por mês.
- Encadeamento: `hash = SHA-256(hash_anterior ‖ JSON_canonico(registro_sem_hash))`.
- Ancoragem diária: hash final do dia assinado com carimbo do tempo e exportado para armazenamento WORM fora do ambiente de produção.
- **Obrigatório auditar:** toda consulta à designação (permitida ou não); todo acesso a autos e documentos; toda alteração de configuração; todo break-glass; toda exportação de dados pessoais; login e falha de login interno; todo ato processual (já coberto pela movimentação, que também gera registro).
- Gravação de auditoria é parte da mesma transação do ato. Se a auditoria falhar, o ato falha.
- Job de verificação da cadeia roda diariamente e alerta em divergência.

## Dados e LGPD

- Base legal: execução de política pública. Minimização em toda tela e exportação.
- Campos cifrados na aplicação (AES-256-GCM, chave no KMS): CPF/CNPJ (com hash HMAC separado para busca), endereço, telefone.
- Transparência ativa: nome do recorrente nunca aparece; placa mascarada; ementas sem dado pessoal.
- Ambientes fora de produção: só dados sintéticos ou mascarados.

## Documentos

- Antivírus (ClamAV) em toda entrada antes de aceitar o protocolo; arquivo infectado é recusado com mensagem clara.
- Normalização para PDF/A-2b; original preservado.
- Object Lock em modo de conformidade, retenção `retencao.anos`.
- Visualização de membros com marca d'água (nome, CPF parcial, data/hora) e sem download (RN29). Isso é dissuasão e rastreabilidade, não DRM; nunca vender como bloqueio técnico.

## Aplicação e cadeia de suprimentos

- OWASP ASVS nível 2.
- SAST, SCA, varredura de imagem (Trivy) no pipeline, bloqueio em severidade alta.
- SBOM por release; imagens assinadas.
- Nenhum segredo no repositório; injeção via Vault ou KMS.
- Pentest independente antes de cada go-live e anualmente.

## Regras para quem escreve código

1. Nunca registre em log: semente, sal, chave, designação não revelada, CPF completo, token.
2. Mensagens de erro não revelam existência de designação, de processo de terceiros ou de usuário.
3. Todo endpoint novo declara papel e escopo; endpoint sem declaração falha no teste de arquitetura.
4. Nenhuma consulta SQL fora do módulo `distribuicao` toca tabelas de designação (ArchUnit + permissões de banco por schema).
