# ARCHITECTURE.md — Guia de Arquitetura e Especificação para Geração de Código

Especificação da plataforma **Lisboa Pedala** para a IA (ou programadores) gerar o esqueleto em Java 21 com Spring Boot e ArchUnit.

## 1. Visão Geral da Plataforma

A plataforma **Lisboa Pedala** é um sistema de micro-mobilidade de bicicletas elétricas partilhadas. A arquitetura foi desenhada como um **Monólito Modular** (particionado por domínio), garantindo elevado desempenho, facilidade de manutenção e desacoplamento temporal.

### Top 3 Características Arquiteturais Norteadoras:
1. **C1 — Disponibilidade (Operacional/Implícita):** O desbloqueio de bicicletas é decidido 100% localmente na doca física (sem dependência do backend em tempo real). Eventos reenviados após falhas de rede são processados de forma estritamente idempotente.
2. **C2 — Elasticidade (Operacional/Implícita):** A receção de telemetria contínua (~35 msg/s das 1 050 bicicletas) e as consultas de leitura do mapa são isoladas da escrita transacional de viagens e ocupação.
3. **C3 — Configurabilidade (Estrutural/Implícita):** O cálculo do valor das viagens é uma função pura e configurável por município, isolada da lógica legal e fiscal de faturação.

## 2. Estrutura de Pacotes (Tree View)

A implementação segue rigorosamente o particionamento por domínio. Os pacotes de primeiro nível representam os **Domínios de Negócio** e os pacotes de segundo nível representam os **Componentes Lógicos (nós-folha)**.

Cada componente lógico possui um subpacote obrigatório `.api` (interfaces, DTOs e eventos públicos). Tudo o que estiver fora do `.api` é implementação privada do componente.

```text
src/main/java/pt/lisboapedala/
├── comum/               # tipos partilhados (IDs, Montante, EventoBase), sem lógica de negócio
├── acesso/
│   ├── identificacao/   # autenticação e elegibilidade dos utilizadores
│   └── autorizacao/     # emissão de QR assinados e sincronização com as docas
├── estacao/
│   ├── docas/           # receção idempotente e ordenada dos eventos das docas
│   ├── ocupacao/        # escrita transacional da ocupação de cada estação
│   ├── disponibilidade/ # vista de leitura do mapa, em cache
│   └── redistribuicao/  # sugestão de rotas de reequilíbrio
├── viagem/
│   ├── viagens/         # ciclo de vida: abre no desbloqueio, fecha no encaixe
│   └── historico/       # consulta das viagens e faturas do próprio utilizador
├── cobranca/
│   ├── tarifario/       # função pura: preço por município
│   ├── faturacao/       # faturas numeradas, comunicação à AT, arquivo de 10 anos
│   └── pagamentos/      # cobrança assíncrona e retentativas
├── frota/
│   ├── telemetria/      # ingestão a cada 30 s e deteção de avarias
│   └── manutencao/      # ordens de trabalho e estado fora de serviço
└── municipio/
    └── relatorios/      # métricas mensais e CO2 evitado por freguesia
```

## 3. Matriz de Dependências Permetidas (*Allowed-to-Use*)

Cada componente **apenas pode importar ou utilizar** o subpacote `.api` dos componentes listados na sua linha. Qualquer outra importação é uma **violação arquitetural gravíssima**.

| Componente | Dependências Permitidas (`.api`) | ADRs |
| :--- | :--- | :--- |
| `comum` | **Nenhuma** (não importa nenhum outro pacote) | — |
| `identificacao` | `comum` | — |
| `autorizacao` | `identificacao.api`, `manutencao.api`, `docas.api`, `comum` | ADR-001 |
| `docas` | `comum` | ADR-001, ADR-003 |
| `viagens` | `docas.api`, `comum` | ADR-003, ADR-006 |
| `ocupacao` | `docas.api`, `comum` | ADR-003 |
| `disponibilidade` | `ocupacao.api`, `telemetria.api`, `manutencao.api`, `comum` | — |
| `telemetria` | `comum` | ADR-004 |
| `manutencao` | `telemetria.api`, `comum` | ADR-004 |
| `tarifario` | `comum` | ADR-002, ADR-005 |
| `faturacao` | `viagens.api`, `tarifario.api`, `identificacao.api`, `comum` | ADR-005, ADR-006 |
| `pagamentos` | `faturacao.api`, `comum` | ADR-006 |
| `historico` | `viagens.api`, `faturacao.api`, `identificacao.api`, `comum` | — |
| `redistribuicao` | `ocupacao.api`, `comum` | — |
| `relatorios` | `viagens.api`, `ocupacao.api`, `comum` | — |

### Regras de Ocultação de Informação (*Information Hiding*):
1. Um pacote `X` só depende dos pacotes expressamente indicados na tabela.
2. De outro componente, **apenas** se importam classes/interfaces pertencentes ao subpacote `.api`.
3. O pacote `comum` contém apenas VO (*Value Objects*) imutáveis (`Montante`, `DocaId`, `UtilizadorId`) e abstrações de eventos. É estritamente proibido colocar lógica de negócio no `comum`.
4. Ausência absoluta de dependências cíclicas entre pacotes de componentes.
5. Cada componente é dono exclusivo do seu estado. NENHUM componente acede diretamente ao estado de outro.

## 4. Regras do Fluxo Principal (US-1: Desbloquear → Devolver → Cobrar)

### Phase 1: Desbloqueio Local (ADR-001 & ADR-003)
* `autorizacao` emite QR assinados com `utilizadorId|tipoUtilizador|emitidoEm|expiraEm|nonce` e sincroniza com as docas físicas (`K2`) a chave pública, os cartões Navegante elegíveis e as bicicletas fora de serviço.
* A doca autoriza localmente o desbloqueio **sem chamar o backend**. Com cartão Navegante, o utilizador é sempre `ASSINANTE`.
* Ao reestabelecer rede, a doca envia `DesbloqueioEfetuado(docaId, numeroSequencia, utilizadorId, bicicletaId, municipioId, tipoUtilizador, ocorridoEm)`. O id da viagem é `docaId-numeroSequencia`.
* `docas` guarda a chave de idempotência `(docaId, numeroSequencia)` antes de responder com `ACK` e publica o evento ordenado.
* `viagens` consome o evento e abre o registo de viagem. Um reenvio não abre outra viagem.

### Phase 2: Devolução e Unidade de Trabalho Atómica (ADR-003)
* O utilizador encaixa a bicicleta na doca. A doca tranca localmente e envia `BicicletaEncaixada(docaId, numeroSequencia, viagemId, ocorridoEm)`, que `docas` deduplica e publica.
* **Unidade de Trabalho Atómica:** No componente `viagens`, o fecho da viagem e a publicação de `ViagemFechada` ocorrem na **mesma transação**, separada da de `docas`. Se o encaixe chegar antes do desbloqueio, fica pendente até ele chegar.
* `ocupacao` reage ao evento e atualiza o estado da estação.

### Phase 3: Cobrança Assíncrona e Faturação (ADR-002, ADR-005 & ADR-006)
* `faturacao` subscreve assincronamente `ViagemFechada` e emite cada fatura numa **transação própria**, para que `FaturaEmitida` seja publicado dentro de uma transação.
* `faturacao` chama `tarifario.api` (função pura) para calcular o preço segundo as regras do município da viagem, carregadas no arranque de `application.properties` (`lisboapedala.tarifario.municipios.<municipio>.*`).
* `faturacao` obtém NIF de `identificacao.api`, grava a fatura numerada, comunica-a simuladamente à Autoridade Tributária (AT) e publica `FaturaEmitida`.
* `pagamentos` subscreve `FaturaEmitida` e cobra no gateway simulado (MB WAY / Cartão). **Garantia de Desacoplamento (ADR-006):** se o pagamento falhar, regista a dívida e agenda retentativas; **a viagem PERMANECE FECHADA e nunca é reaberta nem cancelada.**

## 5. Convenções de Código

* Java 21, Spring Boot 3, nomes em português. Controladores REST em `.internal.web`, sob `/api/...`.
* DTOs e eventos são `record` imutáveis, usando os VO de `comum` quando existem (`UtilizadorId`, `DocaId`, `MunicipioId`).
* Cada evento é declarado no `.api` de quem o publica (`ApplicationEventPublisher`). Quem o consome usa `@EventListener` ou `@TransactionalEventListener(phase = AFTER_COMMIT)` e é idempotente.
* Estado em memória, dentro de cada componente; `docas` usa um diário em ficheiro (ADR-003). Sistemas externos: interface em `.internal` com implementação simulada.
* Configuração em `application.properties` (`lisboapedala.<componente>.*`). Testes ArchUnit em `src/test/java/pt/lisboapedala/architecture/` (`RegrasAllowedToUseTest`, `ApiPublicaTest`, `ComumTest`, `SemCiclosTest`) e das user stories em `.../us/`; tudo corre com `mvn test`.

## 6. O que a IA não pode fazer

1. Usar classes de outro componente fora do seu `.api`, ou de um componente fora da sua linha na Secção 3.
2. Criar ou alterar código em componentes que não lhe foram pedidos.
3. Alterar um `.api`, ou criar eventos e conectores fora da Secção 4 e dos diagramas de `docs/diagramas/`, sem atualizar esta especificação.
4. Fazer o desbloqueio depender de uma chamada ao backend (ADR-001), ou responder `ACK` antes de guardar a chave (ADR-003).
5. Pôr regras de preço fora de `tarifario` (ADR-002, ADR-005), ou reabrir uma viagem por falha de pagamento (ADR-006).
6. Entregar código que não passe em `mvn test`.
