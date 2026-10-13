# Auto Escola 3ESA — API REST de agendamento de instruções

API REST em **Java + Spring Boot** para gerenciar uma auto-escola: instrutores, alunos, usuários (login JWT) e o
agendamento/cancelamento de instruções. Projeto da disciplina **SOA e WebServices** (Checkpoint 5).

## Integrantes

Caio Alexandre dos Santos - RM: 558460

Leandro do Nascimento Souza - RM: 558893

Rafael de Mônaco Maniezo - RM: 556079

Vinicius Rozas Panucci de Paula Cont - RM: 555338

## Tecnologias

Java 25 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA · MySQL · Flyway · springdoc-openapi (Swagger) · JUnit 5 · Mockito · MockMvc

## Como executar

1. Tenha o **MySQL** rodando em `localhost:3306` e crie o banco:
   ```sql
   create database autoescola3esa;
   ```
2. Ajuste usuário/senha em `src/main/resources/application.properties` (padrão: `root` / `fiap`).
3. (Opcional) defina o segredo do JWT: variável de ambiente `JWT_SECRET`.
4. Suba a aplicação (as tabelas são criadas pelo Flyway):
   ```bash
   ./mvnw spring-boot:run
   ```
5. A API sobe em `http://localhost:8085`.

### Primeiro usuário (ADMIN)

Gere o hash BCrypt rodando o `main` de `src/test/java/br/com/fiap3esa/autoescola3esa/util/Encriptador.java` (gera o hash da senha `admin`; troque o texto no código se quiser outra senha) e insira:

```sql
insert into usuarios (login, senha, perfil) values ('admin', '<hash-bcrypt>', 'ADMIN');
```

Depois faça `POST /login` com `{"login": "admin", "senha": "<senha>"}` e use o token em `Authorization: Bearer <token>`.

## Documentação (Swagger / OpenAPI)

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8085/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8085/v3/api-docs |
| OpenAPI (YAML) | http://localhost:8085/v3/api-docs.yaml |

No Swagger UI, clique em **Authorize** e informe o token JWT retornado por `/login`.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/login` | Autenticação (retorna o JWT) |
| CRUD | `/instrutores` | Instrutores (escrita somente ADMIN) |
| CRUD | `/alunos` | Alunos |
| CRUD | `/usuarios` | Usuários (somente ADMIN) |
| POST | `/instrucoes` | Agenda uma instrução |
| PATCH | `/instrucoes/{id}/cancelamento` | Cancela uma instrução (informando o motivo) |
| GET | `/instrucoes`, `/instrucoes/{id}` | Consulta instruções |
| GET | `/enderecos/cep/{cep}` | Consulta endereço pelo CEP (**API externa ViaCEP**) |

### Regras de negócio do agendamento

- Funcionamento de segunda a sábado, das 06:00 às 21:00; instruções de **1 hora**, sempre em **hora cheia** (último início: 20:00).
- Antecedência mínima de **30 minutos** para agendar.
- Máximo de **2 instruções por dia** por aluno.
- Aluno e instrutor precisam estar **ativos**; o instrutor não pode ter outra instrução no mesmo horário.
- Instrutor opcional: se não for informado, é **sorteado** um instrutor disponível da especialidade.
- Cancelamento exige **motivo** (`ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`) e **24 horas** de antecedência.

## Consumo de API externa (ViaCEP)

`GET /enderecos/cep/{cep}` consulta `https://viacep.com.br/ws/{cep}/json/` usando `RestClient` (timeouts de 3 s/5 s) e devolve o
endereço traduzido. Erros: CEP inválido → 422, CEP inexistente → 404, ViaCEP indisponível → 502.
Código em `infra/externo/viacep`.

## CORS

Configurado em `infra/security/SecurityConfig` (origens, métodos GET/POST/PUT/PATCH/DELETE/OPTIONS e cabeçalhos
`Authorization`, `Content-Type`...). Coberto por testes de preflight em `DocumentacaoECorsTest`.

## Testes automatizados

```bash
./mvnw test
```

| Camada | Entidades cobertas | Precisa de banco? |
|---|---|---|
| Regras de entidade (`*Test`) | Aluno, Instrutor, Usuário, Instrução | Não |
| Services com Mockito (`*ServiceTest`) | Aluno, Instrutor, Usuário, Instrução (todas as regras de agendamento/cancelamento) | Não |
| Cliente ViaCEP (`ViaCepClientTest`) | API externa, com `MockRestServiceServer` (sem internet) | Não |
| Repositórios (`*RepositoryTest`) | Aluno, Instrutor, Usuário, Instrução | **Sim** (MySQL; banco `autoescola3esa_test` criado automaticamente) |
| Controllers + Swagger + CORS (`*ControllerTest`, `DocumentacaoECorsTest`) | Aluno, Instrutor, Usuário, Instrução, Endereço | **Sim** (sobem o contexto completo) |
