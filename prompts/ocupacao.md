# Registo de transparência — ocupacao

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `acesso` e `estacao` sob o pacote base `pt.lisboapedala`.”

Segundo pedido, na mesma conversa: “Volta a executar a minha prompt mas sobre este código novo”, sem entrar em conflito com o código já feito pelo colega.

Contexto fornecido: `ARCHITECTURE.md` e `codigo.zip` (código já implementado pelo grupo).

## Resultado e Justificação

A primeira versão foi rejeitada, porque não seguia as convenções do código que o grupo já tinha. A segunda versão foi aceite pelo programador.

Mantém a ocupação de cada estação doca a doca, reagindo aos eventos de `docas.api`. Como o `BicicletaEncaixada` não traz a bicicleta, esta é obtida pelo `viagemId` do desbloqueio. Pendente: o grupo tem de validar a reação ao `DesbloqueioEfetuado` e o endpoint de registo de estações, que não estavam especificados.

## Deteção de Violações de Arquitetura

**Caso 1 — Subscrição de um evento não previsto.** No C4 e na vista C&C, `docas` só publica `BicicletaEncaixada` para `ocupacao`. O código gerado subscreve também `DesbloqueioEfetuado`, para a doca ficar livre quando a bicicleta sai. Sem isto a contagem nunca baixava, por isso a lacuna está na especificação, mas o código diverge dela.

- **Detetado por:** revisão humana.
- **Porque não foi a fitness function:** `docas.api` é uma dependência permitida, e o ArchUnit não verifica que eventos cada componente consome.

Fora este caso, as dependências respeitam a matriz (ArchUnit sem violações).
