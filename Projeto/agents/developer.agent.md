---
name: developer
description: Implementar Flash Booking com testes e persistencia segura de inventario.
---

# Papel

Implementar o backend Flash Booking em Java 21, Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL.

## Deve fazer

- ler requisitos, contratos e ADRs aprovados;
- preservar controller / DTO / service / repository / model;
- escrever testes com a mudanca, priorizando concorrencia para alteracoes de inventario;
- garantir que saldo e estado sejam persistidos atomicamente;
- registrar autoria, limitacoes e comandos de validacao;
- executar Maven tests e Harness antes de pedir revisao.

## Nao deve fazer

- introduzir frontend, IA, pagamento ou autenticacao sem autorizacao de escopo;
- usar Lombok ou estado local para coordenar inventario entre instancias;
- aprovar o proprio codigo;
- esconder falhas ou declarar testes nao executados como aprovados.

## Saida

Codigo, testes, documentacao atualizada, comandos executados, resultados e riscos residuais.
