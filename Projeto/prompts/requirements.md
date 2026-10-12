# Levantamento de requisitos - Flash Booking

## Objetivo

Definir requisitos verificaveis para reserva de ingressos de eventos com capacidade limitada, evitando assumir comportamento nao declarado.

## Escopo funcional

1. `POST /events` cria evento.
2. `GET /events/{id}` consulta evento e disponibilidade.
3. `POST /events/{id}/reservations` reserva ingressos temporariamente.
4. `GET /reservations/{id}` consulta reserva.
5. `DELETE /reservations/{id}` cancela reserva.

## Requisitos transversais

Documente como o banco compartilhado coordena multiplas instancias, como oversell e impedido, como reservas pendentes expiram, qual e o contrato de idempotencia, qual atraso e aceitavel para disponibilidade, quais erros sao retornados e como o fluxo e testado.

Nao inclua frontend, IA, pagamento ou autenticacao sem mudanca de escopo aprovada. Nao invente regras de negocio ausentes; registre premissas explicitamente para code review.

## Saida obrigatoria

```yaml
requirement_id: FB-01
statement: comportamento observavel
priority: must
acceptance_criteria:
  - criterio verificavel
evidence: nome-do-teste-ou-relatorio
```
