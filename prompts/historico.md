# Registo de transparência — historico

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `viagem` e `cobranca` sob o pacote base `pt.lisboapedala`.”

Contexto fornecido: `ARCHITECTURE.md`, `docs/02-componentes.md`, `docs/03-particionamento-e-vistas.md` e a vista de módulos allowed-to-use.

## Resultado e Justificação

Aceite pelo programador. O componente expõe uma consulta pública que combina viagens e faturas por utilizador, tolerando consistência eventual enquanto a fatura ainda não foi emitida.

## Deteção de Violações de Arquitetura

Não foram detetadas violações. A implementação usa apenas `viagens.api`, `faturacao.api` e `comum`, conforme revisão humana dos imports. A execução do ArchUnit com Java 21, depois da correção das regras (ver `archunit.md`), confirmou-o.
