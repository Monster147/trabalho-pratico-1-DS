# Registo de transparência — `frota.manutencao`

## Pedido e Contexto

Pedido exato: “pt.lisboapedala.frota.manutencao (.api e .internal)” e
“notificando avarias para manutencao (ADR-004)”.

Ficheiros de contexto fornecidos: `ARCHITECTURE.md` e `docs/adr/ADR-004.md`.

## Resultado e Justificação

Aceite pelo programador: a manutenção subscreve `AvariaDetetada` através de
`telemetria.api`, cria ordens em memória e expõe as bicicletas fora de serviço
pela API pública. O componente não importa internals de telemetria nem outros
componentes.

## Deteção de Violações de Arquitetura

Não foi detetada violação. A suite foi executada com Java 21 (OpenJDK 21.0.13) e ArchUnit 1.3.0, através de `mvn test`. As regras do `RegrasAllowedToUseTest` e
do `ApiPublicaTest` que dizem respeito a este componente foram avaliadas uma a
uma e passaram sem violações: o componente depende apenas de `telemetria.api` e
`comum`, e ninguém usa os seus internals.

Depois da correção das regras ArchUnit (ver `archunit.md`), a suite completa passa sem violações.
