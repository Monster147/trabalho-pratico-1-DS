# Reflexão

A especificação ajudou-nos a organizar os módulos, limitar as dependências entre componentes e descrever o fluxo principal de desbloqueio, devolução e cobrança. Durante a implementação, porém, percebemos que algumas decisões precisavam de mais detalhe para evitar interpretações diferentes.

A persistência foi um desses pontos. O diário das docas grava eventos e chaves num ficheiro antes de confirmar a receção, mas viagens, faturas e dívidas são guardadas em estruturas em memória. Por isso, esses dados perdem-se se a aplicação reiniciar. Além disso, embora o fecho da viagem esteja anotado com @Transactional, o estado é atualizado numa estrutura em memória; essa anotação, por si só, não garante que a alteração e a publicação do evento sejam revertidas em conjunto se ocorrer uma falha.

Também encontrámos diferenças entre o que os diagramas sugerem e o que o código faz. Por exemplo, a sincronização das docas segue um caminho diferente do indicado na vista C&C, e a disponibilidade recebe atualizações de ocupação através de um evento que não estava descrito dessa forma. Os registos em prompts/ documentam estes casos e explicam como foram identificados. São exemplos úteis para perceber o que as verificações arquiteturais detetam e o que exige revisão do comportamento.

Os testes ArchUnit verificam regras como as dependências permitidas e a ausência de ciclos. Há também um teste unitário do tarifário, mas estes testes não demonstram, por si só, que todos os percursos das histórias de utilizador funcionam de ponta a ponta. Para isso, seria útil testar cenários completos de desbloqueio, devolução e cobrança, incluindo falhas e reinícios.

Durante a geração, encontrámos desvios concretos à especificação. Na sincronização das docas, o código entregou os dados através de docas.api, em vez de seguir o caminho direto representado na vista C&C. Na disponibilidade, foi criado o evento OcupacaoAtualizada, embora a vista descrevesse uma leitura síncrona de ocupacao.api. Estes casos passaram despercebidos aos testes ArchUnit: as dependências usadas eram permitidas pela matriz, e os testes não verificam se os conectores implementam o fluxo dos diagramas. Já a persistência das docas divergiu do meio descrito no C4, tendo sido usado um ficheiro em vez de uma base de dados relacional, e essa diferença foi detetada na revisão humana.

A especificação também poderia esclarecer melhor como tratar avarias reportadas por utilizadores, que dados devem ser persistidos e recuperados após uma falha, e que informação pode ser exposta nos relatórios e nos futuros dados abertos. Numa próxima iteração, detalharíamos esses contratos e cenários e confirmaríamos que os registos de geração cobrem os casos pedidos com ocorrências reais do grupo.

