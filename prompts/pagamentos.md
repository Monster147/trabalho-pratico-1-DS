# Registo de transparência — pagamentos

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-006.md` e `docs/diagramas/cc-desbloquear-devolver-cobrar.puml`.

## Resultado e Justificação

Aceite pelo programador. O listener de `FaturaEmitida` executa a cobrança em modo assíncrono; uma falha gera uma `Divida` com tentativa e próxima data de retentativa, sem qualquer referência a `viagens`, pelo que a viagem permanece fechada.

## Deteção de Violações de Arquitetura

Não foram detetadas violações. A revisão humana confirmou que o componente importa apenas `faturacao.api` e `comum`; o gateway permanece interno. A execução do ArchUnit com Java 21, depois da correção das regras (ver `archunit.md`), confirmou-o.
