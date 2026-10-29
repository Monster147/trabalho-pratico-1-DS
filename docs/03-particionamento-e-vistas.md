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