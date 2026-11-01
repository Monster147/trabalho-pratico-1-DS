# Registo de transparência — faturacao

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-002.md`, `docs/adr/ADR-005.md`, `docs/adr/ADR-006.md` e `docs/diagramas/cc-desbloquear-devolver-cobrar.puml`.

## Resultado e Justificação

Aceite pelo programador. O listener reage assincronamente a `ViagemFechada`, calcula o preço através de `tarifario.api`, obtém o NIF via `identificacao.api`, numera a fatura e publica `FaturaEmitida`.

## Deteção de Violações de Arquitetura

Não foram detetadas violações. A revisão humana confirmou que as dependências externas estão limitadas às três APIs autorizadas e a `comum`.
