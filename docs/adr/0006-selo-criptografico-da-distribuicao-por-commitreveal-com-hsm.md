# ADR-0006: Selo criptográfico da distribuição por commit–reveal com HSM/KMS

**Situação:** aceita (06/10/2026)

## Contexto
O regimento proíbe conhecer a designação antes da reunião; o adversário principal é interno (admin, DBA, fornecedor).

## Decisão
Designação cifrada com chave de dados envelopada por chave mestra em HSM/KMS; compromisso SHA-256 carimbado na auditoria; revelação só na abertura da sessão (doc 07). Mudanças no módulo `distribuicao` exigem revisão de segurança.

## Consequências
Sigilo e integridade verificáveis por terceiros. HSM/KMS vira requisito de hospedagem; plano B com chave dividida é mais fraco e precisa de aceite formal.
