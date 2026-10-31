# Registo de transparência — viagens

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-003.md`, `docs/adr/ADR-005.md`, `docs/adr/ADR-006.md`, `docs/diagramas/cc-desbloquear-devolver-cobrar.puml` e a matriz allowed-to-use.

## Resultado e Justificação

Aceite pelo programador. Foi implementado o ciclo de vida em `viagem.viagens`, com abertura por evento de desbloqueio, fecho idempotente e publicação de `ViagemFechada`. O fecho e a publicação estão no mesmo método anotado com `@Transactional`, preservando a unidade de trabalho exigida.

## Deteção de Violações de Arquitetura

Não foram detetadas violações. A dependência externa usada pela implementação é exclusivamente `estacao.docas.api`; os contratos do próprio componente estão em `viagens.api`. A verificação foi feita por revisão humana e inspeção dos imports; não foi introduzida uma fitness function ArchUnit porque o repositório não tinha build Java pré-existente.
