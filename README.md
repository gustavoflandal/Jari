# Jari — SIREJ

Sistema Integrado de Recursos de Infrações de Trânsito (JARI): processo administrativo eletrônico para o rito recursal de infrações de trânsito.

## Para quem vai desenvolver

Comece por [CLAUDE.md](CLAUDE.md) (invariantes e regras para agentes) e pelo [índice da documentação de referência](docs/dev/00-indice.md). Decisões de arquitetura em [docs/adr](docs/adr/README.md); configurações de regimento em [config/regimentos](config/regimentos). Para executar uma fase do plano com agentes, use os [prompts por fase](docs/prompts/README.md).

### Build local

Requisitos: JDK 25, Node 22.12+ e npm 10.

```sh
./mvnw verify            # backend: testes e ApplicationModules.verify()
cd frontend && npm ci && npm test
```

## Documentação de projeto

| Documento | Conteúdo |
|---|---|
| [docs/plano/plano-projeto-sirej.md](docs/plano/plano-projeto-sirej.md) | Plano do projeto v0.5 (licitação, modelo de SP como alvo, Curitiba em espera): trilhas de produto, proposta e contrato, equipe, riscos e perguntas em aberto |
| [docs/fontes/enunciado-projeto-sistema-jari.md](docs/fontes/enunciado-projeto-sistema-jari.md) | Enunciado do projeto v1.0 |
| [docs/fontes/arquitetura-infraestrutura-sirej.md](docs/fontes/arquitetura-infraestrutura-sirej.md) | Arquitetura técnica e infraestrutura v1.0 |
| [docs/fontes/Estudo_JARI.pdf](docs/fontes/Estudo_JARI.pdf) | Notas de pesquisa que originaram o enunciado |
| [docs/fontes/JARI_CET.pdf](docs/fontes/JARI_CET.pdf) | Edital nº 001/2026-JARI/CET com o Regimento das JARIs (Comunicado 007/23) |
