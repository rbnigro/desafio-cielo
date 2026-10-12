# Arquitetura de Workspaces e Contextos

## Ambientes Cielo

```text
Curso      -> aprender e receber desafios
Projeto    -> implementar e documentar
Avaliacao  -> testar e medir independentemente
```

Os workspaces pertencem ao ambiente Cielo. O produto tecnico atual vive em `Codigo/backend` e implementa uma API Java 21/Spring Boot/JPA/MySQL para Flash Booking.

## Workspace Curso

Contem aulas, referencias, exercicios e requisitos liberados. O agente tutor pode consultar o material didatico. O agente de codificacao recebe somente os requisitos e materiais autorizados para a tarefa atual.

## Projeto

Contem codigo, agentes, prompts, especificacoes e documentacao de arquitetura. A aplicacao e backend-only; nao inclui mais os projetos de CRUD academico ou frontend do template.

## Avaliacao

Contem Harness, cenarios, rubricas e relatorios. O Harness avalia o contrato de eventos/reservas, as invariantes de inventario, o stack Java/MySQL, os testes, Compose e a documentacao.

## Entrega controlada

O Curso fornece ao Projeto requisitos, contratos, restricoes e Definition of Done, sem solucoes privadas ou gabaritos de avaliacao.

## Contexto minimo

```yaml
workspace: Cielo
allowed:
  - Curso/Docs/Decisao_Tecnologica.md
  - Codigo/backend/
  - Projeto/docs/
  - Avaliacao/harness/
excluded:
  - credenciais locais
  - dados privados de avaliacao
```

Separar pastas melhora organizacao, mas isolamento real exige permissoes, ferramentas, montagem de contexto e logs de acesso.

## Fluxo

```text
aprender -> receber desafio -> implementar -> testar -> revisar -> avaliar -> corrigir -> publicar
```
