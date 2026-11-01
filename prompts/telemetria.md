# Registo de transparência — `frota.telemetria`

## Pedido e Contexto

Pedido exato: “Em telemetria: Ingestor rápido de mensagens emitidas a cada 30s
pelas bicicletas (~35 msg/s), retendo o estado mais recente e notificando
avarias para manutencao (ADR-004).”

Ficheiros de contexto fornecidos: `ARCHITECTURE.md` e `docs/adr/ADR-004.md`.

## Resultado e Justificação

Aceite pelo programador: foi criada uma API de telemetria, retenção concorrente
do último estado por bicicleta, deteção de bateria baixa e endpoint de ingestão.
`ConcurrentHashMap` e consumidores `CopyOnWriteArrayList` suportam o padrão de
escrita frequente sem introduzir dependências de outros componentes.

## Deteção de Violações de Arquitetura

Não foi detetada violação allowed-to-use na revisão do código. A regra
`RegrasAllowedToUseTest` deteta dependências proibidas; a execução da suite ficou
bloqueada pelo runtime Java 25 disponível, incompatível com o parser do
ArchUnit 1.3.0. A revisão humana confirmou que a implementação usa apenas
`comum` fora do próprio componente.
