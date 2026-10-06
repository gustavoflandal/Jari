# 05 — Modelo de dados

PostgreSQL 17+, migrações Flyway. Um **schema por módulo** (`processo`, `distribuicao`, `auditoria`...). Nenhuma consulta cruza schemas de outro módulo (doc 03).

## Convenções

- Tabelas e colunas em `snake_case`, português sem acento, singular (`processo`, `pauta_item`).
- Chave primária `id uuid` (UUIDv7, gerado na aplicação). Números de negócio (número do processo, protocolo) são colunas separadas, únicas.
- Datas: `timestamptz` para instantes; `date` para datas de prazo. Fuso da instalação em `configuracao.fusoHorario`.
- Tabelas imutáveis (marcadas **[imutável]**): sem `UPDATE`/`DELETE` pela aplicação. Um trigger `BEFORE UPDATE OR DELETE` lança erro; o usuário de banco da aplicação não tem esses privilégios nelas.
- Enumerações de domínio fixas (situação, tipo de peça) como `text` com `CHECK`. Enumerações configuráveis (resultados, segmentos, motivos) como `text` validado contra a configuração ativa e gravado junto com `config_versao`.
- Toda tabela de negócio tem `criado_em`, `criado_por`. Tabelas mutáveis têm `versao` (lock otimista).
- Dados pessoais sensíveis (CPF, endereço) em colunas cifradas na aplicação quando indicado (doc 08).

## Entidades por módulo

### configuracao
| Tabela | Campos principais | Notas |
|---|---|---|
| `regimento_versao` **[imutável]** | id, conteudo jsonb, hash, vigente_desde, aplicado_por, proposta_id | Cada mudança de configuração é uma nova versão; atos gravam a versão vigente |
| `proposta_alteracao` | id, tipo (REGIMENTO/CALENDARIO/MODELO/IMPORTACAO), secao, conteudo jsonb, diff jsonb, impacto jsonb, justificativa, ato_normativo jsonb, documento_id, vigencia_pretendida, situacao, proposta_por, versao | Mutável só em `RASCUNHO`; transições por comando (doc 18, seção 3) |
| `decisao_proposta` **[imutável]** | id, proposta_id, decisao (SUBMETIDA/APROVADA/REJEITADA/CANCELADA/PUBLICADA), motivo, ator_id, em | Uma linha por transição |
| `calendario_registro` **[imutável]** | id, tipo (FERIADO/PONTO_FACULTATIVO/SUSPENSAO_EXPEDIENTE), data_inicio, data_fim, abrangencia, descricao, proposta_id | Substitui a antiga tabela `feriado` |
| `calendario_revogacao` **[imutável]** | id, registro_id, proposta_id, em | Revogação é novo registro (RN41) |
| `modelo_documento_versao` **[imutável]** | id, tipo, conteudo, campos text[], hash, vigente_desde, proposta_id | RN43 |
| `importacao_configuracao` **[imutável]** | id, origem (INSTALADOR/CONSOLE), hash_yaml, regimento_versao_id, em | Alerta de pacote divergente compara com a última linha |

### identidade
| Tabela | Campos principais | Notas |
|---|---|---|
| `atribuicao_papel` **[imutável]** | id, usuario_id, papel, escopo_tipo (ORGAO/JUNTA/SESSAO), escopo_id, inicio, fim, origem (CONCEDIDA/DERIVADA_MANDATO), mandato_id, proposta_por, aprovada_por | RN42 |
| `revogacao_papel` **[imutável]** | id, atribuicao_id, motivo, ator_id, em | Revogação, suspensão por conflito e fim de mandato |
| `recertificacao` **[imutável]** | id, atribuicao_id, ciclo, decisao, ator_id, em | Revisão mensal |

### pessoas
| Tabela | Campos principais |
|---|---|
| `pessoa` | id, tipo (PF/PJ), cpf_cnpj (cifrado + hash para busca), nome, email, telefone |
| `veiculo` | id, placa, renavam |
| `procuracao` | id, outorgante_id, procurador_id, escopo (processos ou geral), validade_inicio, validade_fim, revogada_em, documento_id |

### processo
| Tabela | Campos principais | Notas |
|---|---|---|
| `processo` | id, numero (único), tipo_peca, instancia, ait_numero, veiculo_id, recorrente_id, procurador_id, data_interposicao, situacao (projeção), prazo_alvo, versao | `situacao` é projeção, reconstruível |
| `movimentacao` **[imutável]** | id, processo_id, sequencia, tipo_evento, ator_id, ator_papel, ocorrido_em, payload jsonb, documento_ids, config_versao | Event store do processo; `(processo_id, sequencia)` único |
| `ciencia` **[imutável]** | id, processo_id, notificacao_ref, meio, incluido_em, ciencia_em, tipo (EFETIVA/PRESUMIDA), comprovante_documento_id | |

### documentos
| Tabela | Campos principais | Notas |
|---|---|---|
| `documento` **[imutável]** | id, processo_id, sha256, mime_original, objeto_original, objeto_pdfa, tamanho, origem (RECORRENTE/ORGAO/JARI/SISTEMA), publico bool, juntado_em, juntado_por | Objeto no S3 com Object Lock; chave = sha256 |
| `desentranhamento` **[imutável]** | id, documento_id, motivo, autor_id, em | |
| `acesso_documento` **[imutável]** | id, documento_id, usuario_id, perfil, em, ip, marca_dagua_id | RN29 |

### prazos
| Tabela | Campos principais |
|---|---|
| `prazo` | id, processo_id, tipo, inicio, vencimento, suspenso_desde, cumprido_em, situacao |
| `alerta_prazo` | id, prazo_id, dias_antes, emitido_em |

### composicao
| Tabela | Campos principais | Notas |
|---|---|---|
| `junta` | id, numero, nome, dia_semana, turno, ativa | |
| `posicao` | id, junta_id, letra, segmento | SP: 6 posições A–F, 2 por segmento |
| `membro` | id, pessoa_id, segmento | |
| `mandato` | id, membro_id, junta_id, posicao_id, tipo (TITULAR/SUPLENTE), inicio, fim, papel_presidencia, encerrado_motivo | Linha do tempo independente do ciclo de credenciamento |
| `presenca` | id, sessao_id, membro_id, presente, justificativa, cancelada_motivo | RN36 |

### distribuicao (segurança reforçada)
| Tabela | Campos principais | Notas |
|---|---|---|
| `lote_distribuicao` **[imutável]** | id, semana_ref, iniciado_em, concluido_em, situacao (CONCLUIDO/SUSPENSO), motivo_suspensao, total_processos, config_versao | |
| `selo` **[imutável]** | id, lote_id ou sessao_id, compromisso_sha256, sal_cifrado, chave_dados_envelopada, kms_chave_ref, carimbo_tempo, revelado_em | Doc 07 |
| `designacao_cifrada` **[imutável]** | id, lote_id, processo_id, conteudo_cifrado | Ilegível sem a chave do KMS |
| `designacao` | id, processo_id, junta_id, posicao_id, sequencia, semana_ref, revelada_em, sessao_id | Só é preenchida na revelação (modo sigiloso) ou no lote (modo aberto) |
| `grupo_conexao` | id, chave (cpf_cnpj/placa hash), posicao_id | Prevenção de conexos |
| `distribuicao_interna` **[imutável]** | id, sessao_id, particao_id, semente_hash, resultado jsonb | |
| `historico_particao` **[imutável]** | id, junta_id, sessao_id, particao_canonica | Rodízio (RN25) |
| `redistribuicao` **[imutável]** | id, processo_id, de_junta, para_junta, motivo_tipo, motivo_texto, criterio_ref, autor_id | RN27 |
| `consulta_designacao` **[imutável]** | id, usuario_id, processo_id, em, permitida bool | Toda consulta auditada |

### sessao
| Tabela | Campos principais |
|---|---|
| `sessao` | id, junta_id, tipo, data, situacao (PREVISTA/ABERTA/ENCERRADA/CANCELADA), aberta_em, aberta_por, encerrada_em |
| `pauta` | id, sessao_id, publicada_em, edital_documento_id |
| `pauta_item` | id, pauta_id, processo_id, posicao_id, sequencia |
| `turma` | id, sessao_id, rotulo, membro_ids |
| `ata` | id, sessao_id, documento_id, assinada_em |

### julgamento
| Tabela | Campos principais | Notas |
|---|---|---|
| `relatorio` | id, processo_id, relator_id, texto, checklist jsonb, rascunho bool | Rascunho editável; versão final imutável ao assinar |
| `voto` **[imutável]** | id, processo_id, sessao_id, membro_id, papel (RELATOR/REVISOR/TERCEIRO), resultado, fundamentacao, dispositivo_normativo, tese_id, divergente bool, assinatura_id, em | |
| `decisao` **[imutável]** | id, processo_id, sessao_id, resultado, tipo (UNANIMIDADE/MAIORIA), votos_ids, proclamada_em, proclamada_por | `CHECK` contra o rol configurado |
| `acordao` | id, decisao_id, ementa, documento_id | |
| `impedimento_declarado` **[imutável]** | id, processo_id, membro_id, tipo (IMPEDIMENTO/SUSPEICAO), motivo_codigo, texto, em | |
| `diligencia` | id, processo_id, solicitante_id, tipo (INFORMACAO/PRESENCIAL), membros_ids, prazo, resultado_documento_id | |
| `achado_sistemico` | id, processo_id, local, codigo_infracao, tipo_falha | Retorno sistêmico (M7) |

### auditoria
| Tabela | Campos principais | Notas |
|---|---|---|
| `registro_auditoria` **[imutável]** | seq bigint (sequência `registro_auditoria_seq`), em, ator_id, ator_papel, ip inet, acao, alvo_tipo, alvo_id, detalhe jsonb, hash_anterior, hash | Particionada por mês (intervalo de `em`, limites em UTC; PK `(seq, em)`); `hash = sha256(hash_anterior ‖ canonical(registro))`; detalhe é objeto plano de texto (D-43) |
| `ancora_diaria` **[imutável]** | dia, seq_final, hash_final, carimbo_tempo, carimbo_emissor, carimbo_em, exportado_em, destino | `seq_final` = último registro antes do fim do dia (0 se a trilha estava vazia) |

Partições: a migração cria do mês anterior a 12 meses à frente; a função `auditoria.garantir_particao(dia)` (`SECURITY DEFINER`, única permissão de DDL do papel da aplicação) cria as seguintes, chamada na subida e diariamente (`sirej.auditoria.particoes.meses-a-frente`, padrão 3). Sem partição para o instante, a gravação falha e o ato falha junto.

Privilégios: o papel `sirej_aplicacao` (criado pela migração se não existir; o instalador cria o usuário de login como membro dele) tem só `USAGE` no schema e `SELECT`/`INSERT` nas duas tabelas. Triggers `BEFORE UPDATE OR DELETE` (linha) e `BEFORE TRUNCATE` (no pai e em cada partição) recusam a operação para qualquer usuário, inclusive o dono.

### integracao
| Tabela | Campos principais |
|---|---|
| `evento_saida` (outbox) | id, tipo, chave_idempotencia, payload, situacao, tentativas, proxima_tentativa, ultimo_erro |
| `evento_entrada` | id, origem, chave_idempotencia, payload, processado_em |
| `reconciliacao` | id, sistema, executada_em, divergencias |

## Volumes e particionamento

- `movimentacao` e `registro_auditoria`: 100 a 200 milhões de linhas em 5 anos na escala de SP. Particionamento mensal desde a primeira migração.
- `documento`: metadados no banco, binários no S3. ~70 GB/mês de objetos em SP.

## Retenção

Retenção mínima alinhada à prescrição quinquenal; parâmetro `retencao.anos` (padrão 5) aplicado no Object Lock e na política de arquivamento. Nunca há exclusão física antes do prazo.
