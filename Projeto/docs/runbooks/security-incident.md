# Runbook de incidente - Flash Booking

## Quando usar

Use este procedimento para credencial exposta, acesso indevido, abuso de API, erro de saldo, violacao de idempotencia ou indisponibilidade do MySQL.

## Contencao

1. Registre horario UTC, versao implantada, request/trace ID e sintoma sem copiar segredo ou PII.
2. Se houver risco de sobre-venda ou inconsistencia, interrompa temporariamente a criacao de reservas.
3. Restrinja acesso ao servico e revogue/rotacione credenciais comprometidas.
4. Preserve logs, estado do banco e identificadores de reservas para investigacao.

## Diagnostico

- compare capacidade, saldo e reservas `PENDING`, `CANCELLED` e `EXPIRED`;
- verifique transacoes, lock waits, deadlocks e reinicios das instancias;
- procure repeticoes de `Idempotency-Key` sem registrar seu valor completo;
- confirme lag do job de expiracao e estado de migrations;
- separe falha de API, banco e ambiente antes de corrigir.

## Recuperacao

1. Corrija a causa e adicione teste de regressao.
2. Valide invariantes e reconcilie inventario com registro auditavel.
3. Execute Maven tests, Harness e revisao independente.
4. Confirme MySQL e migrations no ambiente alvo.
5. Reative reserva gradualmente e documente risco residual.

## Registro minimo

```yaml
incident_id: INC-001
severity: high
trace_id: request-abc
summary: descricao factual
impact: impacto observado
containment: medida aplicada
root_cause: causa confirmada ou unknown
evidence: logs, dados e testes
regression_test: nome do teste
residual_risk: risco remanescente
status: open
```
