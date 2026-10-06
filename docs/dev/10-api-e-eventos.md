# 10 — APIs e eventos

## REST

- Base: `/api/v1`. Dois BFFs lógicos: `/api/v1/portal/**` (cidadão) e `/api/v1/backoffice/**` (interno). Consulta pública anônima em `/api/v1/publico/**`.
- Recursos em português, plural, `kebab-case`: `/processos/{id}/movimentacoes`, `/sessoes/{id}/abertura`.
- Ações de domínio como sub-recurso com `POST` (não `PATCH` de estado): `POST /processos/{id}/exigencias`, `POST /sessoes/{id}/abertura`, `POST /processos/{id}/votos`.
- Erros em `application/problem+json` (RFC 9457) com `type` estável (`/erros/transicao-invalida`, `/erros/prazo-vencido`, `/erros/quorum-insuficiente`). Mensagens de erro nunca revelam designação ou dados de terceiros.
- Paginação por cursor; datas ISO 8601 com fuso.
- Idempotência: `POST` de protocolo e de voto exigem header `Idempotency-Key`.
- Contrato OpenAPI gerado do código (springdoc) e publicado em `docs/dev/contratos/`; o frontend gera tipos a partir dele.
- Todo endpoint declara papel e escopo (doc 08).

## Endpoints essenciais por módulo (núcleo)

| Módulo | Endpoints |
|---|---|
| peticionamento | `GET /portal/autuacoes?placa=` · `GET /portal/autuacoes/{ait}/pecas-cabiveis` · `POST /portal/rascunhos` · `POST /portal/rascunhos/{id}/documentos` · `POST /portal/rascunhos/{id}/protocolo` |
| processo | `GET /portal/processos` · `GET /portal/processos/{id}` (situação, prazo, linha do tempo) · `GET /backoffice/processos/{id}` |
| documentos | `GET /backoffice/documentos/{id}/visualizacao` (marca d'água, sem download para membro) · `GET /portal/processos/{id}/autos.pdf` (recorrente) |
| secretaria | `POST /backoffice/processos/{id}/triagem` · `POST /backoffice/processos/{id}/exigencias` · `POST /backoffice/processos/{id}/admissao` · `POST /backoffice/processos/{id}/inadmissao` |
| sessao | `POST /backoffice/sessoes` · `POST /backoffice/sessoes/{id}/presencas` · `POST /backoffice/sessoes/{id}/abertura` · `POST /backoffice/sessoes/{id}/encerramento` · `GET /backoffice/sessoes/{id}/pauta` |
| julgamento | `GET /backoffice/sessoes/{id}/meus-processos` · `PUT /backoffice/processos/{id}/relatorio` (rascunho) · `POST /backoffice/processos/{id}/votos` · `POST /backoffice/processos/{id}/impedimentos` · `POST /backoffice/processos/{id}/proclamacao` |
| distribuicao | Nenhum endpoint que dispare ou altere distribuição. `GET /backoffice/lotes` (metadados, sem designações) · `GET /publico/selos/{id}/verificacao` |
| auditoria | `GET /backoffice/auditoria?alvo=` (papel `AUDITOR`) · `POST /backoffice/admin/auditoria/verificacoes` · `POST /backoffice/admin/auditoria/exportacoes` |
| configuracao (doc 18) | `GET /backoffice/admin/regimento` · `GET /backoffice/admin/regimento/versoes` · `GET /backoffice/admin/regimento/versoes/{id}/yaml` · `POST /backoffice/admin/propostas` · `PUT /backoffice/admin/propostas/{id}` (rascunho) · `GET /backoffice/admin/propostas/{id}/impacto` · `POST /backoffice/admin/propostas/{id}/submissao` · `POST /backoffice/admin/propostas/{id}/aprovacao` · `POST /backoffice/admin/propostas/{id}/rejeicao` · `POST /backoffice/admin/propostas/{id}/cancelamento` · `GET /backoffice/admin/calendario?ano=` · `GET /backoffice/admin/modelos` · `POST /backoffice/admin/modelos/{tipo}/pre-visualizacao` |
| identidade (doc 18) | `GET /backoffice/admin/usuarios?busca=` · `GET /backoffice/admin/usuarios/{id}/atribuicoes` · `POST /backoffice/admin/atribuicoes` · `POST /backoffice/admin/atribuicoes/{id}/aprovacao` · `POST /backoffice/admin/atribuicoes/{id}/revogacao` · `GET /backoffice/admin/recertificacoes` |
| integracao | `GET /backoffice/admin/integracoes` · `POST /backoffice/admin/integracoes/fila-de-mortos/{id}/reenfileiramento` |

## Eventos de domínio (dentro do monólito)

Nome no passado, em português, `record` imutável com `ocorridoEm`, `ator`, `configVersao`.

| Evento | Publicado por | Consumidores típicos |
|---|---|---|
| `PecaProtocolada` | peticionamento | processo, prazos, notificacao, auditoria |
| `TriagemIniciada`, `ExigenciaEmitida`, `ExigenciaAtendida`, `ExigenciaVencida` | secretaria | processo, prazos, notificacao |
| `RecursoAdmitido`, `RecursoInadmitido` | secretaria | processo, notificacao |
| `InstrucaoConcluida` | instrucao | processo |
| `ProcessoDistribuido` (sem designação no payload no modo sigiloso) | distribuicao | processo, prazos |
| `DistribuicaoSuspensa` | distribuicao | notificacao (coordenador), auditoria |
| `ProcessoPautado` | sessao | processo, notificacao |
| `SessaoAberta`, `DesignacaoRevelada`, `TurmasFormadas` | sessao / distribuicao | julgamento, processo |
| `ProcessoRetiradoDePauta` | sessao | processo, distribuicao |
| `VotoRegistrado` | julgamento | auditoria |
| `ResultadoProclamado` | julgamento | processo, publicidade, notificacao |
| `DecisaoPublicada` | publicidade | processo, prazos, notificacao, instrucao (cumprimento) |
| `DiligenciaSolicitada`, `DiligenciaConcluida` | julgamento | processo, prazos |
| `ImpedimentoDeclarado` | julgamento | distribuicao, composicao (métrica) |
| `PrazoAlertado`, `PrazoVencido` | prazos | notificacao, secretaria |
| `CumprimentoSolicitado`, `CumprimentoConfirmado`, `RestituicaoSolicitada` | instrucao | integracao, processo |
| `PropostaSubmetida`, `PropostaAprovada`, `PropostaRejeitada` | configuracao | notificacao (aprovadores e proponente), auditoria |
| `RegimentoVersaoPublicada`, `ModeloDocumentoPublicado` | configuracao | todos os módulos com cache de configuração |
| `CalendarioAlterado` | configuracao | prazos (recálculo que só prorroga, RN41) |
| `PapelAtribuido`, `PapelRevogado` | identidade | auditoria, notificacao |
| `MandatoIniciado`, `MandatoEncerrado` | composicao | identidade (derivação de `MEMBRO`/`PRESIDENTE`) |
| `FaseArquivisticaAlterada` | processo | documentos, publicidade |

## Eventos de integração (outbox)

Prefixo `integracao.` e versão: `integracao.cumprimento.v1`, `integracao.efeito-suspensivo.v1`, `integracao.notificacao-sne.v1`, `integracao.publicacao-doe.v1`, `integracao.remessa-cetran.v1`. Payload em JSON com esquema versionado em `docs/dev/contratos/eventos/`.
