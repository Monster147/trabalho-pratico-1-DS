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
| Equipa de Redestribuição     | Receber sugestão de rota, Confirmar recolha/entrega de bicicletas   |            `redestribuicao`            |
| Gestor Municipal             | Gerar relatório mensal por freguesia / CO2, Publicar dados abertos  |              `relatorios`              |
| Doca (Sistema)               | Reportar desbloqueio, Encaixe, Ocupação, Reenvio por falha de rede  |             `eventosdoca`              |
| Bicicleta Elétrica (Sistema) | Enviar telemetria (a cada 30 s), Reportar avaria de bateria/travões |        `eventosdoca` -> `frota`        |

## Mapeamento user story -> componente

| User story | Ator      |                                        Componentes                                        |
|:-----------|:----------|:-----------------------------------------------------------------------------------------:|
| US-1       | Assinante | `acessos`, `desbloqueio`, `estacoes`, `eventosdoca`, `viagens`, `faturacao`, `pagamentos` |
| US-2       | Técnico   |                                             ``                                             |
| US-3       | Doca      |                           `eventosdoca`, `viagens`, `estacoes`                            |
