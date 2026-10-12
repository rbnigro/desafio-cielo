# Fatos Semanticos do Projeto

Este arquivo guarda fatos reutilizaveis e verificaveis. Um fato deve ser curto, ter uma fonte e nao depender do historico completo da conversa.

## Formato

```yaml
fact_id: SF-000
statement: fato verificavel
source: caminho ou evidencia
status: verified_documentation|verified_implementation|candidate|superseded
source_detail: secao, teste, commit ou evidencia especifica
valid_from: 2026-08-24
valid_until: null
author_agent_id: project-owner
reviewer_agent_id: explore-01
reviewer_role: documentation-review
last_verified_by: project-owner
```

## Fatos verificados

### SF-001 - Stack obrigatoria

```yaml
fact_id: SF-001
statement: O backend usa JDK 21 (sem Lombok), o frontend usa TypeScript/HTML/CSS e o banco inicial e MySQL.
source: Projeto/CONSTITUTION.md
source_detail: Principios 1 e Limites tecnicos
status: superseded
author_agent_id: project-owner
reviewer_agent_id: explore-01
reviewer_role: documentation-review
valid_from: 2026-08-24
valid_until: 2026-10-08
last_verified_by: project-owner
superseded_by: ADR-001 atualizado em 2026-10-08; frontend removido do escopo.
```

### SF-002 - Independencia do Code Review

```yaml
fact_id: SF-002
statement: O agente que implementa uma demanda nao pode ser seu Code Reviewer.
source: Projeto/agents/code-reviewer.agent.md
source_detail: Regra inviolavel
status: verified_documentation
author_agent_id: project-owner
reviewer_agent_id: explore-01
reviewer_role: documentation-review
valid_from: 2026-08-24
valid_until: null
last_verified_by: project-owner
```

### SF-003 - Degradacao segura

```yaml
fact_id: SF-003
statement: O CRUD deve continuar funcionando quando servicos de IA estiverem indisponiveis.
source: Projeto/CONSTITUTION.md
source_detail: Principio 3
status: superseded
author_agent_id: project-owner
reviewer_agent_id: explore-01
reviewer_role: documentation-review
valid_from: 2026-08-24
valid_until: 2026-10-08
last_verified_by: project-owner
superseded_by: ADR-001 atualizado em 2026-10-08; IA nao integra o desafio.
```

### SF-004 - Contexto minimo

```yaml
fact_id: SF-004
statement: Cada agente recebe somente os artefatos necessarios para sua funcao e tarefa.
source: Projeto/docs/methodology.md
source_detail: Controle de contexto
status: verified_documentation
author_agent_id: project-owner
reviewer_agent_id: explore-01
reviewer_role: documentation-review
valid_from: 2026-08-24
valid_until: null
last_verified_by: project-owner
```

### SF-005 - Escopo Flash Booking

```yaml
fact_id: SF-005
statement: O produto e uma API backend de eventos e reservas de ingressos com capacidade finita; frontend e IA estao fora do escopo atual.
source: Projeto/AGENTS.md
source_detail: Objetivo e tecnologias
status: verified_documentation
author_agent_id: project-owner
reviewer_agent_id: pending
reviewer_role: pending
valid_from: 2026-10-08
valid_until: null
last_verified_by: project-owner
```

### SF-006 - Stack preservada

```yaml
fact_id: SF-006
statement: O backend mantem Java 21 sem Lombok, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL.
source: Codigo/backend/pom.xml
source_detail: java.version e dependencias Spring/JPA/MySQL; verificado no backend.
status: verified_implementation
author_agent_id: copilot-assistant
reviewer_agent_id: pending
reviewer_role: pending
valid_from: 2026-10-08
valid_until: null
last_verified_by: copilot-assistant
```

## Regras de uso

- fatos `verified` podem ser recuperados quando relevantes;
- fatos `candidate` exigem validacao antes de orientar uma decisao;
- todo fato deve apontar para uma fonte;
- fatos contraditos devem ser marcados como `superseded`, nunca apagados sem registro;
- nao inclua segredo, PII ou afirmacao gerada sem evidencia;
- alteracoes devem registrar `author_agent_id` e `reviewer_agent_id`.
