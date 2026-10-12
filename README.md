# Desafio Cielo

Projeto de backend para reservas temporarias de ingressos em eventos com capacidade limitada. A API foi implementada em Java 21 com Spring Boot e MySQL 8.4.

## Executar localmente

Requisitos: Docker Engine e Docker Compose v2.

No PowerShell, na pasta do projeto:

```powershell
Set-Location Codigo\backend
Copy-Item .env.example .env
docker compose up --build
```

Quando a inicializacao terminar:

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI: <http://localhost:8080/v3/api-docs>
- Health check: <http://localhost:8080/health>

Para encerrar os containers sem apagar os dados do banco:

```powershell
docker compose down
```

O arquivo `.env.example` usa credenciais simples destinadas somente ao desenvolvimento local. Nao use esses valores em ambientes compartilhados ou de producao.

## Funcionalidades da API

| Metodo | Rota | Descricao |
| --- | --- | --- |
| `GET` | `/health` | Verifica se a API esta respondendo |
| `POST` | `/events` | Cria um evento |
| `GET` | `/events/{id}` | Consulta capacidade e disponibilidade |
| `POST` | `/events/{id}/reservations` | Cria uma reserva temporaria; requer `Idempotency-Key` |
| `GET` | `/reservations/{id}` | Consulta uma reserva |
| `DELETE` | `/reservations/{id}` | Cancela uma reserva pendente |

Payloads de exemplo, respostas e codigos de erro estao em [`Codigo/backend/API_PAYLOADS.md`](Codigo/backend/API_PAYLOADS.md).

## Testes

Com Java 21 e Maven instalados, execute na pasta `Codigo\backend`:

```powershell
mvn test
```

Os testes principais rodam com H2; o teste de concorrencia com MySQL real requer Docker.

## Estrutura do repositorio

- [`Codigo/backend`](Codigo/backend): codigo-fonte, configuracao, testes e documentacao da API.
- [`Projeto`](Projeto): agentes, prompts, metodologia e artefatos de arquitetura.
- [`Curso/Docs`](Curso/Docs): materiais e decisoes relacionadas ao projeto.
- [`Avaliacao`](Avaliacao): harness, rubricas e relatorio de baseline.
- [`Case BackEnd.pdf`](Case%20BackEnd.pdf): enunciado do desafio.

Para detalhes de execucao, decisoes tecnicas e trade-offs, consulte o [README do backend](Codigo/backend/README.md).
