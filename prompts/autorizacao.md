# Registo de transparência — autorizacao

## Pedido e Contexto

Pedido exato: “Atua como Engenheiro de Software Sénior Java/Spring Boot. Com base nas especificações do ficheiro `ARCHITECTURE.md` (em anexo/fornecido), gera a estrutura e o código funcional para os domínios `acesso` e `estacao` sob o pacote base `pt.lisboapedala`.”

Segundo pedido, na mesma conversa: “Volta a executar a minha prompt mas sobre este código novo”, sem entrar em conflito com o código já feito pelo colega.

Contexto fornecido: `ARCHITECTURE.md` e `codigo.zip` (código já implementado pelo grupo).

## Resultado e Justificação

A primeira versão foi rejeitada, porque não seguia as convenções do código que o grupo já tinha. A segunda versão foi aceite pelo programador.

Emite QR assinados com Ed25519, validáveis pela doca só com a chave pública, e sincroniza periodicamente com as docas a chave pública, os cartões Navegante elegíveis e as bicicletas fora de serviço (ADR-001). Pendente: `manutencao.api` é um contrato provisório, a trocar pelo real quando o componente existir.

## Deteção de Violações de Arquitetura

**Caso 1 — Sincronização K2 feita por outro caminho.** No C4 e na vista C&C, `autorizacao` envia os dados de sincronização diretamente às docas físicas (`Auth -> Doca`, K2). O código gerado entrega o pacote a `docas.api`, e são as docas que o vão buscar a `GET /api/docas/{docaId}/sincronizacao`.

- **Detetado por:** revisão humana.
- **Porque não foi a fitness function:** a dependência `autorizacao → docas.api` é permitida pela matriz. O ArchUnit verifica dependências entre pacotes, não conectores.

Fora este caso, as dependências respeitam a matriz (ArchUnit sem violações).
