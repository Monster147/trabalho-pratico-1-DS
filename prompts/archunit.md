# Registo de transparência — Suite ArchUnit

## Pedido e Contexto

Pedido exato: “Em src/test/java/pt/lisboapedala/architecture/: Implementa os
testes de arquitetura com ArchUnit (RegrasAllowedToUseTest, ApiPublicaTest,
ComumTest, SemCiclosTest).”

Ficheiro de contexto fornecido: `ARCHITECTURE.md`, especialmente as secções
“Matriz de Dependências Permitidas” e “Requisitos para Testes de Governação”.

## Resultado e Justificação

Aceite pelo programador: a suite foi completada para todo o monólito, cobrindo
allowed-to-use, acesso a APIs públicas, isolamento de `comum` e ciclos entre
domínios. As regras foram mantidas independentes de frameworks de produção e
importam todos os pacotes `pt.lisboapedala`. A compilação Java 21 das fontes
relevantes passou; a execução foi bloqueada porque o ambiente só disponibiliza
Java 25 e o ArchUnit 1.3.0 não suporta o bytecode da JDK usada para os módulos
do runtime.

## Deteção de Violações de Arquitetura

Na primeira execução com Java 21 (OpenJDK 21.0.13) e ArchUnit 1.3.0, através de
`mvn test`, falharam 3 dos 7 testes que o projeto tinha na altura. Ao analisar as
falhas, concluiu-se que nenhuma corresponde a uma violação real do código: são
erros nas próprias regras.

- `RegrasAllowedToUseTest`: a regra de `autorizacao` proíbe `..estacao..`
  inteiro, mas a matriz permite `docas.api`. Como as regras são verificadas em
  sequência no mesmo método, esta falha impede que as 12 regras seguintes sejam
  avaliadas.
- `ApiPublicaTest`: a importação inclui as classes de teste, e o
  `TarifarioServiceTest` usa `tarifario.internal`.
- `SemCiclosTest`: procura ciclos entre domínios (`pt.lisboapedala.(*)..`) em
  vez de entre componentes, e assinala o ciclo `viagem ↔ cobranca`
  (`faturacao → viagens.api` e `historico → faturacao.api`), que a matriz
  permite.

Com as três regras corrigidas, as fitness functions passam e não há violações
reais.

Detetado por: a execução da fitness function. A conclusão de que eram falsos
positivos resultou da revisão humana das falhas.

Nota: nenhuma das violações reais registadas nos outros ficheiros foi apanhada
por estas regras, porque nenhuma envolve uma dependência proibida. São elas: a
sincronização K2, o evento `OcupacaoAtualizada`, a persistência das docas,
`viagens` sem ouvir os eventos, o tipo de utilizador fixo, `FaturaEmitida` sem
chegar a `pagamentos` e o tarifário por configurar.
