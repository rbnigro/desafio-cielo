# ADR-001 - Flash Booking como nucleo do projeto

```yaml
adr_id: ADR-001
status: accepted
created_at: 2026-10-08
author_agent_id: project-owner
reviewer_agent_id: pending
```

## Objetivo

Definir o escopo do produto como um backend de reserva de ingressos em flash sale, substituindo o CRUD academico anterior.

## Contexto

Eventos possuem capacidade limitada e precisam aceitar reservas durante picos concorrentes. O nucleo precisa impedir oversell com varias instancias da API, liberar reservas pendentes expiradas, permitir repeticao idempotente e apresentar disponibilidade com consistencia eventual documentada. A entrega inclui Docker Compose, testes e README, com code review e apresentacao das decisoes.

## Decisao

- Manter Java 21 sem Lombok, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL.
- Expor `POST /events`, `GET /events/{id}`, `POST /events/{id}/reservations`, `GET /reservations/{id}` e `DELETE /reservations/{id}`.
- Usar o banco compartilhado como fonte de verdade do inventario; transacoes/locks e constraints devem proteger a capacidade entre instancias.
- Persistir a chave idempotente e estado da reserva no mesmo banco.
- Automatizar expiracao de reservas pendentes; documentar intervalo de varredura e trade-offs.
- Manter o escopo backend-only; nao incluir frontend nem capacidades de IA nesta entrega.

## Alternativas e trade-offs

Um lock por evento oferece comportamento simples e demonstravel, mas serializa reservas para um evento quente. Contadores atomicos ou particoes de inventario podem elevar throughput e aumentar complexidade. Cache/projecao de disponibilidade pode ser eventualmente consistente, mas nunca autoriza a venda nem substitui saldo transacional.

## Consequencias

O CRUD de pacientes e o frontend do template deixam de definir o produto. O contrato, esquema, testes, Harness, README e rubricas passam a medir somente os requisitos de Flash Booking. Docker Compose e necessario para demonstrar a configuracao MySQL de forma repetivel.

## Revisao

Revisitar este ADR se um teste de carga mostrar que o lock por evento nao atende ao objetivo de desempenho, se uma nova exigencia alterar o modelo de reserva ou se evidencia MySQL contradizer o mecanismo de consistencia escolhido. Registrar entao alternativas, testes e nova decisao.
