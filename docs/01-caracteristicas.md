# 01 — Características arquiteturais

Este documento visa identificar 7 características candidatas a partir do enunciado, escolher as 3 que melhor representam o sistema e associar cada uma a um cenário concreto.

## 1. Método de avaliação

Para avaliar as possíveis características arquiteturais existentes no sistema, utilizamos os três critérios aprendidos em aula:

- É uma consideração de design fora do domínio;
- Irá influenciar um aspeto estrutural do design;
- É crítica ou importante para o sucesso do sistema.

## 2. 7 características candidatas


| # | Característica          | Tipo      | Categoria   | Frase do enunciado de onde saiu                                                                                                                                                                                   | Porque passa os 3 critérios                                                                                                                                                                                                                                                          |
|---|-------------------------|-----------|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | Disponibilidade         | Implícito | Operacional | «O contrato de concessão aplica uma multa por cada hora em que mais de 10% das estações não conseguem desbloquear bicicletas» ou «As docas comunicam por rede móvel e há zonas onde a rede falha com frequência.» | Fora do domínio, por envolver a continuidade do serviço perante falhas; estrutural, por condicionar a gestão de falhas, a reconciliação de operações e a idempotência; crítica para a sustentabilidade financeira e a reputação.                                                     |
| 2 | Elasticidade            | Implícito | Operacional | «Cerca de 70% dos desbloqueios acontecem entre as 8h00–9h30 e as 17h30–19h00. Em eventos (Santos Populares, maratona) a procura em algumas estações multiplica por 4.»                                            | Fora do domínio, por estar relacionada com a capacidade de processamento; estrutural, por poder exigir o ajuste dinâmico dos recursos e componentes capazes de suportar cargas variáveis; crítica para manter a capacidade de resposta durante os picos de procura.                  |
| 3 | Configurabilidade       | Implícito | Estrutural  | «Duas câmaras vizinhas mostraram interesse em usar a plataforma com tarifários próprios.»                                                                                                                         | Fora do domínio, por envolver a adaptação do sistema às regras de cada entidade; estrutural, por exigir tarifários configuráveis e independentes da lógica de negócio; crítica para permitir a expansão do negócio sem duplicação de código.                                         |
| 4 | Desempenho              | Explícito | Operacional | «Mapa em tempo real com bicicletas e docas livres por estação.»                                                                                                                                                   | Fora do domínio, por envolver a latência e o débito do sistema; estrutural, por poder exigir mecanismos de atualização assíncrona e disponibilização eficiente do estado das estações; importante, mas não crítico, desde que pequenos atrasos não comprometam a utilização do mapa. |
| 5 | Segurança               | Implícito | Transversal | «Desbloquear uma bicicleta na estação com a app (código QR) ou com o cartão Navegante.» ou «O percurso de cada viagem é dado pessoal (RGPD).»                                                                     | Fora do domínio, por envolver autenticação, autorização e proteção de dados; estrutural, por condicionar a arquitetura de autenticação e o tratamento dos dados pessoais; crítica para prevenir fraudes e garantir o cumprimento do RGPD.                                            |
| 6 | Legal / Arquivabilidade | Implícito | Transversal | «As faturas são comunicadas à Autoridade Tributária e guardadas durante 10 anos.»                                                                                                                                 | Fora do domínio, por decorrer de obrigações legais; estrutural, por exigir persistência duradoura e conservação dos registos durante 10 anos; crítica para garantir o cumprimento legal.                                                                                             |
| 7 | Interoperabilidade      | Explícito | Transversal | «Cartão Navegante.» ou «MB WAY ou cartão.»                                                                                                                                                                        | Fora do domínio, por depender da integração com serviços externos; estrutural, por exigir mecanismos de comunicação e integração com sistemas de terceiros; importante para disponibilizar diferentes métodos de acesso e pagamento.                                                 |

## 3. Decomposição de uma característica composta

A característica arquitetural «Segurança» pode ser decomposta nas seguintes subcaracterísticas:

| Subcaracterística | Aplicação no Lisboa Pedala                                                                      |
|-------------------|-------------------------------------------------------------------------------------------------|
| Autenticação      | Verificar a identidade do utilizador através da aplicação ou do cartão Navegante.               |
| Autorização       | Garantir que cada utilizador só pode executar as operações permitidas.                          |
| Confidencialidade | Proteger os dados pessoais dos utilizadores, como o percurso das viagens, de acordo com o RGPD. |

## 4. Top 3 características arquiteturais

### 4.1. Disponibilidade

- Cenário: Durante uma falha de comunicação com determinadas estações, o sistema deve continuar a permitir o desbloqueio nas estações operacionais e sincronizar os eventos armazenados localmente quando a comunicação for restabelecida, evitando operações duplicadas.

- Implicações arquiteturais: Tolerância a falhas de comunicação, armazenamento temporário de eventos, sincronização e processamento idempotente.

### 4.2. Elasticidade

- Cenário: Durante os períodos de pico, como as manhãs e finais de tarde, o sistema deve ser capaz de escalar automaticamente os recursos para lidar com o aumento da procura, garantindo que todos os desbloqueios e bloqueios de bicicletas sejam processados sem atrasos significativos.

- Implicações arquiteturais: Capacidade de ajustar os recursos de processamento à variação da procura e evitar limitações causadas por recursos partilhados.

### 4.3. Configurabilidade

- Cenário: Quando uma nova câmara municipal adere à plataforma com um tarifário diferente, o sistema deve permitir configurar as suas regras de cobrança sem alterar a lógica de negócio comum nem duplicar o código da aplicação.

- Implicações arquiteturais: Flexibilidade na configuração de regras de negócio e capacidade de suportar diferentes cenários de uso sem necessidade de alterações no código-fonte.

## 5. Outras consideradas

| Característica | Porque não entrou no top 3 |
|---|---|
| Desempenho | Importante, mas não crítica: pequenos atrasos não comprometem o uso do mapa. O isolamento das leituras do mapa já é garantido pela Elasticidade. |
| Segurança | Crítica, mas resolve-se com mecanismos conhecidos (autenticação, QR assinado, proteção de dados) sem ser a que mais condiciona a divisão em componentes. |
| Legal / Arquivabilidade | É uma obrigação real, mas fica concentrada num só componente (`faturacao`) e não condiciona o resto da arquitetura. |