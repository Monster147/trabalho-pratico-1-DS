# Registo de transparência — `municipio.relatorios`

## Pedido e Contexto

Pedido exato: “Em relatorios: Agregador mensal de métricas de utilização e
estimativa de CO₂ evitado por freguesia.”

Ficheiro de contexto fornecido: `ARCHITECTURE.md`.

## Resultado e Justificação

Aceite pelo programador: foi criado um agregador mensal que consulta apenas
`viagens.api` e `ocupacao.api`, calcula minutos de utilização, bicicletas
conhecidas e CO2 evitado, e disponibiliza o relatório por endpoint.

## Deteção de Violações de Arquitetura

Não foi detetada violação. A suite foi executada com Java 21 (OpenJDK 21.0.13) e ArchUnit 1.3.0, através de `mvn test`. As regras do `RegrasAllowedToUseTest` e
do `ApiPublicaTest` que dizem respeito a este componente foram avaliadas uma a
uma e passaram sem violações. A revisão humana confirmou o mesmo no desenho das
interfaces.

Depois da correção das regras ArchUnit (ver `archunit.md`), a suite completa passa sem violações.
