# Arquitetura

## Limites

Aplicacao backend Java 21 com Spring Boot, organizacao em camadas controller / DTO / service / repository / model, persistencia MySQL por JPA/Hibernate e migrations Flyway. Nenhuma interface frontend ou componente de IA faz parte do produto.

## Invariante de inventario

`events.available_tickets` e o saldo transacional autoritativo. O fluxo de reservar, cancelar e expirar primeiro bloqueia a linha do evento em uma transacao (`PESSIMISTIC_WRITE` / `SELECT FOR UPDATE`), atualiza o saldo e persiste o estado da reserva antes de confirmar. Cada instancia usa o mesmo MySQL, portanto o bloqueio nao depende de memoria local. As restricoes/checks do banco oferecem uma segunda barreira contra saldos negativos ou acima da capacidade.

Isso evita oversell entre instancias e processos. A contrapartida e serializacao por evento: eventos independentes podem progredir em paralelo, mas uma flash sale de um unico evento compartilha um ponto de contencao. Alternativas de contador atomico condicional ou particionamento de inventario podem ser avaliadas com testes de carga antes de substituir o modelo.

## Idempotencia

Toda criacao de reserva exige `Idempotency-Key`. O banco aplica unicidade por `(event_id, idempotency_key)` e guarda hash SHA-256 do payload normalizado. Uma repeticao de mesma chave/payload retorna a reserva criada; payload divergente recebe conflito. O bloqueio do evento serializa as verificacoes concorrentes; a constraint unica protege tambem contra erros futuros no fluxo.

## Expiracao e consistencia

Reservas pendentes retêm inventario ate `expires_at`. Uma tarefa agendada consulta eventos com pendencias vencidas e chama um fluxo transacional que bloqueia o evento, muda o estado e devolve ingressos uma unica vez. Todas as instancias podem executar a tarefa: o lock compartilhado e a transicao condicional do estado tornam a operacao idempotente entre workers.

A disponibilidade e lida da tabela do evento; nao ha cache distribuido. Como o worker percorre expiracoes em intervalos configuraveis, a visibilidade da capacidade liberada pode atrasar ate o proximo ciclo. `GET /reservations/{id}` e uma leitura com efeito de materializar uma expiracao vencida. Uma outbox e uma projecao/cache eventual sao evolucoes possiveis se a carga de leitura justificar, sem tornarem-se fonte da verdade do inventario.

## Transacoes e erros

Operacoes em saldo e reserva sao atomicas. Datas sao armazenadas e comparadas em UTC. `ApiExceptionHandler` converte validacao e erros de dominio em respostas HTTP explicitas com `code` e `message`; erros inesperados nao sao convertidos em sucesso.

## Operacao e evolucao

Docker Compose inicia MySQL e uma instancia da API; mais replicas do mesmo servico compartilham o banco e preservam a protecao de concorrencia. Aumentar replicas melhora throughput de requests em eventos distintos, nao paralelismo de escrita para um evento sob o lock por linha. Proximos passos possiveis: teste concorrente em MySQL real, benchmark com perfil de flash sale, metricas para fila/latencia de locks, outbox para eventos, retencao de chaves idempotentes e fluxo de pagamento/confirmacao.
