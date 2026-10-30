# 03 - Particionamento e vistas

O objetivo deste documento é de justificar a escolha de particionamento de topo tomada, e descrever o sistema nas vistas de módulos (com regras allowed-to-use), vista de componentes e conectores do fluxo desbloquear → devolver → cobrar, e a vista de alocação (onde corre cada parte, quem é responsável por cada componente).
O particionamento parte das componentes finais e dependências indicadas no documento 02-componentes.md.

## 1. Particionamento de topo
### 1.1. Opções

|              | Técnico                                                                                              | Por domínio                                                                                  |
|--------------|------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| Forma        | `apresentacao/`, `negocio/`, `persistencia/`                                                         | `acesso/`, `estacao/`, `viagem/`, `cobranca/`, ...                                           |
| Vantagens    | Preocupações técnicas separadas por camadas, alinhadas com MVC                                       | Cada elemento integra um domínio separado inteiramente, alinha com o estilo Modular Monolith |
| Desvantagens | Os fluxos atravessam todas as camadas e os domínios não ficam concentrados, gerando mais acoplamento | O código comum repete-se em vários domínios                                                  |

### 1.2. Decisão: particionamento por domínio

Apesar de não existir resposta universalmente correta para a implementação, a escolha de particionamento do sistema a ser desenvolvido foi o particionamento por domínio. Esta escolha deve-se principal às seguintes razões:
1. Os fluxos de ação atravessam várias componentes, que se separados em camadas técnicas, seriam espalhados e alterações mínimas em parte deles implicariam alterações em outros domínios.
2. A característica de configurabilidade do sistema. Deste modo, implementações novas afetam só o domínio em que se enquadram, em vez das camadas de apresentação, negócio e persistência.
3. Os componentes situam-se em pacotes únicos, logo, ao apagar e voltar a gerá-los individualmente, nada é alterado em outras componentes do sistema.

### 1.3. Trade-off aceite
Aceitamos o custo de algum código e conceitos comuns atravessarem mais do que um domínio, o que pode criar dependências entre pacotes. Em contrapartida, cada domínio reúne as responsabilidades necessárias para uma função do sistema e pode evoluir sem espalhar uma alteração por camadas técnicas. Consideramos este compromisso adequado ao Lisboa pedala porque os fluxos importantes (como desbloquear, devolver e cobrar uma viagem) atravessam vários domínios. A separação por domínio também ajuda a acomodar tarifários diferentes por município, mantendo as regras de cada função do sistema junto dos componentes que as executam.
Para limitar o acoplamento entre domínios, as dependências serão explícitas e controladas pelas regras `allowed-to-use` da vista de módulos. Os tipos realmente partilhados podem ficar em `comum`, mas esse módulo não deve acumular lógica de negócio. O custo aceite é, portanto, gerir dependências entre domínios. O benefício esperado é concentrar alterações no domínio relevante e facilitar a evolução independente dos componentes.

## 2. Vista de módulos
A implementação é particionada por domínio. Cada componente final definido em [02-componentes.md](02-componentes.md) corresponde a um módulo independente dentro do pacote `lisboa_pedala`. O módulo ´comum´ contém apenas tipos partilhados e não pode conter lógica de negócio.

```text
lisboa_pedala/
├─ comum/
├─ identificacao/
├─ autorizacao/
├─ docas/
├─ viagens/
├─ ocupacao/
├─ disponibilidade/
├─ telemetria/
├─ manutencao/
├─ tarifario/
├─ faturacao/
├─ pagamento/
├─ historico/
├─ redistribuicao/
└─ relatorios/
```

### 2.1. Regras `allowed-to-use`
Uma dependência `A -> B` significa que o módulo `A` pode importar ou chamar interfaces públicas de `B`. A tabela contém todas as dependências permitidas, qualquer dependência não esteja listada é proibida.

| Módulo | Pode depender de |
|:---:|:---|
| `identificacao` | `comum` |
| `autorizacao` | `identificacao`, `manutencao`, `docas`, `comum` |
| `docas` | `comum` |
| `viagens` | `docas`, `comum` |
| `ocupacao` | `docas`, `comum` |
| `disponibilidade` | `ocupacao`, `telemetria`, `manutencao`, `comum` |
| `telemetria` | `comum` |
| `manutencao` | `telemetria`, `comum` |
| `tarifario` | `comum` |
| `faturacao` | `viagens`, `tarifario`, `identificacao`, `comum` |
| `pagamentos` | `faturacao`, `comun` |
| `historico` | `viagens`, `faturacao`, `identificacao`, `comum` |
| `redistribuicao` | `ocupacao`, `comum` |
| `relatorios` | `viagens`, `ocupacao`, `comum` |
| `comum` | Nenhum módulo |

### 2.2. Restrições

1. Um módulo só pode importar ou chamar outro módulo se a dependência estiver indicada na tabela.
2. `comum` não pode depender de nenhum outro módulo nem conter lógica de negócio.
3. Não são permitidas dependências cíclicas entre módulos.
4. Um módulo só pode aceder à interface pública de outro módulo, não pode importar diretamente a sua implementação interna.


## 3. Vista de componentes e conectores

Esta vista descreve o fluxo de uma viagem desde o desbloqueio até à cobrança. Uma seta `A -> B` indica que A envia informação a B. Os eventos são processados de forma assíncrona, menos caso seja indicado o contrário.

### 3.1. Desbloquear

1. `autorizacao` sincroniza com as docas a informação necessária para validar o desbloqueio localmente, incluindo os dados de elegibilidade e o estado das bicicletas.
2. O utilizador apresenta o QR ou o cartão Navegante à doca.
3. A doca decide localmente se autoriza o desbloqueio, usando a informação sincronizada. Esta decisão não depende de uma chamada ao backend em tempo real.
4. A doca guarda localmente o evento de desbloqueio e envia-o ao backend quando houver ligação.
5. `docas` guarda o evento e a sua chave `(id da doca, número de sequência)` antes de confirmar a receção. Depois, publica os eventos por ordem de sequência.
6. `viagens` consome o evento de desbloqueio e abre a viagem, usando o identificador do evento como identificador da viagem.

### 3.2. Devolver

1. O utilizador encaixa a bicicleta numa doca livre.
2. A doca confirma localmente o encaixe, guarda o evento e envia-o ao backend quando houver ligação.
3. `docas` recebe e deduplica o evento, confirma a receção e publica os eventos da doca por ordem de sequência.
4. `viagens` consome o evento de encaixe, fecha a viagem e regista `ViagemFechada`. O fecho da viagem e o registo deste evento acontecem na mesma unidade de trabalho.
5. `ocupacao` atualiza o estado da doca e da estação a partir do evento de encaixe.

### 3.3. Cobrar

1. `faturacao` reage a `ViagemFechada`.
2. `faturacao` obtém de `tarifario` o valor aplicável à duração da viagem, ao tipo de utilizador e ao município. Usa também `identificacao` para obter ou validar os dados do utilizador necessários à faturação.
3. `faturacao` emite e numera a fatura, comunica-a à Autoridade Tributária e arquiva-a.
4. `pagamentos` reage à fatura emitida e tenta cobrar pelo meio de pagamento do utilizador.
5. Se o pagamento falhar, `pagamentos` faz novas tentativas e, se necessário, regista o valor em dívida. A viagem permanece fechada e não é reaberta.

O fluxo de cobrança é assíncrono em relação ao fecho da viagem. Uma falha no pagamento não impede a devolução nem o fecho da viagem.

### 3.4. Conectores do fluxo

| Origem | Destino | Informação | Conector |
|:---:|---|---|---|
| `autorizacao` | Doca | Dados de validação e autorização | Sincronização com a doca |
| Doca | `docas` | Eventos de desbloqueio e encaixe | Envio de eventos; pode ser adiado até a ligação voltar |
| `docas` | `viagens` | Eventos de desbloqueio e encaixe, deduplicados e ordenados | Publicação/consumo de eventos |
| `docas` | `ocupacao` | Eventos de encaixe para atualizar a ocupação | Publicação/consumo de eventos |
| `viagens` | `faturacao` | `ViagemFechada` | Publicação/consumo de eventos |
| `faturacao` | `tarifario` | Duração, tipo de utilizador e município; recebe o montante | Chamada ao componente |
| `faturacao` | `identificacao` | Dados do utilizador necessários à faturação | Chamada ao componente |
| `pagamentos` | `faturacao` | Fatura emitida a cobrar | Reação assíncrona ao evento de fatura |

As decisões de desbloqueio local e sincronização seguem o [ADR-001](adr/ADR-001.md).
A receção idempotente e ordenada dos eventos segue o [ADR-003](adr/ADR-003.md).
O cálculo configurável do preço segue o [ADR-005](adr/ADR-005.md). 
A cobrança assíncrona e independente do fecho segue o [ADR-006](adr/ADR-006.md).


## 4. Vista de alocação

Os componentes da vista lógica são módulos do sistema, não serviços independentes. Na primeira implantação, os módulos de negócio correm no backend da plataforma, organizados como um monólito modular. As docas e bicicletas são equipamentos físicos que comunicam com o backend por canais distintos.

| Componente ou elemento | Onde corre | Responsabilidade na alocação |
|:---:|---|---|
| Aplicação do utilizador | Telemóvel do utilizador | Apresenta o QR e permite consultar viagens e faturas. |
| Decisão local de desbloqueio | Software da doca | Valida localmente o QR ou cartão com informação sincronizada e guarda os eventos durante falhas de rede (ADR-001). |
| `autorizacao` | Backend | Prepara e sincroniza com as docas os dados necessários à autorização local. |
| `docas` | Backend | Recebe eventos das docas, deduplica-os, confirma a receção e publica-os por ordem de sequência (ADR-003). |
| `identificacao` | Backend | Autentica utilizadores e verifica a sua elegibilidade. |
| `viagens` | Backend | Abre e fecha viagens a partir dos eventos recebidos. |
| `ocupacao` | Backend | Atualiza o estado de ocupação das estações. |
| `disponibilidade` | Backend | Disponibiliza a vista de leitura do mapa. |
| `telemetria` | Backend, no processo de receção de telemetria | Recebe as mensagens enviadas diretamente pelas bicicletas e deteta avarias (ADR-004). |
| `manutencao` | Backend | Gere ordens de manutenção e o estado de serviço das bicicletas. |
| `tarifario` | Backend | Calcula o valor da viagem segundo a configuração do município (ADR-005). |
| `faturacao` | Backend | Emite, comunica à AT e arquiva as faturas. |
| `pagamentos` | Backend, no processamento assíncrono de pagamentos | Cobra faturas e trata novas tentativas após falhas (ADR-006). |
| `historico` | Backend | Devolve ao utilizador as suas viagens e faturas. |
| `redistribuicao` | Backend | Calcula sugestões de rotas para as carrinhas. |
| `relatorios` | Backend | Produz os relatórios para a Câmara. |
| `comum` | Backend, como pacote partilhado | Disponibiliza tipos partilhados aos restantes módulos, sem lógica de negócio. |
| Base de dados | Infraestrutura do backend | Guarda os dados necessários às viagens, faturação, eventos e restantes módulos. |
| Autoridade Tributária | Sistema externo | Recebe a comunicação das faturas. |
| Serviço de pagamento | Sistema externo simulado | Processa os pedidos de pagamento de MB WAY ou cartão. |

Os módulos `telemetria` e `pagamentos` podem ser executados por processos de trabalho separados do processamento de pedidos do backend, para que a receção de telemetria e as falhas dos pagamentos não bloqueiem os fluxos de viagens (ADR-004 e ADR-006). A separação é uma decisão de implantação, os componentes continuam a pertencer ao mesmo particionamento lógico por domínio.

A responsabilidade de cada componente pertence ao membro indicado no ficheiro `CODEOWNERS`. Os caminhos e os responsáveis que lá estão registados devem corresponder aos pacotes da vista de módulos e aos membros reais do grupo.