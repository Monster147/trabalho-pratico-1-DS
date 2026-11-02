# Registo de transparência — `pt.lisboapedala.comum`

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base
nas especificações do ficheiro ARCHITECTURE.md, gera a estrutura e o código para
os domínios frota, municipio, o kernel comum e a suite de testes de arquitetura
com ArchUnit.”

Âmbito atribuído: “pt.lisboapedala.comum (Value Objects imutáveis: Montante,
DocaId, UtilizadorId, EventoBase — ESTRITAMENTE SEM LÓGICA DE NEGÓCIO)”.
Ficheiro de contexto fornecido: `ARCHITECTURE.md`.

## Resultado e Justificação

Aceite pelo programador: implementação de `EventoBase` e manutenção das
validações de invariantes dos VOs. A operação `Montante.somar` foi removida por
ser comportamento de domínio, mantendo o pacote comum limitado a representação
e invariantes.

## Deteção de Violações de Arquitetura

Não foi detetada violação. A suite foi executada com Java 21 (OpenJDK 21.0.13) e ArchUnit 1.3.0, através de `mvn test`. O `ComumTest` (2 testes) e a regra
`comumNaoImportaModulos` do `RegrasAllowedToUseTest` passaram: `comum` não
depende de nenhum outro pacote do projeto nem de frameworks. A remoção da
operação de soma foi uma decisão de revisão humana do programador.

Depois da correção das regras ArchUnit (ver `archunit.md`), a suite completa passa sem violações.
