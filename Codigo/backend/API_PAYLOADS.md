# Contrato da API Flash Booking

Base URL: `http://localhost:8080`. Corpos e respostas usam JSON e timestamps ISO-8601 UTC.

A documentacao interativa esta em `http://localhost:8080/swagger-ui/index.html`; a especificacao OpenAPI em JSON esta em `http://localhost:8080/v3/api-docs`.

## Verificar se a API esta no ar

`GET /health` retorna `200 OK` com `{"status":"UP"}` quando a aplicacao esta respondendo. Este endpoint verifica somente a API, nao a conectividade com o banco de dados.

## Criar evento

`POST /events`

```json
{
  "name": "Festival de Exemplo",
  "startsAt": "2027-06-20T18:00:00Z",
  "capacity": 500
}
```

Retorna `201 Created`, `Location: /events/{id}` e:

```json
{
  "id": "a8a881e3-80b8-4b89-87b8-a8655a890352",
  "name": "Festival de Exemplo",
  "startsAt": "2027-06-20T18:00:00Z",
  "capacity": 500,
  "availableTickets": 500
}
```

`name` e `startsAt` sao obrigatorios; `capacity` deve ser maior que zero.

## Consultar disponibilidade

`GET /events/{id}` retorna os mesmos campos do evento, incluindo `availableTickets`. Evento inexistente retorna `404`.

## Reservar ingressos

`POST /events/{id}/reservations` exige `Idempotency-Key` (1 a 128 caracteres):

```http
Idempotency-Key: checkout-2027-0001
Content-Type: application/json
```

```json
{
  "quantity": 2
}
```

Retorna `201 Created`, `Location: /reservations/{id}` e:

```json
{
  "id": "c2a7ce3b-6965-46d8-bc02-da82242cbbfa",
  "eventId": "a8a881e3-80b8-4b89-87b8-a8655a890352",
  "quantity": 2,
  "status": "PENDING",
  "createdAt": "2026-10-08T16:00:00Z",
  "expiresAt": "2026-10-08T16:15:00Z"
}
```

O TTL padrao e 15 minutos (`RESERVATION_TTL`). Repetir a mesma chave no mesmo evento com o mesmo corpo devolve o mesmo identificador e resultado. Reusar a chave com outro corpo retorna `409 IDEMPOTENCY_KEY_REUSED`. Se nao houver ingressos suficientes, retorna `409 INSUFFICIENT_CAPACITY`.

## Consultar reserva e cancelar

- `GET /reservations/{id}` retorna a reserva. Se o prazo ja venceu, esta operacao marca a reserva como `EXPIRED` e devolve os ingressos a disponibilidade na mesma transacao.
- `DELETE /reservations/{id}` cancela uma reserva pendente e responde `204 No Content`. Cancelar repetidamente e seguro; reservas expiradas nao liberam ingressos novamente.
- Reserva inexistente retorna `404 RESOURCE_NOT_FOUND`.

## Erros

Respostas de erro usam o formato estavel:

```json
{
  "code": "INSUFFICIENT_CAPACITY",
  "message": "Requested quantity exceeds available event capacity"
}
```

| HTTP | Codigo | Causa |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | Corpo, campo ou chave de idempotencia invalida/ausente |
| `404` | `RESOURCE_NOT_FOUND` | Evento ou reserva nao encontrado |
| `409` | `INSUFFICIENT_CAPACITY` | Quantidade solicitada excede o saldo disponivel |
| `409` | `IDEMPOTENCY_KEY_REUSED` | Chave ja usada com payload diferente |

## Verificacao manual resumida

1. `POST /events` com capacidade `1`.
2. `POST /events/{id}/reservations` com quantidade `1` e uma chave.
3. Repetir a requisicao com a mesma chave e verificar o mesmo `id`.
4. Reservar com outra chave: resposta `409`.
5. `GET /reservations/{id}` e `DELETE /reservations/{id}`.
6. `GET /events/{id}` e verificar a capacidade restaurada.
