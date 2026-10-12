# Rubrica de avaliacao - Flash Booking

## Objetivo e stack preservada

Avaliar o nucleo backend de reservas de ingressos com JDK 21, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL. O escopo nao inclui frontend, IA, pagamento ou autenticacao. Cada criterio recebe `0` (nao demonstrado), `1` (parcial) ou `2` (demonstrado por evidencia reproduzivel).

## Criterios

| ID | Criterio | Evidencia minima | Bloqueador |
| --- | --- | --- | --- |
| FB-01 | Contrato REST | Cinco rotas documentadas e testadas; validacao do payload e respostas HTTP | Rota obrigatoria ausente ou sem teste |
| FB-02 | Capacidade e oversell | Teste concorrente com capacidade finita; saldo nunca negativo nem acima da capacidade | Reserva aceita alem da capacidade |
| FB-03 | Multiplas instancias | Reserva e cancelamento coordenados por lock/transacao no armazenamento compartilhado, sem estado local como fonte da verdade | Correcao depende de memoria local |
| FB-04 | Idempotencia | Chave persistida com unicidade; repeticao igual devolve o mesmo resultado; payload diferente gera conflito | Duplicacao de reserva para repeticao |
| FB-05 | Expiracao e cancelamento | Estado terminal persistido, liberacao unica do inventario e testes de repeticao/race | Inventario liberado duas vezes ou hold sem expiracao |
| FB-06 | Persistencia | Schema/migrations MySQL, integridade referencial e execucao reproduzivel | Schema ausente ou incompatibilidade nao detectada |
| FB-07 | Erros | Codigos HTTP e corpo de erro explicitos para validacao, recurso ausente, conflito e capacidade | Erro de dominio vira sucesso ou 500 sem motivo |
| FB-08 | Entrega | README, decisoes/trade-offs, evolucoes futuras e Docker Compose | Aplicacao nao inicializa conforme instrucoes |
| FB-09 | Qualidade e seguranca | Maven tests, Harness, ausencia de segredos e revisao independente | Segredo versionado, testes ausentes ou autor aprova o proprio codigo |

## Decisao

Um bloqueador resulta em `blocked`, independentemente da soma. Sem bloqueadores, publicar a soma de `0..18` e a lista de ressalvas. O Harness registra evidencias automatizaveis; integracao MySQL e revisao de code review devem ser identificadas separadamente, sem inferir sua conclusao a partir de teste H2.

## Registro

```yaml
project: flash-booking
score: 0
criteria:
  - id: FB-02
    score: 0
    evidence: Avaliacao/reports/baseline.json
blockers: []
decision: blocked
reviewer_agent_id: evaluator-01
```
