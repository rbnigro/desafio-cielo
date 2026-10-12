# Visao da implementacao

## Escopo

Servico backend Flash Booking em Java 21 e Spring Boot. Persistencia MySQL com JPA/Hibernate e schema versionado por Flyway. Contratos REST e respostas de erro estao em `API_PAYLOADS.md`.

## Camadas

- `controller`: endpoints de eventos e reservas e mapeamento HTTP.
- `dto`: modelos validados de entrada e saida.
- `service`: regras transacionais de capacidade, idempotencia, cancelamento e expiracao.
- `repository`: consultas JPA e lock pessimista por evento.
- `model`: eventos, reservas e estados persistidos.

## Persistencia e concorrencia

`events.available_tickets` e atualizado na mesma transacao que a reserva correspondente. Toda escrita de inventario bloqueia a linha do evento no MySQL; chaves de idempotencia e limites de dominio tambem possuem constraints no schema. Flyway aplica `src/main/resources/db/migration/V1__create_events_and_reservations.sql`; Hibernate valida o mapeamento sem alterar o schema.

## Expiracao

`ReservationExpiryJob` varre expiracoes vencidas a cada segundo por padrao. A mesma rotina e acionada durante criacao de reserva e consulta de reserva; estado e disponibilidade mudam atomicamente. Parametros podem ser configurados por `RESERVATION_TTL` e `EXPIRATION_SWEEP_INTERVAL_MS`.

## Execucao e validacao

- Execucao integrada: `docker compose up --build` (configurar `.env` conforme `.env.example`).
- Testes: `mvn test`; testes de contrato/concorrencia usam H2 e o teste MySQL Testcontainers executa quando Docker estiver disponivel.
- O Harness independente fica em `Avaliacao/harness/run_harness.py`; sua execucao e registrada em `Avaliacao/reports/baseline.json`.
- Se Docker nao estiver disponivel, a integracao MySQL e carga de flash sale permanecem validacoes adicionais recomendadas antes de uso produtivo.
