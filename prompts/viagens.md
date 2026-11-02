# Registo de transparência — viagens

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-003.md`, `docs/adr/ADR-005.md`, `docs/adr/ADR-006.md`, `docs/diagramas/cc-desbloquear-devolver-cobrar.puml` e a matriz allowed-to-use.

## Resultado e Justificação

Aceite pelo programador. Foi implementado o ciclo de vida em `viagem.viagens`, com abertura por evento de desbloqueio, fecho idempotente e publicação de `ViagemFechada`. O fecho e a publicação estão no mesmo método anotado com `@Transactional`, preservando a unidade de trabalho exigida.

## Deteção de Violações de Arquitetura

**Caso 1 — `viagens` não consumia os eventos de `docas`.** A especificação (secção 4 do `ARCHITECTURE.md` e vista C&C) diz que `viagens` consome `DesbloqueioEfetuado` e `BicicletaEncaixada`. O código gerado só tinha métodos públicos `abrir` e `fechar`, que ninguém chamava: nenhuma viagem abria nem fechava, e a US-1 não funcionava de ponta a ponta.

**Caso 2 — Tipo de utilizador fixo.** Sem o tipo no evento, `abrir(evento)` assumia sempre `OCASIONAL`, pelo que um assinante seria cobrado com a tarifa errada.

- **Detetado por:** ninguém até mais tarde. Os dois casos passaram nas fitness functions (as dependências eram permitidas) e na revisão inicial. Só foram encontrados na verificação final do repositório, e confirmados pelos testes da US-1 e da US-3.

Fora estes casos, as dependências respeitam a matriz (ArchUnit sem violações).
