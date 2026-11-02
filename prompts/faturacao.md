# Registo de transparência — faturacao

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-002.md`, `docs/adr/ADR-005.md`, `docs/adr/ADR-006.md` e `docs/diagramas/cc-desbloquear-devolver-cobrar.puml`.

## Resultado e Justificação

Aceite pelo programador. O listener reage assincronamente a `ViagemFechada`, calcula o preço através de `tarifario.api`, obtém o NIF via `identificacao.api`, numera a fatura e publica `FaturaEmitida`.

## Deteção de Violações de Arquitetura

**Caso 1 — `FaturaEmitida` nunca chegava a `pagamentos`.** A especificação (ADR-006 e vista C&C) diz que `pagamentos` subscreve `FaturaEmitida`. O evento era publicado fora de qualquer transação, e o listener `AFTER_COMMIT` de `pagamentos` ignora eventos sem transação: as faturas eram emitidas, mas nunca cobradas.

- **Detetado por:** ninguém até mais tarde. As dependências estavam corretas, por isso as fitness functions passavam, e a revisão humana inicial não o apanhou. Só foi encontrado pelo teste da US-1.

Fora este caso, as dependências respeitam a matriz (ArchUnit sem violações).
