# Roteiro – Persistência com Spring (Spring Boot + Spring Data JPA + SQLite)

## 1. Visão geral (1 min)
O sistema tem uma interface web e uma API REST. Ele cadastra clientes, contas e boletos e simula **transferências** (Pix, TED e DOC) e **pagamentos** (boleto, Pix e saldo em conta). O SQLite grava os dados em `banco.db` e permite consultá-los depois de reiniciar a aplicação.

Fluxo de uma requisição:

```
HTTP (JSON) -> Controller -> Service (regras + @Transactional) -> Repository (Spring Data) -> Hibernate/JPA -> SQLite
```

## 2. Dependências (`pom.xml`)
| Dependência | Para quê |
|---|---|
| `spring-boot-starter-web` | Controllers REST (Tomcat + Jackson/JSON) |
| `spring-boot-starter-data-jpa` | JPA + Hibernate + Spring Data (repositórios) |
| `spring-boot-starter-validation` | `@NotNull`, `@DecimalMin`, `@Pattern` nos DTOs |
| `sqlite-jdbc` | Driver JDBC do SQLite |
| `hibernate-community-dialects` | Dialeto `SQLiteDialect` (o Hibernate não traz SQLite no núcleo) |

## 3. Configuração (`application.properties`)
```properties
spring.datasource.url=jdbc:sqlite:banco.db          # arquivo do banco (criado automaticamente)
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.hikari.maximum-pool-size=1        # SQLite aceita 1 escritor por vez
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update                # cria/atualiza as tabelas a partir das @Entity
```
Ponto-chave: **não escrevemos SQL de criação de tabelas**. O Hibernate gera o schema a partir das classes anotadas.

## 4. Entidades (`model/`) – classes que viram tabelas
- `@Entity` + `@Table`: a classe vira tabela. `@Id @GeneratedValue(IDENTITY)`: chave autoincremento.
- `@Column`: restrições (`nullable`, `unique`, `precision`).
- Relacionamentos: `Conta` → `@ManyToOne Cliente`; `Cliente` → `@OneToMany(mappedBy="cliente")` contas; `Transferencia`/`Pagamento` → `@ManyToOne Conta`.
- `@Enumerated(EnumType.STRING)`: grava `PIX`, `CONCLUIDA` etc. como texto.

| Entidade | Origem nos diagramas |
|---|---|
| `Cliente` | Ator "Cliente" |
| `Conta` | verificarSaldo / reservarSaldo / confirmarDebito (saldo + saldo reservado) |
| `Transferencia` | Transferência PIX, TED, DOC (DOC fica `AGENDADO`) |
| `Pagamento` | Pagamento BOLETO, PIX, Saldo em Contas |
| `Boleto` | consultarDadosBoleto |

## 5. Repositórios (`repository/`) – acesso a dados sem SQL
`interface XRepository extends JpaRepository<Entidade, Long>` já traz `save`, `findById`, `findAll`, `count`, `delete`.
Também há **consultas derivadas do nome do método**, ex.: `findByCpf`, `findByAgenciaAndNumero`, `findByStatusAndDataProcessamentoLessThanEqual`. O Spring implementa tudo em tempo de execução.

## 6. Services (`service/`) – regras de negócio e transações
- `@Transactional`: se der erro no meio, o JPA desfaz tudo (rollback). Ex.: débito + registro da transferência acontecem juntos ou nenhum.
- `CadastroService`: cadastro de cliente, conta e boleto (valida duplicidades).
- `TransferenciaService` valida a entrada e verifica o saldo. Pix e TED são autorizados, debitados e registrados na hora. O DOC reserva saldo no agendamento e solicita autorização no processamento. Nesse momento, o serviço confirma o débito ou libera a reserva.
- `PagamentoService` valida os dados de cada modalidade e usa a mesma rotina para verificar saldo, autorizar, debitar e registrar. Após o débito, marca o boleto como pago ou credita a conta de destino, conforme a modalidade.
- `ContaService`: debitar, creditar, reservar, confirmar/liberar reserva.
- `BancoCentralGateway` define a interface de autorização. `BancoCentralSimulado` executa essa regra no próprio aplicativo e aprova valores até o limite configurado.
- A API grava recusas de negócio com status `NEGADA` e o motivo. Na solicitação de transferência ou pagamento, uma recusa retorna HTTP 422. No processamento de um DOC já agendado, a resposta informa o estado resultante e o motivo, quando houver.

## 7. Controllers (`controller/`) e DTOs (`dto/`)
- `@RestController` + `@RequestMapping`: expõem a API. `@Valid` valida o JSON de entrada.
- DTOs (`record`) separam o JSON da entidade (evita expor/serializar relações lazy).
- `ApiExceptionHandler` (`@RestControllerAdvice`) traduz erros de validação e registros ausentes em respostas JSON. A interface exibe o status HTTP e o retorno da operação, incluindo o motivo das recusas. Isso corresponde à exibição de erro dos diagramas.

## 8. Demonstração passo a passo (PowerShell)
Suba a aplicação (`BancoApplication`) e rode:

```powershell
$h=@{'Content-Type'='application/json'}; $u='http://localhost:8080/api'

# 1) Cadastrar clientes
irm -Method Post "$u/clientes" -Headers $h -Body '{"nome":"Ana Souza","cpf":"11111111111"}'
irm -Method Post "$u/clientes" -Headers $h -Body '{"nome":"Bruno Lima","cpf":"22222222222"}'

# 2) Criar contas (clienteId vem da resposta anterior)
irm -Method Post "$u/contas" -Headers $h -Body '{"clienteId":1,"agencia":"0001","numero":"12345-6","saldoInicial":5000}'
irm -Method Post "$u/contas" -Headers $h -Body '{"clienteId":2,"agencia":"0001","numero":"65432-1","saldoInicial":1000}'

# 3) Cadastrar um boleto
irm -Method Post "$u/boletos" -Headers $h -Body '{"codigoBarras":"11111111111111111111111111111111111111111111111","beneficiario":"Companhia de Energia","valor":250.75,"vencimento":"2026-12-01"}'

# 4) Transferência PIX (debita na hora)
irm -Method Post "$u/transferencias/pix" -Headers $h -Body '{"contaOrigemId":1,"valor":100,"chavePix":"bruno@email.com","destinatario":"Bruno"}'

# 5) Transferência DOC (reserva saldo e agenda) e processamento manual para demonstração
irm -Method Post "$u/transferencias/doc" -Headers $h -Body '{"contaOrigemId":1,"valor":200,"bancoDestino":"237","agenciaDestino":"1","contaDestino":"2","cpfDestino":"22222222222"}'
irm -Method Post "$u/transferencias/2/processar"

# 6) Pagamentos
irm -Method Post "$u/pagamentos/boleto" -Headers $h -Body '{"contaId":1,"codigoBarras":"11111111111111111111111111111111111111111111111"}'
irm -Method Post "$u/pagamentos/saldo_em_conta" -Headers $h -Body '{"contaId":1,"valor":10,"destinatario":"65432-1"}'

# 7) Falha de negócio (saldo insuficiente -> 422 e registro NEGADA)
irm -Method Post "$u/transferencias/pix" -Headers $h -Body '{"contaOrigemId":1,"valor":999999,"chavePix":"x","destinatario":"Y"}'

# 8) Conferir
irm "$u/contas"; irm "$u/transferencias/conta/1"; irm "$u/pagamentos/conta/1"; irm "$u/boletos"
```

## 9. Provar a persistência
1. Pare a aplicação e suba de novo.
2. `irm http://localhost:8080/api/contas` → saldos e movimentações continuam.
3. Abra `banco.db` (DB Browser for SQLite ou IntelliJ Database) e mostre as tabelas `cliente`, `conta`, `transferencia`, `pagamento`, `boleto` criadas pelo Hibernate.
4. Para repetir a demonstração com dados novos, use um arquivo separado. Passe `--spring.datasource.url=jdbc:sqlite:banco-teste.db` nos argumentos da aplicação.

## 10. Comparação com os diagramas de sequência, pacotes e implantação

| Diagrama | Correspondência e diferenças |
|---|---|
| Sequência de transferência Pix e TED | `TransferenciaService` valida os dados, consulta o saldo, solicita autorização simulada, debita a origem e registra a operação. A interface exibe o resultado em JSON. No `.asta`, a sequência da TED usa o rótulo `exibirComprovantePIX`. |
| Sequência de transferência DOC | A sequência verifica o cliente de destino e autoriza o DOC antes de confirmar o agendamento. O código valida os campos e o formato do CPF, reserva o saldo no agendamento e solicita autorização no processamento posterior. O processamento em lote espera a data agendada. O botão **Processar agora** permite antecipá-lo na demonstração. |
| Sequências de pagamento por boleto, Pix e saldo em conta | `PagamentoService` corresponde ao fluxo de validação, saldo, autorização, débito e registro. O boleto é consultado no cadastro local. Pagamento com saldo também credita a conta de destino. A interface mostra o registro da operação em JSON. |
| Diagrama de pacotes | `pagamento`, `boleto` e `SaldoEmConta` correspondem ao serviço de pagamentos e às operações de boleto e conta. `tranferencia`, `pix`, `ted` e `doc` aparecem como operações do serviço de transferências. `Saldo` corresponde a `ContaService`. `pix2` aparece como modalidade de pagamento e `BancoCentral` corresponde ao `BancoCentralGateway` e à implementação simulada. |
| Implantação | O diagrama representa Nginx e Kong na borda, serviços Spring Boot em Kubernetes, RabbitMQ para eventos, PostgreSQL e S3 na camada de dados e uma integração externa com o Banco Central. A aplicação local executa a interface e a API no mesmo processo e usa um SQLite compartilhado. Essa versão cobre o exercício de persistência. Para reproduzir a implantação desenhada, seriam necessárias aplicações implantáveis de forma independente, comunicação entre serviços e configuração da infraestrutura. |

O pacote `doc` do modelo de pacotes e das sequências aparece como `docs` no desenho de implantação, com a descrição “Gerenciamento documental”. Se essa caixa deveria representar o DOC bancário, use `doc` com a descrição “Transferência DOC”. Se ela representa documentos, o desenho de implantação ainda precisa mostrar onde roda o DOC. O projeto mantém esse fluxo para acompanhar o modelo acadêmico. [Febraban sobre a descontinuação do DOC](https://portal.febraban.org.br/noticia/4290/pt-br/)
