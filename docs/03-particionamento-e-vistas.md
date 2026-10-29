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