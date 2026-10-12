# Entregaveis do projeto Flash Booking

O produto e o nucleo backend de reservas temporarias de ingressos, em Java 21/Spring Boot/JPA/MySQL. Frontend e IA nao fazem parte do escopo.

## Entregaveis obrigatorios

| Item | Conteudo minimo | Evidencia |
| --- | --- | --- |
| API | Cinco rotas, validacoes, idempotencia e erros explicitos | `Codigo/backend/API_PAYLOADS.md` e testes |
| Dominio | Eventos, capacidade e estado de reservas | modelo e migration SQL |
| Concorrencia | Sem oversell entre instancias e operacoes atomicas | testes concorrentes e decisao arquitetural |
| Expiracao | Reserva pendente vence e devolve inventario uma unica vez | teste de expiracao e agendador |
| Persistencia | MySQL e migrations versionadas | Flyway e Docker Compose |
| Execucao | Instrucoes reproduziveis com configuracao local | `Codigo/backend/README.md` |
| Qualidade | Testes automatizados, Harness e revisao independente | Maven, `Avaliacao/` e parecer |
| Decisoes | Alternativas, trade-offs e evolucoes futuras | arquitetura e ADR |

## Relatorio tecnico

Documentar:

1. problema, usuarios e premissas;
2. fronteiras e camadas;
3. contrato REST e modelo de dados;
4. protecao de inventario sob concorrencia;
5. idempotencia, expiracao, cancelamento e erros;
6. testes e banco utilizado em cada teste;
7. operacao via Compose;
8. riscos, trade-offs e evolucoes futuras.

Nao afirmar que MySQL real foi testado quando a evidencia veio de H2.

## Demonstracao

1. iniciar API e MySQL com Docker Compose;
2. criar evento de capacidade pequena;
3. reservar ingressos e repetir a mesma chave idempotente;
4. demonstrar recusa ao exceder a capacidade;
5. consultar e cancelar uma reserva, confirmando o saldo;
6. deixar uma reserva expirar e confirmar a liberacao;
7. executar teste concorrente, testes Maven e Harness;
8. apresentar decisao arquitetural e revisao independente.

## Criterio de aceite

Uma pessoa externa consegue subir os servicos, seguir o contrato, reproduzir testes, entender a estrategia anti-oversell e examinar resultados e riscos. A rubrica `Avaliacao/rubrics/stage-rubrics.md` separa aprovacao automatizada, validacao MySQL e code review.
