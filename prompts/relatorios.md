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

Não foi detetada violação na revisão do código. A fitness function
`RegrasAllowedToUseTest` verifica que o componente não depende de internals ou
de domínios não autorizados; a revisão humana confirmou o mesmo no desenho das
interfaces. A execução da suite ficou bloqueada pelo runtime Java 25 disponível,
incompatível com o parser do ArchUnit 1.3.0.
