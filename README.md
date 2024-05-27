# ProductsAPI

A ProductsAPI é uma aplicação Spring Boot que gerencia produtos e usuários, integrando múltiplos repositórios e fontes de dados.

## Funcionalidades

- **Gerenciamento de Produtos**: Crie, atualize, delete e visualize produtos através de uma API REST.
- **Sincronização de Memória**: Sincronize dados em tempo real entre diferentes fontes de dados usando Application Runners para inicialização.

## Configuração do Projeto

### Pré-Requisitos

- Java JDK 11 ou superior
- Maven para gerenciamento de dependências
- PostgreSQL e H2 como sistemas de gerenciamento de banco de dados

### Configuração de Datasource

O projeto utiliza duas fontes de dados configuradas em `DataSourceConfig`:

1. **Primary DataSource**: Conecta-se a um banco de dados PostgreSQL para operações de produção.
2. **Secondary DataSource**: Utiliza um banco de dados em memória H2 para operações de teste e desenvolvimento.

### Entidades

Entidades em `com.dev.ProductsAPI.models` representam as tabelas no banco de dados. Inclui entidades para usuários e produtos, cada uma configurada com JPA para interação com o banco de dados.

### Repositórios

Os repositórios em `com.dev.ProductsAPI.repository` facilitam a interação com o banco de dados, abstraindo as operações de CRUD:

- **ProductRepository**: Interage com a tabela de produtos no banco de dados primário.
- **UserRepository**: Gerencia as informações dos usuários no banco de dados primário.

### Estrutura dos Testes

1. **Testes de Controllers**: Verificam os endpoints da API REST, simulando requisições HTTP e validando as respostas.
2. **Testes de Services**: Testam a lógica de negócios, interações com repositórios e serviços externos.

### Configuração dos Testes

- **Testes de Integração**: Utilizam o `Secondary DataSource` com H2 para garantir que não haja impacto no banco de dados de produção.
- **Mocking**: Testes utilizam o `Mockito` para mockar dependências, como repositórios e serviços, permitindo testes isolados das unidades de código.

