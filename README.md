# TaskFlow API

## Descrição
TaskFlow API é uma aplicação de gerenciamento de tarefas que permite aos usuários cadastrar-se, autenticar-se e gerenciar suas tarefas pessoais. Desenvolvida em Java 17 com Spring Boot 3, a aplicação oferece uma interface RESTful para interação com sistemas frontend.

## Funcionalidades Principais

### Autenticação e Usuários
- **Cadastro de Usuários**: Permite criar uma nova conta de usuário com nome, email e senha.
- **Login**: Sistema de autenticação que retorna um token JWT para uso nas requisições protegidas.

### Gerenciamento de Tarefas
- **Listar Tarefas**: Visualização de todas as tarefas do usuário autenticado.
- **Detalhes da Tarefa**: Consulta de informações específicas de uma tarefa.
- **Criar Tarefa**: Registro de novas tarefas com título, descrição, status e datas.
- **Atualizar Tarefa**: Modificação de tarefas existentes.
- **Excluir Tarefa**: Remoção de tarefas do sistema.

## Segurança
A API utiliza JWT (JSON Web Tokens) para autenticação e autorização:

- **Autenticação Stateless**: Não mantém sessão no servidor, usando tokens para autenticar cada requisição.
- **Proteção de Endpoints**: Apenas usuários autenticados podem acessar os endpoints protegidos.
- **Criptografia**: Senhas armazenadas no banco de dados são criptografadas usando BCrypt.
- **Chaves RSA**: Sistema de chaves pública/privada para assinatura e validação de tokens.

## Endpoints da API

### Autenticação
- `POST /api/auth/signup`: Cadastro de novo usuário
- `POST /api/auth/login`: Autenticação e obtenção do token JWT

### Tarefas
- `GET /api/task/all`: Listar todas as tarefas do usuário
- `GET /api/task/{id}`: Obter detalhes de uma tarefa específica
- `POST /api/task`: Criar nova tarefa
- `PUT /api/task/{id}`: Atualizar uma tarefa existente
- `DELETE /api/task/{id}`: Excluir uma tarefa

## Como Usar
Todas as requisições para endpoints protegidos devem incluir o cabeçalho de autorização:
```
Authorization: <token_jwt>
```

## Modelo de Dados
- **Usuário**: Contém informações do usuário (nome, email, senha)
- **Tarefa**: Representa uma tarefa com título, descrição, status (PENDENTE, EM_ANDAMENTO, CONCLUÍDA), data de criação e prazo.
