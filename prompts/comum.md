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

Não foi detetada violação de dependência entre `comum` e outros pacotes na
revisão do código. A regra `ComumTest` foi criada para automatizar a verificação,
mas a execução ficou bloqueada pelo runtime Java 25 disponível, incompatível
com o parser do ArchUnit 1.3.0; a remoção da operação de soma foi uma decisão
de revisão humana do programador.
