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

As violações de dependência serão detetadas automaticamente por
`RegrasAllowedToUseTest`; violações de encapsulamento por `ApiPublicaTest`;
dependências indevidas de `comum` por `ComumTest`; e ciclos por
`SemCiclosTest`. A revisão humana foi usada para alinhar as regras com a matriz
do `ARCHITECTURE.md` e não foram aceites dependências proibidas; a execução
automática deve ser repetida com Java 21 e Maven disponível.
