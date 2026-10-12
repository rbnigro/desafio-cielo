# Flash Booking

Backend de reservas temporarias de ingressos para eventos de capacidade limitada. A API usa Java 21, Spring Boot, JPA/Hibernate e MySQL 8.4. Nao ha aplicacao frontend neste escopo.

## Requisitos locais

- Docker Engine com Docker Compose v2; ou Java 21, Maven e MySQL 8.4.
- Para desenvolvimento local, `.env.example` configura usuario e senha `root`. Essas credenciais sao inseguras: use-as somente na maquina local, nunca em ambientes compartilhados ou de producao, e nao versione `.env`.

## Iniciar com Docker Compose

Na pasta `Codigo/backend`:

```powershell
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://127.0.0.1:8080/health
```

A primeira inicializacao pode levar alguns minutos enquanto as imagens e dependencias sao baixadas e a aplicacao inicia. Quando estiver pronta, o health check retorna `status: UP`. A API fica disponivel em `http://127.0.0.1:8080`; o Flyway cria o schema MySQL na primeira inicializacao.

Use `127.0.0.1` em vez de `localhost` se o cliente tentar conectar por IPv6 (`::1`). O MySQL e acessivel pela API na rede interna do Compose, mas a porta `3306` nao e publicada no host. Para conferir os logs da API:

```powershell
docker compose logs -f api
```

Para encerrar sem apagar os dados:

```powershell
docker compose down
```

Para apagar tambem o volume local do banco e todos os dados nele, use `docker compose down --volumes`.

## Executar sem containers

Com um schema MySQL `flash_booking` e o usuario local `root` com permissao de DDL/DML:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/flash_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
mvn spring-boot:run
```

Flyway aplica as migrations e Hibernate valida o mapeamento; a aplicacao nao depende de `ddl-auto=update`.

## Testes

```powershell
mvn test
```

Os testes de contrato e concorrencia usam H2 em modo MySQL para rodar sem um servidor local. Um teste adicional com Testcontainers executa migration e corrida concorrente em MySQL 8.4 quando Docker estiver disponivel; sem Docker, esse teste e marcado como ignorado. O ambiente Compose executa MySQL 8.4.

## API

Contrato completo, exemplos JSON e codigos de erro: [`API_PAYLOADS.md`](./API_PAYLOADS.md). Com a aplicacao em execucao, explore e teste os endpoints em `http://127.0.0.1:8080/swagger-ui/index.html`; o documento OpenAPI JSON esta em `http://127.0.0.1:8080/v3/api-docs`.

| Metodo | Rota | Resultado |
| --- | --- | --- |
| `GET` | `/health` | Verifica se a API esta respondendo; `200 OK` com `{"status":"UP"}` |
| `POST` | `/events` | Cria evento; `201 Created` |
| `GET` | `/events/{id}` | Consulta capacidade/disponibilidade; `200 OK` |
| `POST` | `/events/{id}/reservations` | Reserva quantidade; exige `Idempotency-Key` |
| `GET` | `/reservations/{id}` | Consulta reserva e atualiza expiracao, se devida |
| `DELETE` | `/reservations/{id}` | Cancela reserva pendente; `204 No Content` |

## Decisoes, trade-offs e evolucoes

- Todas as mutacoes de inventario bloqueiam a linha do evento com `SELECT ... FOR UPDATE`. A alteracao da disponibilidade e o estado da reserva pertencem a mesma transacao MySQL, inclusive entre instancias da API. Isso prioriza correccao; reservas simultaneas para um mesmo evento ficam serializadas.
- `Idempotency-Key` e unica por evento no banco e ligada a um hash do payload. Repeticoes iguais retornam a reserva original; reutilizacao com outro payload retorna `409`.
- Reservas ficam `PENDING` durante 15 minutos por padrao. Um agendador em cada instancia procura expiracoes; o lock por evento faz as instancias cooperarem sem liberar o mesmo inventario duas vezes. A leitura da disponibilidade pode refletir uma expiracao com atraso de ate um ciclo do agendador (1 segundo por padrao).
- O banco e a fonte de verdade da capacidade; disponibilidade nao depende de cache eventual. Futuramente, uma projecao/cache pode ser atualizada por eventos de dominio/outbox caso a escala justifique consistencia eventual para leitura.
- A chave de idempotencia e mantida junto da reserva sem politica automatica de retencao; limpeza futura deve preservar a janela contratual de repeticao. Pagamento, confirmacao, autenticacao, rate limiting e mensageria estao fora deste desafio.

Consulte [`IMPLEMENTATION.md`](./IMPLEMENTATION.md), [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) e [`docs/DATA_MODEL.md`](./docs/DATA_MODEL.md) para detalhes.
