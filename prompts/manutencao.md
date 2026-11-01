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

Não foi detetada violação na revisão do código. `RegrasAllowedToUseTest` verifica
a dependência exclusiva em `telemetria.api` e `comum`, enquanto
`ApiPublicaTest` verifica o encapsulamento dos internals. A execução ficou
bloqueada pelo runtime Java 25 disponível, incompatível com o parser do
ArchUnit 1.3.0.
