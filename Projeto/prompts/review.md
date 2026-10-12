# Code Review Independente - Flash Booking

## Precondicao

`reviewer_agent_id` deve ser diferente de `author_agent_id`; identidades ausentes ou iguais bloqueiam a revisao.

## Verificacao

Revise o diff contra os requisitos Flash Booking. Priorize contratos REST, locks/transacoes MySQL, concorrencia entre instancias, ausencia de oversell, idempotencia, expiracao, cancelamento, migration, tratamento de erro e testes. Verifique Java 21 sem Lombok, ausencia de segredos e limites explicitados na documentacao. Diferencie evidencia H2 de evidencia MySQL.

Nao inclua criterios de frontend, TypeScript, IA ou MCP; nao edite o codigo revisado.

## Saida

Liste achados por severidade, arquivo e evidencia, impacto, testes revisados/executados, riscos residuais e decisao `approved`, `approved_with_remarks` ou `blocked`.
