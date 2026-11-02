# Registo de transparência — testes das user stories

## Pedido e Contexto

Pedido exato: “dá-me as correções do ArchUnit, [...] Dá-me as os testes das user stories e ainda o US-1 ponta a ponta”.

Contexto fornecido: o repositório do grupo (`ARCHITECTURE.md`, `docs/02-componentes.md`, `docs/adr/` e `docs/diagramas/`) e o enunciado do trabalho.

## Resultado e Justificação

Aceite pelo programador. Os testes cobrem, pela API REST, as três user stories escolhidas em `02-componentes.md` (US-1, US-2 e US-3), com uma doca simulada que valida o QR só com a chave pública sincronizada (ADR-001). Foram ainda repostos os testes unitários da receção idempotente das docas (ADR-003) e da verificação local do QR.

## Deteção de Violações de Arquitetura

Não foram detetadas violações nos testes. Foram eles que confirmaram quatro violações que o ArchUnit não apanhava: `viagens` não consumia os eventos de `docas`, todas as viagens eram cobradas como ocasionais, `FaturaEmitida` não chegava a `pagamentos` e não havia tarifário configurado (ver `viagens.md`, `faturacao.md` e `tarifario.md`).
