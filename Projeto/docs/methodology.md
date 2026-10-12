# Metodologia de desenvolvimento - Flash Booking

## Objetivo

Conduzir a implementacao do backend de reservas desde os requisitos ate uma entrega reproduzivel e defensavel em code review. A prioridade e preservar invariantes de inventario, apresentar evidencia de concorrencia e documentar limites e trade-offs.

## Escopo tecnologico

JDK 21 sem Lombok, Spring Boot, arquitetura controller / DTO / service / repository / model, JPA/Hibernate, MySQL, Flyway, Docker Compose e testes automatizados. O produto atual e backend-only; frontend, IA, pagamento e autenticacao estao fora do desafio.

## Requisitos que guiam o trabalho

- `POST /events` cria evento de capacidade finita.
- `GET /events/{id}` expoe capacidade e disponibilidade.
- `POST /events/{id}/reservations` retira temporariamente uma quantidade disponivel, com idempotencia obrigatoria.
- `GET /reservations/{id}` consulta reserva e estado.
- `DELETE /reservations/{id}` cancela uma reserva pendente.
- Instancias concorrentes compartilham a mesma fonte transacional de inventario; oversell nunca e aceitavel.
- Reservas pendentes expiram automaticamente, e a liberacao de ingressos so ocorre uma vez.
- Erros de validacao, ausencia de recurso, capacidade e chave repetida sao explicitos.
- README, Docker Compose e testes acompanham a entrega.

## Ciclo de mudanca

```text
Requisito -> contrato e modelo -> decisao arquitetural -> implementacao -> testes -> Harness -> revisao independente -> entrega
```

Antes de alterar inventario, identificar o efeito transacional e adicionar cobertura de repeticao, cancelamento, expiracao e corrida concorrente. Uma alteracao nao esta pronta apenas porque compila.

## Gates e evidencias

| Gate | Evidencia |
| --- | --- |
| Contrato | `Codigo/backend/API_PAYLOADS.md` e testes MVC para as cinco rotas |
| Arquitetura | `Codigo/backend/docs/ARCHITECTURE.md`, modelo de dados e ADRs |
| Integridade | testes de capacidade concorrente, chave idempotente, expiracao e cancelamento |
| Persistencia | migrations Flyway e inicializacao MySQL via Compose |
| Entrega | README e `Avaliacao/harness/run_harness.py` |
| Aprovacao | rubrica `Avaliacao/rubrics/stage-rubrics.md` e code review independente |

Teste H2 facilita execucao automatizada local, mas nao comprova sozinho semantica de lock ou migration em MySQL. Relatorios devem indicar separadamente qual banco foi usado e se a revisao independente foi concluida.

## Governanca e qualidade

- Vincular mudancas a criterios `FB-01` a `FB-09`.
- Registrar novas decisoes arquiteturais em ADR com alternativas e consequencias.
- Preservar Java 21 e evitar Lombok.
- Nao atribuir resultados que nao foram executados; reportar falhas e ressalvas.
- Nao versionar credenciais; usar variaveis de ambiente e arquivo `.env` local ignorado.
- Solicitar code review por identidade diferente do autor antes de declarar a entrega aprovada.

## Trade-offs que a apresentacao deve cobrir

- Lock de linha por evento: simples e seguro em multiplas instancias, mas serializa escrita para um evento quente.
- Expiracao por polling: facil de operar e idempotente via banco, mas libera disponibilidade com atraso limitado pelo intervalo de varredura.
- MySQL como fonte de verdade: evita inconsistencias de cache na decisao de venda; leitura futura pode usar projecao eventual desde que nunca autorize oversell.
- Retencao de chaves idempotentes: replay permanece deterministico enquanto a reserva for mantida; politica de limpeza precisa ser definida antes de producao.
