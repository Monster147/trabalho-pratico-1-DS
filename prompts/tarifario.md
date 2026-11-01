# Registo de transparência — tarifario

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-002.md`, `docs/adr/ADR-005.md` e a regra de dependência `tarifario -> comum`.

## Resultado e Justificação

Aceite pelo programador. O cálculo recebe duração, tipo de utilizador e município, lê uma configuração por município e devolve um `Montante`; não contém regras fiscais nem depende de faturação.

## Deteção de Violações de Arquitetura

Não foram detetadas violações. O componente importa apenas tipos de `comum` fora do seu próprio pacote; a revisão humana verificou a separação entre API e implementação interna.
