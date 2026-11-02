# Registo de transparência — tarifario

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/adr/ADR-002.md`, `docs/adr/ADR-005.md` e a regra de dependência `tarifario -> comum`.

## Resultado e Justificação

Aceite pelo programador. O cálculo recebe duração, tipo de utilizador e município, lê uma configuração por município e devolve um `Montante`; não contém regras fiscais nem depende de faturação.

## Deteção de Violações de Arquitetura

**Caso 1 — Nenhum tarifário configurado na aplicação.** O ADR-002 decide que cada município tem o seu tarifário em configuração. O código gerado só permitia configurar tarifários por código (`configurar`), e nada o fazia no arranque: a aplicação a correr não tinha tarifário para Lisboa, e a faturação da US-1 falhava com "Tarifário inexistente".

- **Detetado por:** ninguém até mais tarde. Passou nas fitness functions e no teste unitário do tarifário, que configura o tarifário à mão. Só foi visto ao escrever o teste ponta a ponta da US-1.

Fora este caso, o componente importa apenas `comum` (ArchUnit sem violações).
