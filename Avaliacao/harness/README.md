# Flash Booking Acceptance Harness

`run_harness.py` valida o backend do desafio Flash Booking sem depender de servicos de IA ou do frontend. O Harness confere os artefatos de entrega e o contrato, Java 21/Spring/JPA/MySQL, Docker Compose, ausencia do dominio CRUD anterior e padroes de segredos; por fim executa os testes Maven.

## Executar

Na raiz do workspace:

```powershell
python Avaliacao/harness/run_harness.py
```

Grave o resultado para avaliacao:

```powershell
python Avaliacao/harness/run_harness.py --report Avaliacao/reports/baseline.json
```

Para diagnostico sem testes, use `--skip-tests`. Essa execucao termina bloqueada, pois testes automatizados sao evidencias obrigatorias.

## Verificacoes

- README, contrato REST, arquitetura, modelo de dados, migration SQL, Dockerfile e Compose;
- cinco rotas funcionais documentadas;
- Java 21, Spring Web/JPA, MySQL e Flyway; sem Lombok;
- duas aplicacoes/servicos no Compose (API e MySQL), volume persistente e runtime nao-root;
- ausencia de arquivos do frontend e do CRUD de pacientes no backend;
- busca por segredos hard-coded;
- `mvn -q test`, incluindo contrato, estados de reserva e concorrencia.

O Compose e validado com `docker compose config --quiet` quando Docker estiver instalado. Sem Docker, a configuracao recebe ressalva estatica; os testes Maven ainda sao executados. A suite inclui concorrencia em H2 e um teste MySQL Testcontainers, que e ignorado automaticamente quando Docker nao esta disponivel. Diferencie os resultados no relatorio.
