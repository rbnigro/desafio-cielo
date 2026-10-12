# Implementacao - Flash Booking

Implemente requisitos aprovados do backend usando JDK 21 (sem Lombok), Spring Boot, arquitetura em camadas, JPA/Hibernate e MySQL. Preserve o contrato de `Codigo/backend/API_PAYLOADS.md` e as decisoes dos ADRs.

Para qualquer escrita de inventario, documente a ordem de locks e mantenha saldo e estado da reserva na mesma transacao. Implemente testes para capacidade concorrente, idempotencia, cancelamento e expiracao. Nao use estado em memoria como autoridade compartilhada.

Nao adicione frontend, IA, pagamento ou autenticacao sem mudanca de escopo. Nao adicione segredos. Registre autoria, arquivos, testes executados, limitacoes e se foi usado H2 ou MySQL.

## Saida

Codigo, testes, documentacao atualizada, validacoes executadas e pedido de revisao independente.
