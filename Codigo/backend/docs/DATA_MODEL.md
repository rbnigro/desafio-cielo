# Modelo de dados

## `events`

| Coluna | Tipo/logica | Regra |
| --- | --- | --- |
| `id` | UUID | Chave primaria |
| `name` | texto ate 200 | Obrigatorio |
| `starts_at` | instante UTC | Obrigatorio |
| `capacity` | inteiro | Maior que zero, imutavel no fluxo atual |
| `available_tickets` | inteiro | Entre zero e `capacity`; inclui lugares nao retidos por reservas pendentes |
| `created_at` | instante UTC | Criacao |

## `reservations`

| Coluna | Tipo/logica | Regra |
| --- | --- | --- |
| `id` | UUID | Chave primaria |
| `event_id` | UUID | FK para `events` |
| `quantity` | inteiro | Maior que zero |
| `status` | enum textual | `PENDING`, `CANCELLED` ou `EXPIRED` |
| `created_at`, `expires_at` | instantes UTC | `expires_at` define o TTL do hold |
| `idempotency_key` | texto ate 128 | Unica dentro do evento |
| `request_hash` | SHA-256 hexadecimal | Detecta reuso da chave com corpo diferente |

Indices: unicidade `(event_id, idempotency_key)` para deduplicacao e `(status, expires_at, event_id)` para varredura de expiracoes.

## Estados e saldo

Criar reserva muda saldo e estado para `PENDING` atomicamente. Cancelar uma reserva pendente muda para `CANCELLED` e devolve sua quantidade uma vez. Expirar muda para `EXPIRED` e tambem devolve uma vez. Cancelar novamente ou processar novamente uma expiracao nao altera o saldo. A tabela de reservas permanece como registro de idempotencia e auditoria; politica de retencao nao faz parte do escopo atual.
