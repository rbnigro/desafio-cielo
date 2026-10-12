# Diretrizes do projeto Flash Booking

## Objetivo e tecnologias

Construir e avaliar o backend de reserva de ingressos para eventos de capacidade limitada, em modelo flash sale. A stack mantida e Java 21 sem Lombok, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL. Docker Compose, testes automatizados e README sao obrigatorios. Nao ha frontend neste escopo.

## Requisitos vinculantes

- `POST /events`: criar evento.
- `GET /events/{id}`: consultar evento e disponibilidade.
- `POST /events/{id}/reservations`: reter ingressos com chave de idempotencia.
- `GET /reservations/{id}`: consultar o estado da reserva.
- `DELETE /reservations/{id}`: cancelar reserva pendente.
- Suportar varias instancias da API; MySQL, nao memoria local, coordena as escritas.
- Nunca permitir oversell; estados de reserva, saldo e expiracao devem ser transacionais.
- Expirar reservas pendentes automaticamente; tornar erros explicitos e documentar atraso/consistencia da disponibilidade.

## Responsabilidades

| Papel | Responsabilidade |
| --- | --- |
| Architect | Especificar invariantes, contrato, modelo de dados, riscos e alternativas. |
| Developer | Implementar em Java 21/Spring/MySQL, adicionar testes e documentar alteracoes. |
| Code Reviewer | Revisar independentemente concorrencia, idempotencia, transacoes, validacao e seguranca. |
| Evaluator | Executar Harness e testes, registrar somente evidencias observadas e aplicar a rubrica Flash Booking. |
| Security Reviewer | Avaliar configuracao, segredos, validacao e riscos do backend. |

## Fluxo de mudanca

1. Relacionar mudancas a requisitos e criterios `FB-01` a `FB-09` da rubrica.
2. Manter controller / DTO / service / repository / model; registrar mudancas arquiteturais em ADR.
3. Implementar testes junto da mudanca, incluindo comportamento de concorrencia quando inventario for afetado.
4. Executar `mvn test` e `python Avaliacao/harness/run_harness.py`.
5. Registrar limitacoes e separar testes H2 de evidencia MySQL real.
6. Obter revisao independente antes de declarar aprovada a entrega.

## Regras permanentes

- Nao usar Lombok.
- Nao aprovar o proprio codigo nem apresentar testes nao executados como aprovados.
- Nao adicionar capacidades de frontend, IA, pagamento ou autenticacao sem mudanca explicita de escopo.
- Nunca versionar segredos; usar variaveis de ambiente e placeholders locais documentados.
