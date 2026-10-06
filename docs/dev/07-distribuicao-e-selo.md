# 07 — Distribuição, sessão e selo

Este é o componente de maior risco do produto. O regimento de referência existe, em boa parte, para impedir que alguém direcione um recurso a um julgador. **O adversário principal está dentro do sistema**: membro, secretaria, administrador, DBA, fornecedor.

Toda mudança neste documento ou no módulo `distribuicao` exige revisão do comitê de segurança (ADR-0006).

## 1. Visão geral em duas etapas

| Etapa | Quando | Quem dispara | O que decide | Visível para |
|---|---|---|---|---|
| 1. Distribuição semanal | Uma vez por semana, fora da janela de sessões | Job agendado (nunca uma pessoa) | Processo → junta + posição + sequência | Ninguém, até a abertura (modo sigiloso) |
| 2. Distribuição interna | Na abertura de cada sessão | Presidente da junta, pelo sistema | Posição → membro presente; formação das turmas; papéis de revisor e 3º membro | Membros da sessão, após a abertura |

No modo `ABERTO`, a etapa 1 é publicada junto com a pauta e o selo serve apenas como prova de integridade.

## 2. Distribuição semanal

### Entrada
Processos em `AGUARDANDO_DISTRIBUICAO` cuja admissão ocorreu na janela da semana (`semana_ref` ISO, fuso da instalação), mais os processos de semanas suspensas.

### Algoritmo

1. **Grupos de conexão.** Una processos que compartilham requerente (CPF/CNPJ) ou veículo (placa/RENAVAM) por fecho transitivo (union-find). Cada grupo é indivisível.
2. **Prevenção.** Se algum processo do grupo for conexo de processo **ainda não julgado** já distribuído, o grupo inteiro vai para a mesma posição (`distribuicao.conexaoComPendentes = MESMA_POSICAO`), se a posição tiver mandato ativo. Caso contrário, segue para sorteio.
3. **Posições elegíveis.** Todas as posições de juntas ativas com mandato vigente na data prevista da sessão.
4. **Ordenação.** Grupos ordenados por menor `data_interposicao`; desempate pelo sorteio.
5. **Atribuição equitativa.** Para cada grupo, em ordem: escolha, entre as posições com menor carga acumulada na semana, uma posição por sorteio com a semente do lote; atribua o grupo inteiro. Grupos grandes podem desequilibrar; o critério de equidade é minimizar `max(carga) - min(carga)` na semana.
6. **Bloqueios.** Uma posição é inelegível para um processo se o titular estiver impedido de forma objetiva conhecida no momento (RN06: membro que lavrou o AIT).
7. **Sequência.** Dentro de cada posição, a sequência segue a data de interposição (RN27).
8. **Selo do processo.** Gerado como `{ano}-S{semana} / {n}ª Junta / {letra} / seq. {n}`.

### Aleatoriedade
- Semente de 256 bits de `SecureRandom` forte (`DRBG`), gerada por lote.
- Gerador derivado da semente determinístico e documentado (ex.: HMAC-DRBG com SHA-256), para que **qualquer auditor reproduza o sorteio** a partir da semente revelada e das entradas.
- A semente nunca é registrada em log, nunca vai para o frontend, nunca fica em claro no banco.

### Falha fechada (RN24, invariante 2)
- O job roda com trava distribuída (ShedLock) e é idempotente por `semana_ref`.
- O lote inteiro (atribuições + selo + compromisso + movimentações) é uma transação. Qualquer exceção, indisponibilidade do KMS ou do banco, ou violação de verificação interna → rollback total, `lote_distribuicao.situacao = SUSPENSO` com motivo, evento `DistribuicaoSuspensa`, alerta ao coordenador.
- Processos do lote suspenso entram no próximo lote, mantendo a data de interposição para a ordem.
- **Não existe** endpoint, comando, script de suporte ou tela para distribuir manualmente, reprocessar parcialmente ou escolher posição. Um teste ArchUnit e uma busca no código garantem isso (doc 12).

## 3. Selo criptográfico (commit–reveal)

Aplica-se aos dados da etapa 1 no modo sigiloso.

1. **Serialização canônica** do mapeamento `[(processo_id, junta, posicao, sequencia)]`, ordenado por `processo_id`, em JSON canônico (RFC 8785).
2. **Sal** de 256 bits aleatório.
3. **Compromisso** = `SHA-256(sal ‖ mapeamento_canonico ‖ semente)`.
4. **Cifra**: o mapeamento e a semente são cifrados com uma chave de dados AES-256-GCM gerada para o lote. A chave de dados é envelopada pela chave mestra no **HSM/KMS**, que nunca sai dele. Grava-se só o envelope e o texto cifrado (`selo`, `designacao_cifrada`).
5. **Registro do compromisso** na trilha de auditoria encadeada e carimbo do tempo por ACT credenciada. Opcionalmente publicado no diário oficial com a pauta.
6. **Revelação**: somente a transação de **abertura de sessão** (seção 4), autenticada pelo presidente com MFA, chama o KMS para desenvelopar a chave **das designações daquela junta naquela sessão**. A política do KMS só aceita a operação vinda da identidade de serviço do módulo `distribuicao`, com contexto de criptografia contendo `sessao_id` e `junta_id`.
7. **Verificação**: qualquer pessoa autorizada recalcula `SHA-256(sal ‖ mapeamento ‖ semente)` após a revelação e compara com o compromisso carimbado. Endpoint público de verificação em `publicidade`.

O que o selo garante: ninguém lê nem altera a designação antes da hora sem que a divergência apareça. O que não garante: que o algoritmo seja justo. Isso exige código auditável, semente criptográfica e revisão independente.

**Plano B (instalação sem HSM/KMS):** KMS em software isolado, com chave mestra dividida (Shamir 2-de-3) entre coordenador, controle interno e TI do órgão. Mais fraco; exige aceite formal do órgão e fica registrado como risco.

## 4. Abertura da sessão e distribuição interna

Transação única, executada pelo presidente (ou vice, se ausente) no horário da sessão:

1. Registrar presenças (`presenca`).
2. Verificar quórum (`sessao.quorumAbertura`). Sem quórum: a sessão não abre; nada é revelado; processos permanecem `DISTRIBUIDO`.
3. Revelar a designação da junta (seção 3.6) → evento `DesignacaoRevelada`.
4. **Escolher a partição de turmas** (rodízio, seção 5).
5. **Mapear posições a pessoas**: cada posição é ocupada pelo titular presente; ausente → suplente do mesmo segmento, se a configuração tiver suplentes; senão a posição fica vaga.
6. **Atribuir papéis por processo**: relator = ocupante da posição do processo; revisor e 3º membro = os outros dois membros da turma do relator, na ordem sorteada para a sessão.
7. Processos de posição vaga (relator ausente): `sessao.relatorAusente = RETORNA_A_PAUTA` → evento `ProcessoRetiradoDePauta`, voltam para a próxima sessão da mesma junta, mesma posição.
8. Membro ausente da turma: substituído no 2º ou 3º voto por presente do mesmo segmento de outra turma da junta (RN35).
9. Gravar `distribuicao_interna` (semente própria da sessão com hash no compromisso) e `historico_particao`; gerar relatório para a Secretaria (art. 16, par. único).

## 5. Rodízio combinatório das turmas (RN25)

- Com 6 posições, 2 por segmento, e turmas de 3 com um por segmento, existem **4 partições** distintas em duas turmas. Generalize: enumere todas as partições válidas dos membros presentes respeitando `umPorSegmento` e restrições (ex.: `presidenteEViceEmTurmasDiferentes`).
- Representação canônica da partição: conjunto de turmas, cada turma como conjunto ordenado de ids de posição. Duas partições iguais com rótulos trocados são a mesma.
- Escolha: sorteie entre as partições **ainda não usadas no ciclo atual** da junta. Quando todas foram usadas, inicia novo ciclo.
- Ausências mudam as partições possíveis. Regra: a partição escolhida é sobre posições; posições vagas são tratadas pela substituição (seção 4.8), e a partição conta como usada.
- Teste de propriedade: para qualquer sequência de N sessões completas, nenhuma partição se repete antes de todas as 4 serem usadas.

## 6. Pauta

- Gerada para cada sessão a partir das designações da junta, ordenada por posição e sequência (RN27).
- Inclui processos de volta de diligência e retirados da pauta anterior (art. 29, IV).
- Modo sigiloso: antes da abertura, nenhuma pauta (pública ou interna) mostra junta, posição ou sequência. O cidadão vê apenas que o processo está distribuído e a semana prevista. A pauta completa só é gerada na abertura.
- Modo aberto: a pauta completa, com relator, é publicada com o edital.
- Edital de pauta com antecedência mínima configurável; PDF/A assinado.

## 7. Votação e apuração

1. Relator registra relatório e voto (resultado do rol, fundamentação, dispositivo normativo obrigatório, checklist RN13) e assina.
2. Com `votacao.ordem = SEQUENCIAL`: revisor vê o voto do relator só depois da assinatura do relator; vota (acompanha ou diverge com justificativa) e assina; depois o 3º membro.
3. Apuração automática: resultado com maioria dos votos assinados. Unanimidade ou maioria. Empate é impossível com número ímpar; com a exceção de 2 votos, os dois precisam concordar, senão o processo volta à pauta.
4. Proclamação pelo presidente → `ResultadoProclamado`, geração do acórdão e ementa.
5. Não existe edição de voto assinado. Erro material → retificação registrada como novo ato.

## 8. Impedimento, suspeição e redistribuição

- Declaração pelo próprio membro, com motivo do rol (`impedimento.motivos`) e texto; recompõe a turma pela regra de substituição; contabiliza por membro (RN34).
- Redistribuição **entre juntas** só por força maior, impedimento ou suspeição de membros da junta, segundo critério pré-estabelecido pelo coordenador (cadastrado antes, versionado), com motivo nos autos (RN27). Executada pelo sistema com nova semente e novo selo; nunca com escolha de destino por pessoa.

## 9. Consulta à designação

- API única: `DesignacaoConsulta.obter(processoId, solicitante)`.
- Modo sigiloso, antes da revelação: retorna `NAO_REVELADA` para todos os perfis, inclusive administrador, e grava `consulta_designacao` com `permitida = false`.
- Após a revelação: retorna os dados conforme o perfil e grava `consulta_designacao` com `permitida = true`.
- Telas, relatórios, exportações, logs e mensagens de erro nunca incluem designação não revelada. Revisão obrigatória de qualquer painel que exponha relator.

## 10. Testes obrigatórios deste módulo

| Teste | Regra |
|---|---|
| Equidade: diferença máxima de carga entre posições ≤ tamanho do maior grupo de conexão | RN19 |
| Conexos sempre na mesma posição (propriedade, jqwik) | RN19 |
| Reprodutibilidade: mesma semente + mesmas entradas = mesmo resultado | Selo |
| Falha injetada em qualquer passo → nenhuma designação gravada, lote `SUSPENSO`, processos no próximo lote | RN24 |
| Não existe rota de distribuição manual (ArchUnit + varredura de endpoints) | RN24 |
| Designação ilegível antes da abertura para todos os perfis, inclusive admin com acesso ao banco de teste | RN20 |
| Verificação do compromisso após revelação confere | Selo |
| Alteração de uma designação cifrada → verificação falha | Selo |
| Rodízio sem repetição até esgotar | RN25 |
| Turma sempre com um membro por segmento | RN21 |
| Abertura sem quórum não revela nada | RN22 |
