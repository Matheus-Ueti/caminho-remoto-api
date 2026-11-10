# Caminho Remoto API

Backend do Caminho Remoto, em Java 21 e Spring Boot 4. Por enquanto cuida só da **autenticação**,
que é feita pelo **Keycloak** (um servidor de login pronto). A API faz a ponte entre o site e o Keycloak.

## O que precisa estar rodando

| Serviço  | Porta | Pasta                                 | Como subir                      |
| -------- | ----- | ------------------------------------- | ------------------------------- |
| Keycloak | 8180  | `../keycloak` (fora dos repositórios) | `keycloak\iniciar-keycloak.cmd` |
| API      | 8080  | esta pasta                            | `./mvnw spring-boot:run`        |
| Site     | 3000  | `../CaminhoRemoto`                    | `npm run dev`                   |

Suba nessa ordem, cada um num terminal. Depois abra **http://localhost:3000**.
A API não tem páginas: se você abrir `http://localhost:8080` direto, ela responde 401, e isso é normal.

## Como instalar (uma vez)

1. **JDK 21** (ex.: Temurin, em adoptium.net). Não precisa instalar o Maven: o projeto usa o `mvnw`.
2. **Keycloak 26:** baixe o `.zip` em github.com/keycloak/keycloak/releases e extraia numa pasta
   chamada `keycloak`, ao lado desta pasta (`ProjetoClaudio/keycloak`). Ele roda com o mesmo JDK 21.
3. **Antivírus com inspeção de HTTPS (ex.: Avast):** se o Maven der erro `PKIX path building failed`,
   rode uma vez `setx MAVEN_OPTS "-Djavax.net.ssl.trustStoreType=Windows-ROOT"` e reabra o terminal.
   Isso faz o Java usar os certificados do Windows.

O Keycloak é um programa pronto, feito com outra tecnologia por dentro (Quarkus). Nós não mexemos no
código dele: só o configuramos pelo arquivo `keycloak/realm-caminho-remoto.json`.

## Keycloak

- **Console de administração:** http://localhost:8180, usuário **`admin`**, senha **`admin`**.
  **Somente para desenvolvimento**: troque antes de colocar no ar.
- O script `keycloak/iniciar-keycloak.cmd` importa o arquivo `keycloak/realm-caminho-remoto.json`,
  que cria o espaço do projeto (o "realm" `caminho-remoto`):
  - cadastro aberto, com o e-mail como nome de usuário;
  - telas em português;
  - a API cadastrada como cliente (`caminho-remoto-api`), com PKCE obrigatório;
  - "esqueci a senha" desligado, porque ainda não há servidor de e-mail configurado.
- As contas criadas ficam salvas no Keycloak, mesmo depois de reiniciar.
- Para ver as contas: entre no console, troque o realm para `caminho-remoto` e abra **Users**.

## Como o login funciona

1. O site leva a pessoa para `GET /oauth2/authorization/keycloak` na API (com `?cadastro=true`, para criar conta).
2. A API manda a pessoa para a tela de login do Keycloak.
3. Depois do login, o Keycloak devolve a pessoa para a API (`/login/oauth2/code/keycloak`).
4. A API guarda quem está logado numa **sessão** (cookie `JSESSIONID`, protegido) e manda a pessoa para `/aluno` no site.
5. O site pergunta `GET /auth/eu` para saber quem está logado. O `fetch` precisa de `credentials: 'include'`.

Os tokens do Keycloak ficam só na API e nunca chegam ao navegador.

## Rotas

| Método | Rota                             | O que faz                                                           |
| ------ | -------------------------------- | ------------------------------------------------------------------- |
| GET    | `/oauth2/authorization/keycloak` | Abre a tela de login do Keycloak (`?cadastro=true`: criar conta)    |
| GET    | `/auth/eu`                       | Devolve `id`, `nome` e `email` de quem está logado (401 se ninguém) |
| GET    | `/auth/sair`                     | Encerra o login na API e no Keycloak e volta para o site            |
| GET    | `/vagas`                         | Lista as vagas, sem precisar de login (`?busca=texto` filtra pelo título) |
| GET    | `/vagas/{id}`                    | Devolve uma vaga (404 se não existe)                                |

As duas primeiras e a de sair são criadas pelo Spring Security; só `/auth/eu` está no nosso controller.

## Vagas (Jooble)

As vagas vêm do **Jooble**, um site que junta vagas de vários outros. A API busca vagas home office ao subir
e depois a cada 6 horas, e guarda tudo **na memória** (some ao reiniciar e volta na próxima busca).

- Peça a chave em https://br.jooble.org/api/about e rode uma vez `setx JOOBLE_API_KEY "sua-chave"`, depois reabra o terminal.
- Sem a chave, a API sobe normalmente e `/vagas` devolve uma lista vazia.
- Palavras buscadas, local e intervalo ficam no `application.properties`.
- **Avast:** se o log mostrar `PKIX path building failed`, suba a API assim:
  `./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Djavax.net.ssl.trustStoreType=Windows-ROOT"`

## Estrutura

```
src/main/java/br/com/caminhoremoto/api/
  config/
    SegurancaConfig.java            Regras de segurança, login com Keycloak, sair e CORS
    ResolvedorDoPedidoDeLogin.java  Liga o PKCE e a opção "?cadastro=true"
    SaidaDoKeycloak.java            Ao sair, encerra o login também no Keycloak
  autenticacao/
    AutenticacaoController.java     Rota /auth/eu
    dto/UsuarioResponse.java        O que a API devolve sobre quem está logado
keycloak/
  realm-caminho-remoto.json         Configuração do Keycloak para este projeto
  iniciar-keycloak.cmd              Sobe o Keycloak na porta 8180
```

## Configurações (`application.properties`)

| Propriedade                              | Para quê                                                                         |
| ---------------------------------------- | -------------------------------------------------------------------------------- |
| `autenticacao.front.origem`              | Endereço do site: quem pode chamar a API e para onde a pessoa volta              |
| `keycloak.endereco`                      | Endereço do realm no Keycloak                                                    |
| `...registration.keycloak.client-secret` | Segredo da API no Keycloak. Em produção, use a variável `KEYCLOAK_CLIENT_SECRET` |

## Testes

```bash
./mvnw test
```

Os testes não precisam do Keycloak rodando: eles simulam a pessoa logada.

**Decisão em aberto:** o banco de dados definitivo. Hoje a API não guarda nada; as contas ficam no Keycloak.
