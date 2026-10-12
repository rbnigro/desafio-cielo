# Observabilidade - Flash Booking

## Objetivo

Fornecer evidencias sobre disponibilidade do servico e da capacidade de reserva sob carga. Este documento descreve a direcao de evolucao; metricas nao listadas como implementadas nao devem ser tratadas como existentes.

## Sinais recomendados

- logs estruturados com `trace_id`, endpoint, resultado e duracao; nunca gravar chaves idempotentes completas ou credenciais;
- contagem de reservas aceitas, negadas por capacidade, canceladas e expiradas;
- duracao e timeout de aquisicao de lock por evento;
- lag entre `expires_at` e transicao efetiva para `EXPIRED`;
- conexoes e saturacao do pool JDBC, erros HTTP e disponibilidade MySQL;
- latencia HTTP p50/p95 durante teste de carga.

## Alertas futuros

Definir limiares depois de medir carga representativa: aumento de conflitos/erros, lock wait, fila de expiracao, conexoes esgotadas ou falhas de banco. Nunca usar metricas de disponibilidade como fonte para decidir se ingressos podem ser vendidos.

## Privacidade e resiliencia

Nao registrar payloads pessoais ou segredos. A API deve continuar disponivel se o coletor de metricas/dashboard estiver fora do ar; telemetria e ferramenta de diagnostico, nao parte da transacao de inventario.
