# Seguranca do backend Flash Booking

## Escopo e limite

Esta arquitetura cobre API REST, validacao, persistencia MySQL, configuracao e operacao em Compose. Autenticacao/autorizacao, pagamento, gateway/WAF e rate limiting nao sao implementados pelo desafio; nao exponha a instancia local diretamente a internet.

## Controles

- Validar corpos, UUIDs, quantidades positivas e `Idempotency-Key` limitada a 128 caracteres.
- Parametrizar acesso a dados via Spring Data JPA; nao construir SQL com entrada de usuario.
- Manter credenciais em variaveis de ambiente/`.env` local ignorado, sem valores reais versionados.
- Restringir MySQL a rede privada do Compose em ambientes publicados; a porta do banco publicada no Compose e somente para desenvolvimento local.
- Usar usuario de aplicacao com privilegios restritos fora do bootstrapping do schema.
- Usar TLS e identidade/autorizacao antes de qualquer exposicao publica.
- Nao incluir dados sensiveis desnecessarios em logs ou respostas de erro.
- Preservar integridade por transacoes, chaves unicas e constraints do banco; disponibilidade consultada nao autoriza uma venda.

## Incidente

Em caso de credencial exposta ou acesso indevido, revogue a credencial, restrinja o servico, preserve logs sem copiar valores secretos, investigue eventos/reservas afetados, corrija e adicione teste de regressao antes de reabrir acesso.

## Riscos pendentes

Sem autenticacao/autorizacao e rate limiting, a API e apropriada apenas para demonstracao local/controlada. Antes de producao, adicionar controles de identidade, abuso, TLS, rotacao de credenciais e observabilidade auditavel.
