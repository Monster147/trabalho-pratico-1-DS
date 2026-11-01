# Registo de transparência — docas

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `acesso` e `estacao` sob o pacote base `pt.lisboapedala`.”

Segundo pedido, na mesma conversa: “Volta a executar a minha prompt mas sobre este código novo”, sem entrar em conflito com o código já feito pelo colega.

Contexto fornecido: `ARCHITECTURE.md` e `codigo.zip` (código já implementado pelo grupo).

## Resultado e Justificação

A primeira versão foi rejeitada, porque não seguia as convenções do código que o grupo já tinha. A segunda versão foi aceite pelo programador.

Recebe os eventos das docas de forma idempotente: a chave `(docaId, numeroSequencia)` é gravada em disco antes de enviar o ACK (ADR-003). Publica os eventos por ordem de sequência, usando os records `DesbloqueioEfetuado` e `BicicletaEncaixada` que já existiam, sem os alterar.

## Deteção de Violações de Arquitetura

**Caso 1 — Persistência fora da base de dados especificada.** O C4 diz que `docas` persiste eventos e chaves na base de dados relacional (SQLite/PostgreSQL). Na 1.ª versão o código usou H2. Na 2.ª, como o projeto do grupo não tem base de dados, usa um ficheiro em disco. A garantia do ADR-003 mantém-se (a chave é gravada antes do ACK), mas o meio de persistência diverge do especificado.

- **Detetado por:** revisão humana.
- **Porque não foi a fitness function:** o ArchUnit não verifica a tecnologia de persistência.

Fora este caso, as dependências respeitam a matriz (ArchUnit sem violações).
