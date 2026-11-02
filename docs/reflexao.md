# Reflexão

A especificação ajudou-nos a organizar os módulos, limitar as dependências entre componentes e descrever o fluxo principal de desbloqueio, devolução e cobrança. Durante a implementação, porém, percebemos que algumas decisões precisavam de mais detalhe para evitar interpretações diferentes.

## O que a especificação não previu

A persistência foi um desses pontos. O diário das docas grava eventos e chaves num ficheiro antes de confirmar a receção, mas viagens, faturas e dívidas são guardadas em estruturas em memória. Por isso, esses dados perdem-se se a aplicação reiniciar. Além disso, as anotações `@Transactional` assentam num gestor de transações que não faz nada: não garantem que uma alteração e a publicação do evento sejam revertidas em conjunto se ocorrer uma falha.

A especificação também não dizia de onde vinha o tipo de utilizador de cada viagem. `viagens` precisa dele para a tarifa, mas só pode usar `docas.api`, e o evento `DesbloqueioEfetuado(docaId, numSequencia)` não o trazia. A solução foi o QR assinado passar a incluir o tipo, e a doca enviá-lo no evento (no desbloqueio com cartão Navegante é sempre assinante).

Também não estava dito como o tarifário de cada município chega à aplicação. O ADR-002 decide que é configuração, mas nada a carregava no arranque, e a cobrança da US-1 falhava fora dos testes. Os tarifários passaram a ser lidos de `application.properties`.

Ficaram ainda por esclarecer como tratar avarias reportadas por utilizadores, que dados devem ser recuperados após uma falha e que informação pode ser exposta nos relatórios e nos futuros dados abertos.

## Violações cometidas pela IA

Encontrámos desvios concretos à especificação, todos registados em `prompts/`:

- Na sincronização das docas (K2), o código entregou os dados através de `docas.api`, em vez de seguir o caminho direto representado na vista C&C.
- Na disponibilidade, foi criado o evento `OcupacaoAtualizada`, embora a vista descrevesse uma leitura síncrona de `ocupacao.api`.
- A persistência das docas usou um ficheiro, e não a base de dados relacional do C4.
- `viagens` não consumia os eventos de `docas`: nenhuma viagem abria nem fechava. Além disso, assumia sempre o tipo `OCASIONAL`, pelo que um assinante seria cobrado com a tarifa errada.
- `faturacao` publicava `FaturaEmitida` fora de qualquer transação, e o listener de `pagamentos`, que só reage depois de um commit, ignorava-o: as faturas eram emitidas, mas nunca cobradas.
- O tarifário não era carregado no arranque: o ADR-002 decide que é configuração por município, mas o código só o permitia configurar por código, e a aplicação a correr não tinha tarifário.
- Na 1.ª versão, a IA escreveu código no pacote de outro componente (`frota.manutencao.internal`) para o projeto compilar.

## O que as fitness functions apanharam e o que passou despercebido

Nenhuma destas violações foi apanhada pelo ArchUnit. Todas usavam dependências permitidas pela matriz, e as regras só verificam dependências entre pacotes: não verificam se os conectores seguem os diagramas, que eventos cada componente consome, nem onde se persiste.

Os três primeiros casos e o último foram detetados na revisão humana. Os restantes (`viagens`, `faturacao` e o tarifário) passaram despercebidos até mais tarde: só foram encontrados na verificação final do repositório e confirmados pelos testes ponta a ponta, que falham sem as correções.

As próprias fitness functions também tinham erros. Quando finalmente correram com Java 21, falharam 3 dos 7 testes, mas eram falsos positivos: uma regra proibia o domínio `estacao` inteiro a `autorizacao`, apesar de `docas.api` ser permitida; outra analisava também as classes de teste; e o teste de ciclos procurava ciclos entre domínios em vez de entre componentes. Pior, como as regras de dependências são verificadas em sequência no mesmo método, a primeira falha escondia as 12 regras seguintes. Depois de corrigidas, as fitness functions e os testes das user stories passam com um único `mvn test`.

## O que faríamos diferente

Retiramos três lições. Uma especificação com vistas e diagramas não chega se as regras verificáveis só cobrirem dependências: seria útil acrescentar regras sobre que eventos cada componente pode ouvir. Os testes das user stories, escritos mais cedo, teriam apanhado os erros de `viagens` e `faturacao` logo na geração. E as fitness functions têm de correr desde o primeiro dia, porque um teste que nunca correu pode estar errado sem ninguém saber.
