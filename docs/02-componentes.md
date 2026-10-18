# 02 - Componentes 

Este documento tem como objetivo identificar os componentes lógicos, explicitar as abordagens de identificação, mapear User Stories aos componentes, analisar a coesão e o acoplamento e documentar a iteração de restruturação fundamentada nas Top 3 Características Arquiteturais do documento #01.

## 1. Ligação com as Top 3 Características Arquiteturais do #01

A decomposição lógica do sistema é governada pelas três características condutoras selecionadas no documento #01:

1. C1 - Disponibilidade (Operacional/Implícita): Exige isolar a porta de entrada e a reconciliação de eventos, com mecanismos de armazenamento local, garantindo que o serviços seja contínuo sem duplicar viagens ou cobranças durante falhas de rede.

2. C2 - Elasticidade (Operacional/Implícita): Exige separar o canal de frequência elevada de telemetria contínua elevada (~35 msgs/s vindas das 1050 bicicletas elétricas) do estado estático das estações (`frota` separado de `estacoes`), além de isolar organização do fluxo nos momentos de pico (70% nas horas de ponta/4x em eventos) para evitar contágio de recursos.

3. C3 - Configurabilidade (Estrutural/Implícita): Exige isolar o cálculo dos tarifários e a faturação (`faturacao`) do ciclo de vida das viagens (`viagem`), permitindo desta forma suportar câmaras municipais vizinhas com regras financeiras próprias sem alterar a lógica central de negócio.

## 2. Abordagem de identificação de Componentes

Para evitar o antipadrão da Entity Trap, utilizámos as duas abordagens complementares em zonas distintas do domínio:

- Workflow: Aplicado ao fluxo de negócio principal (desbloquear -> devolver -> cobrar - US-1), por ser ua sequência de passos ordenada com um happy path crítico bem definido).
- Actor/Action: Aplicado aos restantes atores, por não haver um fluxo ordenado de passos e se tratar de ações independentes sem happy path crítico.

### 2.1 Worflow do Fluxo Principal (US-1)
| #  | Passo (Happy Path)                                              |   Componente Responsável   |
|:--:|:----------------------------------------------------------------|:--------------------------:|
| 1  | Utilizzador autentica-se e solicita desbloqueio na app          | `acessos` -> `desbloqueio` |
| 2  | Sistema valida elegibilidade (passe ativo, saldo, sem bloqueios |         `acessos`          |
| 3  | Sistema reserva a bicicleta na estação (TTL de 60s)             |         `estacoes`         |
| 4  | Sistema envia comando de abertura à doca                        |       `desbloqueio`        |
| 5  | Doca confirma a abertura e libertação do fecho                  |  `eventosdoca` (receção)   |
| 6  | Sistema abre viagem com carimbo temporal da doca (tsDoca)       |         `viagens`          |
| 7  | Doca confirma o encaixe de devolução numa doca livre            |       `eventosdoca`        |
| 8  | Sistema encerra viagem e atualiza a ocupação da estação         |   `viagens`, `estacoes`    |
| 9  | Sistema calcula o preço (regra municipal) e emite a fatura      |        `faturacao`         |
| 10 | Sistema executa a cobrança (MBWay/Cartão)                       |        `pagamentos`        |
| 11 | Sistema comunica a fatura emitida à Autoridade Tributária       |        `faturacao`         |

### 2.2 Actor/Action dos Restantes Atores
| Ator                         | Ação de Negócio                                                     |           Componente Lógico            |
|:-----------------------------|:--------------------------------------------------------------------|:--------------------------------------:|
| Assinante/Ocasional          | Desbloquear, Devolver, Ver mapa em tempo real, Histórico e faturas  | `desbloqueio`, `estacoes`, `faturacao` |
| Técnico de Manutenção        | Receber ordem, Aceitar intervenção, Registar reparação              |              `manutencao`              |
| Equipa de Redistribuição     | Receber sugestão de rota, Confirmar recolha/entrega de bicicletas   |            `redistribuicao`            |
| Gestor Municipal             | Gerar relatório mensal por freguesia / CO2, Publicar dados abertos  |              `relatorios`              |
| Doca (Sistema)               | Reportar desbloqueio, Encaixe, Ocupação, Reenvio por falha de rede  |             `eventosdoca`              |
| Bicicleta Elétrica (Sistema) | Enviar telemetria (a cada 30 s), Reportar avaria de bateria/travões |        `eventosdoca` -> `frota`        |

## 3. Mapeamento user story - componente

| User Story | Ator      |                                    Componentes Lógicos                                    |
|:-----------|:----------|:-----------------------------------------------------------------------------------------:|
| US-1       | Assinante | `acessos`, `desbloqueio`, `estacoes`, `eventosdoca`, `viagens`, `faturacao`, `pagamentos` |
| US-2       | Técnico   |            `eventosdoca`, `frota`, `manutencao`, `acessos` (para autenticação)            |
| US-3       | Doca      |                           `eventosdoca`, `viagens`, `estacoes`                            |

## 4. Análise de coesão

| Componente       | Nível              | Justificação                                                                                                                    |
|------------------|--------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `comum`          | (1) Funcional      | Contém eventos base e value objects, de modo a que os elementos do sistema possuam um vocabulário partilhado.                   |
| `acessos`        | (3) Comunicacional | Agrega os mesmos dados dos utilizadores (conta, passe, histórico, ...). Todas as operações são sobre o mesmo conjunto de dados. |                      
| `estacoes`       | (1) Funcional      | Gere a topologia, ocupação e reserva de docas em estações de bicicletas.                                                        |                        
| `frota`          | (1) Funcional      | Monitoriza o estado de uma bicicleta individual (bateria, avarias, localização).                                                |                        
| `eventosdoca`    | (1) Funcional      | Recebe, reduplica e entrega eventos enviados pelas docas (sem os interpretar).                                                  |                        
| `desbloqueio`    | (1) Funcional      | Orquestra o desbloqueio de bicicletas: validação, reserva e comando para ser destrancada.                                       |                        
| `viagens`        | (1) Funcional      | Gere o ciclo de vida de uma viagem (iniciar, acompanhar, fechar).                                                               |                        
| `faturacao`      | (2) Sequencial     | Trabalha com os dados de faturação em sequência. Obtém o preço, cria a fatura e envia-a à Autoridade Tributária.                |                        
| `pagamentos`     | (1) Funcional      | Cobrança de viagens independentes.                                                                                              |                        
| `manutencao`     | (1) Funcional      | Gere o ciclo de vida das ordens de manutenção atribuídas a técnicos.                                                            |                        
| `redistribuicao` | (1) Funcional      | Gere sugestões de movimento para as carrinhas para a recolha de bicicletas.                                                     |                        
| `relatorios`     | (1) Funcional      | Gerar relatórios estatísticos para a Câmara Municipal.                                                                          |                        

## 5. Acoplamento aferente, eferente e instabilidade por componente

A tabela relativa ao acoplamento aferente, eferente e instabilidade não conta com a componente `comum`, pois todas as outras dependem dela e esta é estável (I = 0.00).

| Componente       | CE                                              | CA                                                           | Instabilidade $$Ce/(Ca+Ce)$$ | Análise da Instabilidade |
|------------------|-------------------------------------------------|--------------------------------------------------------------|------------------------------|--------------------------|
| `acessos`        | 0                                               | 2 (`desbloqueio`, `faturacao`)                               | 0                            | Estabilidade máxima      |
| `estacoes`       | 1 (`eventosdoca`)                               | 4 (`desbloqueio`, `viagens`, `redistribuicao`, `relatorios`) | 0.20                         | Altamente estável        |
| `frota`          | 1 (`eventosdoca`)                               | 3 (`desbloqueio`, `manutencao`, `relatorios`)                | 0.25                         | Altamente estável        |
| `eventosdoca`    | 0                                               | 4 (`viagens`, `estacoes`, `frota`, `manutencao`)             | 0                            | Estabilidade máxima      |
| `desbloqueio`    | 3 (`acessos`, `estacoes`, `frota`)              | 0                                                            | 1                            | Instável                 |
| `viagens`        | 2 (`estacoes`, `eventosdoca`)                   | 2 (`faturacao`, `relatorios`)                                | 0.5                          | Equilibrado              |
| `faturacao`      | 3 (`viagens`, `acessos`, `pagamentos`)          | 1 (`relatorios`)                                             | 0.75                         | Moderadamente instável   |
| `pagamentos`     | 0                                               | 1 (`faturacao`)                                              | 0                            | Estabilidade máxima      |
| `manutencao`     | 2 (`frota`, `eventosdoca`)                      | 0                                                            | 1                            | Instável                 |
| `redistribuicao` | 1 (`estacoes`)                                  | 0                                                            | 1                            | Instável                 |
| `relatorios`     | 4 (`faturacao`, `viagens`, `estacoes`, `frota`) | 0                                                            | 1                            | Instável                 |

## 6. Conascência entre componentes

## 7. Exemplos de acoplamento temporal