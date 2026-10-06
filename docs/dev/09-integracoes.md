# 09 — Integrações

Princípio: **antifrágil por padrão**. Toda chamada externa é assíncrona quando possível, idempotente, com retry exponencial, fila de mortos e reconciliação periódica. A indisponibilidade de terceiros **nunca** bloqueia o direito de petição do cidadão.

## Portas (interfaces no domínio) e adaptadores

Cada porta é uma interface Java declarada no módulo de negócio que a consome. Implementações ficam em `integracao`, uma por órgão ou tecnologia, selecionadas pela configuração da instalação. Toda porta tem um **adaptador simulado** (`*Simulado`) usado em desenvolvimento, testes e demonstração/PoC.

| Porta | Módulo dono | Direção | Operações | Observação |
|---|---|---|---|---|
| `SistemaMultasPort` | `instrucao` | bidirecional | `consultarAit`, `consultarNotificacoes`, `consultarPenalidades(cpfCnpj/placa)`, `informarEfeitoSuspensivo`, `informarCumprimento`, `informarRestituicao` | **Uma implementação por órgão**; cada estado fornece a API (ou arquivo/banco). Maior esforço de cada implantação |
| `IdentidadeCidadaoPort` | `identidade` | entrada | `autenticar`, `nivelConfianca` | gov.br; identidade digital do PR; outros por configuração |
| `NotificacaoEletronicaPort` | `notificacao` | bidirecional | `enviar`, `consultarCiencia` | SNE/CDT |
| `RenainfPort` | `secretaria` | bidirecional | `registrarRecebimento`, `remeterAoCompetente` | |
| `DiarioOficialPort` | `publicidade` | saída | `publicar(ato)` | Pauta, resultado, nomeações |
| `AssinaturaPort` | `assinatura` | saída | `assinar(documento, signatario)`, `carimbar`, `validar` | gov.br avançada e ICP-Brasil |
| `CofreChavesPort` | `distribuicao` | saída | `gerarChaveDados`, `envelopar`, `desenvelopar(contexto)` | HSM/KMS; síncrono e crítico |
| `ArmazenamentoObjetosPort` | `documentos` | saída | `gravar(sha256)`, `ler`, `urlAssinada` | S3 com Object Lock |
| `AntivirusPort` | `documentos` | saída | `verificar` | ClamAV |
| `CetranPort` | `segundainstancia` | bidirecional | `remeterAutos`, `receberDecisao` | Pode ser pacote de arquivos se o CETRAN não tiver sistema |
| `MensageriaCortesiaPort` | `notificacao` | saída | `email`, `push`, `sms` | Não oficial |
| `CadinPort` | `credenciamento` | saída | `consultar(cnpj/cpf)` | |

## Regras

1. O domínio só conhece a porta. Nenhum tipo de biblioteca externa vaza para o domínio.
2. **Camada anticorrupção** em todo adaptador de sistema de multas: traduz o modelo do órgão para `Ait`, `NotificacaoAutuacao`, `NotificacaoPenalidade`, `Penalidade` do SIREJ.
3. **Saída** por outbox (`evento_saida`): gravado na mesma transação do ato; publicador assíncrono com retry exponencial (1 s, 2 s, 4 s... até 1 h), máximo configurável, depois fila de mortos com alerta.
4. **Idempotência**: toda mensagem tem `chave_idempotencia` determinística (ex.: `cumprimento:{processo_id}:{decisao_id}`). Entradas repetidas são ignoradas.
5. **Reconciliação** diária com o sistema de multas: compara situação das penalidades afetadas por decisões e registra divergências.
6. **Degradação controlada**: se o sistema de multas cair, o protocolo continua (com dados informados pelo cidadão e validação posterior), e a sincronização entra na fila.
7. `CofreChavesPort` é exceção: é síncrona e, se indisponível, a operação que depende dela falha fechada (distribuição suspensa, sessão não abre).
8. Timeouts explícitos em toda chamada; circuit breaker por porta.

## Contrato padrão do sistema de multas

Para acelerar implantações, o produto publica um **contrato de referência** (OpenAPI em `docs/dev/contratos/sistema-multas.yaml`, a criar no PT-10). Órgãos com API própria recebem um adaptador; órgãos sem API recebem adaptador por arquivo (CSV/JSON em SFTP) ou leitura de visão de banco, sempre atrás da mesma porta.
