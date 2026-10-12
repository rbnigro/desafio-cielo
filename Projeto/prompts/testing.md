# Testes e validacao - Flash Booking

## Objetivo

Verificar contratos, integridade transacional, concorrencia, persistencia e entrega do backend.

## Casos obrigatorios

- exercitar as cinco rotas, campos invalidos e recursos inexistentes;
- executar requisicoes concorrentes acima da capacidade e provar que o saldo nunca fica negativo;
- repetir uma chave idempotente com payload igual e com payload diferente;
- cancelar e expirar reservas e confirmar a liberacao exatamente uma vez;
- testar isolamento/locks no banco real MySQL quando Docker estiver disponivel;
- validar migrations e inicializacao com Docker Compose.

Testes H2 podem dar feedback rapido, mas nao comprovam sozinhos a semantica de lock do MySQL. Registre claramente banco, perfil, versao e se a verificacao real foi executada.

## Saida

Relate comando, ambiente, banco, casos executados, resultado, falhas, cobertura relevante e riscos nao testados. Nunca declare sucesso sem evidencia.
