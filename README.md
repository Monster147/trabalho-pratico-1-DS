# Lisboa Pedala

Especificação de arquitetura e esqueleto funcional da plataforma de bicicletas partilhadas Lisboa Pedala (Trabalho Prático 1 de Design de Software).

O sistema é um monólito modular em Java 21 com Spring Boot, particionado por domínio. A especificação para geração de código está em [`ARCHITECTURE.md`](ARCHITECTURE.md), e a restante documentação em [`docs/`](docs/).

## Estrutura do repositório

| Caminho | Conteúdo |
| :--- | :--- |
| `ARCHITECTURE.md` | Especificação lida pela IA antes de gerar código |
| `docs/` | Características, componentes, particionamento e vistas, reflexão |
| `docs/adr/` | Registos de decisões de arquitetura (ADR-001 a ADR-006) |
| `docs/diagramas/` | Diagramas C4 (níveis 1 a 3), vista de módulos e vista C&C, em PlantUML |
| `src/main/java/pt/lisboapedala/` | Código, um pacote por domínio e um subpacote por componente |
| `src/test/java/pt/lisboapedala/architecture/` | Fitness functions (ArchUnit) |
| `prompts/` | Registo do uso de IA, um ficheiro por componente |
| `CODEOWNERS` | Responsável por cada componente (vista de alocação) |

As fitness functions estão em `src/test/java/pt/lisboapedala/architecture/` e não em `tests/architecture/`, porque é aí que o Maven procura os testes num projeto Java.

## Como correr

Requisitos: Java 21 e Maven.

```bash
mvn test              # fitness functions (ArchUnit) e restantes testes, num único comando
mvn spring-boot:run   # arranca a aplicação (API REST)
```

## Uso de IA

| Ferramenta | Modelo          | Utilização |
| :--- |:----------------| :--- |
| Claude (Anthropic), via claude.ai | Claude Opus 5.5 | Geração de código e testes |
| GitHub Copilot | ---             | Geração de código |

Cada pedido de geração, os ficheiros de contexto fornecidos, o resultado e as violações de arquitetura detetadas estão registados em [`prompts/`](prompts/).