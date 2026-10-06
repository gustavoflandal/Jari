# ADR-0011: Configuração em produção por proposta com dupla aprovação

**Situação:** proposta (06/10/2026)

## Contexto
O enunciado pede parametrização sem deploy. Parametrizar é poder mudar prazo, quórum, distribuição e acesso. A ameaça nº 1 do produto é o interno que direciona o julgamento. Uma tela de configuração com efeito imediato e um único autor seria o atalho que o selo da distribuição fecha. Também é preciso decidir quem prevalece quando o YAML do pacote e o banco divergem.

## Decisão
- Toda mudança de regimento, calendário, modelo de documento ou papel sensível é uma proposta. Ela é validada, justificada e aprovada por pessoa diferente do proponente, e tem vigência futura. Não existe fluxo de emergência sem aprovação.
- A vigência nunca retroage. Lote, sessão e prazo já iniciados terminam com a versão em que começaram.
- Depois da instalação, o banco é a fonte de verdade. O YAML do pacote só entra por importação, que é também uma proposta. Um YAML divergente gera alerta e nunca é aplicado sozinho.
- O console administrativo não lê processo, dado de recorrente nem designação.

Especificação no doc 18.

## Consequências
- Mudança urgente leva no mínimo o tempo de uma aprovação. A indisponibilidade em fim de prazo é tratada como suspensão de expediente, que pode ter vigência imediata.
- Atualizar o produto não muda a configuração do órgão sem o órgão aprovar.
- Órgão muito pequeno precisa de ao menos duas pessoas com papéis distintos: um administrador e um coordenador.
