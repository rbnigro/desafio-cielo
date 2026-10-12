# Constituicao de engenharia - Flash Booking

## Proposito

Esta constituicao define os limites de escopo, qualidade e evidencia para o backend Flash Booking: reservas temporarias de ingressos para eventos com capacidade limitada e picos de acesso.

## Stack e escopo

- Java 21, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL.
- Docker Compose, testes automatizados e README reproduzivel.
- API composta pelas cinco rotas definidas em `Codigo/backend/API_PAYLOADS.md`.
- Frontend, IA, pagamento e autenticacao nao fazem parte da entrega atual.

## Invariantes

1. O saldo de ingressos nao pode ficar negativo ou ultrapassar a capacidade do evento.
2. A protecao contra oversell deve funcionar entre processos e instancias e, portanto, ser garantida por transacoes/constraints no armazenamento compartilhado.
3. Criar reserva, reservar inventario, cancelar e expirar devem atualizar estado e saldo atomicamente.
4. A chave de idempotencia persiste no banco e replays iguais nao criam outra reserva.
5. Falhas de validacao e dominio tem status e resposta explicitos; falha nao pode ser mascarada como sucesso.
6. Disponibilidade pode ter consistencia eventual documentada, mas nao e fonte de verdade para aceitar uma venda.

## Evidencia e governanca

- Cada criterio da rubrica Flash Booking deve apontar para teste, migration, documentacao ou resultado do Harness.
- Diferenciar evidencia local/H2 de execucao contra MySQL real.
- Revisao de codigo e independente de autoria; autor nao aprova seu proprio trabalho.
- Nenhuma credencial ou segredo real pode aparecer em codigo, exemplos ou relatorios.
- Uma entrega nao e aprovada se testes obrigatorios falharem, houver risco de oversell ou estiver ausente uma revisao exigida.

## Processo

Manter camadas, registrar mudancas arquiteturais em ADRs, rodar testes e Harness e documentar trade-offs, limites conhecidos e evolucoes futuras antes da publicacao.
