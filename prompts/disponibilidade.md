# Registo de transparência — disponibilidade

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `acesso` e `estacao` sob o pacote base `pt.lisboapedala`.”

Segundo pedido, na mesma conversa: “Volta a executar a minha prompt mas sobre este código novo”, sem entrar em conflito com o código já feito pelo colega.

Contexto fornecido: `ARCHITECTURE.md` e `codigo.zip` (código já implementado pelo grupo).

## Resultado e Justificação

A primeira versão foi rejeitada, porque não seguia as convenções do código que o grupo já tinha. A segunda versão foi aceite pelo programador.

Serve o mapa em tempo real a partir de uma cache em memória que nunca vai à escrita transacional (C2). A cache é atualizada por `OcupacaoAtualizada` e pela telemetria. Pendente: `telemetria.api` e `manutencao.api` são contratos provisórios.

## Deteção de Violações de Arquitetura

**Caso 1 — Evento novo fora da especificação.** No C4, `disponibilidade` lê a ocupação por chamada síncrona a `ocupacao.api`. O código gerado criou um evento novo, `OcupacaoAtualizada`, publicado por `ocupacao` e consumido aqui para atualizar a cache.

- **Detetado por:** revisão humana.
- **Porque não foi a fitness function:** o evento está em `ocupacao.api`, que é uma dependência permitida.

**Caso 2 — Código gerado no pacote de outro componente (1.ª versão).** Para o projeto compilar, a IA criou uma implementação provisória em `frota.manutencao.internal`, um componente que não estava atribuído a este programador.

- **Detetado por:** revisão humana.
- **Porque não foi a fitness function:** o ArchUnit não verifica se o código gerado está no pacote correto.
- **Correção:** na 2.ª versão a classe foi removida, e a dependência passou a `Optional<ManutencaoApi>`.

Na versão final, as dependências respeitam a matriz (ArchUnit sem violações).
