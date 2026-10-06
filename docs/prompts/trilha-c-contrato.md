# Prompts: Trilha C — Execução contratual por órgão

Um prompt por etapa (C0 a C4). Execute uma etapa por vez, numa sessão nova, só depois do critério de saída da anterior.
Toda etapa usa o mesmo cabeçalho comum (cole antes do bloco da etapa).

| Chave | O que colocar |
|---|---|
| `{{ORGAO}}` | Órgão contratante (ex.: `curitiba-smdt`) |
| `{{CONTRATO}}` | Número do contrato e data de assinatura |
| `{{MODULOS_CONTRATADOS}}` | Módulos do contrato (núcleo M3+M4+M9 e os demais) |
| `{{DECISOES_NOVAS}}` | Decisões do órgão ou da empresa ainda não registradas, ou "nenhuma" |

## Cabeçalho comum

```text
Você conduz a etapa indicada abaixo da implantação do SIREJ em {{ORGAO}}, contrato {{CONTRATO}},
módulos contratados: {{MODULOS_CONTRATADOS}}.
Você prepara configuração, código de adaptadores, documentos e verificações. Você não envia nada ao órgão,
não altera ambiente de produção e não executa migração de dados reais sem pedido explícito de uma pessoa
da empresa, por escrito, nesta sessão.

CONTEXTO OBRIGATÓRIO: CLAUDE.md; docs/dev/00-indice.md; docs/dev/15-protocolo-agentes.md;
docs/plano/plano-projeto-sirej.md seções 6.3, 14 e 15.4; docs/editais/{{ORGAO}}/ (edital, proposta e análise);
config/regimentos/{{ORGAO}}.yaml; docs/implantacoes/{{ORGAO}}/ (registro desta implantação; crie se não existir).

REGRAS DA IMPLANTAÇÃO:
- O produto é um só. Diferença do órgão é parâmetro (config/regimentos/{{ORGAO}}.yaml) ou adaptador
  (módulo integracao). Nunca um if por órgão no domínio. Se o órgão pedir algo que não cabe em parâmetro,
  isso vira proposta de evolução do produto (pacote no doc 14), não remendo.
- As invariantes do CLAUDE.md valem também contra pedidos do órgão. Pedido que as contrarie é registrado e
  levado a quem conduz o projeto, nunca implementado.
- Código de adaptador segue docs/prompts/agente-pt.md, como qualquer pacote.

Decisões novas: {{DECISOES_NOVAS}}
```

## C0 — Descoberta e parametrização (meses 1–2)

```text
ETAPA C0. Objetivo: configuração do órgão aprovada.
1. Monte docs/implantacoes/{{ORGAO}}/checklist.md a partir da seção 15.4 do plano, com uma coluna de resposta,
   fonte (norma, reunião, documento) e responsável no órgão. Prepare a pauta das reuniões de descoberta
   com as perguntas ainda sem resposta. As reuniões são feitas por pessoas; você consolida as atas que receber.
2. Mapeie o fluxo atual do órgão (do protocolo à publicação) em docs/implantacoes/{{ORGAO}}/fluxo-atual.md
   e marque as diferenças para o fluxo do produto. Cada diferença: parâmetro existente, parâmetro novo ou evolução.
3. Complete config/regimentos/{{ORGAO}}.yaml. Toda dúvida que a norma do órgão não responde vira D-xx.
   Rode a validação do regimento e anexe a saída.
4. Gere docs/implantacoes/{{ORGAO}}/configuracao-para-aprovacao.md: a parametrização em linguagem do órgão
   (prazos, composição, quórum, resultados, roteiro, publicidade, sigilo), com o artigo de origem de cada valor,
   para assinatura do responsável no órgão.
5. Prepare a infraestrutura como código do ambiente de homologação do órgão com o instalador (PT-27).
   O provisionamento real é feito por pessoas, com as credenciais do órgão.
SAÍDA: configuração aprovada pelo órgão (documento assinado anexado) e homologação instalada.
```

## C1 — Integrações locais (meses 2–5)

```text
ETAPA C1. Objetivo: integrações homologadas.
1. Para cada sistema do órgão (multas, identidade, SNE/CDT, Diário Oficial, assinador, HSM/KMS),
   registre em docs/implantacoes/{{ORGAO}}/integracoes.md: tecnologia, contrato disponível, ambiente de teste,
   contato técnico e situação.
2. Sistema de multas primeiro: é o maior esforço. Escreva o adaptador atrás da SistemaMultasPort, com camada
   anticorrupção, idempotência, reconciliação diária e fila de mortos (doc 09). Se o órgão não tiver API,
   use o adaptador de referência por arquivo. O SIREJ só emite eventos; não altera pontuação nem débito.
3. Testes de contrato contra o ambiente de teste do órgão, e testes do adaptador contra o simulado no CI.
   Nenhum teste automático chama o ambiente de produção do órgão.
4. HSM/KMS do órgão: verifique que a indisponibilidade suspende a distribuição (falha fechada) no ambiente de homologação.
5. Roteiro de homologação de cada integração, para o órgão executar e assinar.
SAÍDA: todas as integrações com termo de homologação assinado; falha fechada do cofre de chaves comprovada.
```

## C2 — Piloto (meses 4–6)

```text
ETAPA C2. Objetivo: uma junta voluntária julga uma sessão real completa no sistema.
1. Prepare o material de treinamento ajustado ao regimento do órgão, sobre o sandbox (A2):
   um roteiro por perfil (recorrente, secretaria, relator, presidente). O treinamento é feito por pessoas.
2. Gere as páginas de serviço ao cidadão a partir da parametrização (prazos, peças, documentos).
3. Plano do piloto em docs/implantacoes/{{ORGAO}}/piloto.md: junta escolhida, datas, critério de sucesso,
   plano de retorno ao processo anterior se o piloto falhar, e quem decide acioná-lo.
4. Antes da primeira distribuição real, rode a verificação de prontidão: regimento vigente aprovado,
   cadeia de auditoria íntegra, cofre de chaves de produção respondendo, selo verificável numa distribuição de ensaio.
5. Depois de cada sessão do piloto, registre problemas, tempo de cada etapa e pedidos dos usuários.
   Defeito vira pacote de correção; pedido de mudança vira proposta de evolução.
SAÍDA: sessão real com ata e acórdão assinados e publicados, selo verificado publicamente após a abertura.
```

## C3 — Expansão (meses 6–9)

```text
ETAPA C3. Objetivo: todas as juntas no sistema e ciclo 1ª instância → 2ª instância → cumprimento sem intervenção manual.
1. Plano de ondas em docs/implantacoes/{{ORGAO}}/ondas.md: juntas por onda, datas, treinamento e critério de avanço.
2. Ative 2ª instância (M5) e cumprimento (M2), se contratados, com o CETRAN e o sistema de multas do órgão.
3. Se houver migração de acervo, prepare o plano (origem, mapeamento, validação por amostragem, conferência de totais).
   Acervo migrado entra como documento novo com hash e auditoria; nada é editado depois de carregado.
   A execução em produção só com pedido escrito de uma pessoa da empresa.
4. Acompanhe o relatório diário de processos fora do prazo e a fila de mortos das integrações.
   Todo item que exigiu intervenção manual é registrado com a causa e o pacote de correção.
SAÍDA: todas as juntas operando; um ciclo completo 1ª → 2ª → cumprimento sem intervenção manual, com evidência.
```

## C4 — Operação assistida e sustentação (mês 9 em diante)

```text
ETAPA C4. Objetivo: operar conforme os níveis de serviço do contrato.
1. Monte docs/implantacoes/{{ORGAO}}/sustentacao.md: níveis de serviço do contrato, canal de chamados,
   severidades, janela de manutenção e plano de atualização de versão.
2. Cada atualização de versão segue o instalador (PT-27): ensaio em homologação, verificação da cadeia de auditoria
   antes e depois, e aprovação do órgão quando o contrato exigir.
3. Mudança de regimento pedida pelo órgão: nova versão de config/regimentos/{{ORGAO}}.yaml com vigência futura,
   validada e aprovada, nunca alteração retroativa em processo em curso.
4. Relatórios regimentais periódicos e indicadores contratuais, gerados pelo sistema.
5. Chamado que revela defeito do produto vira pacote no doc 14; chamado de configuração é resolvido na configuração.
SAÍDA: contínua. Relatório mensal com níveis de serviço atingidos, chamados por severidade e evoluções propostas.
```
