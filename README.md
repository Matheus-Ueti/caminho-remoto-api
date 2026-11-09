# Caminho Remoto API

Backend do Caminho Remoto, em Java 21 e Spring Boot 4. Por enquanto cuida só da **autenticação**
(cadastro, login, sair e "quem está logado").

## Como rodar

Requer o **JDK 21**. Não precisa instalar o Maven: o projeto usa o `mvnw` (Maven embutido).

```bash
./mvnw spring-boot:run     # sobe a API em http://localhost:8080
./mvnw test                # roda os testes
```

> **Antivírus com inspeção de HTTPS (ex.: Avast):** se o Maven der erro `PKIX path building failed`,
> rode uma vez `setx MAVEN_OPTS "-Djavax.net.ssl.trustStoreType=Windows-ROOT"` e reabra o terminal.
> Isso faz o Java usar os certificados do Windows.

## Rotas

| Método | Rota             | Precisa de login? | O que faz                                          |
| ------ | ---------------- | ----------------- | -------------------------------------------------- |
| POST   | `/auth/cadastro` | Não               | Cria a conta e já deixa a pessoa logada            |
| POST   | `/auth/login`    | Não               | Confere e-mail e senha e cria o cookie de login    |
| POST   | `/auth/sair`     | Não               | Apaga o cookie de login                            |
| GET    | `/auth/eu`       | Sim               | Devolve id, nome e e-mail de quem está logado      |

Exemplo de cadastro:

```json
POST /auth/cadastro
{ "nome": "Maria", "email": "maria@exemplo.com", "senha": "senha-segura-123" }
```

Erros sempre vêm neste formato:

```json
{ "mensagem": "Confira os campos do formulário.", "campos": { "email": "Informe um e-mail válido." } }
```

## Como o login funciona

1. No cadastro ou no login, a API gera um **JWT** (um "crachá digital" assinado) com o id do usuário.
2. O JWT vai num **cookie `httpOnly`**, que o JavaScript da página não consegue ler.
3. O navegador envia o cookie sozinho a cada pedido. O front precisa usar `credentials: 'include'` no `fetch`.
4. As senhas são guardadas com **BCrypt**, nunca em texto.

## Estrutura

```
src/main/java/br/com/caminhoremoto/api/
  config/          Regras de segurança, JWT, CORS e leitura do cookie
  usuario/         Tabela de usuários e acesso ao banco
  autenticacao/    Rotas, regras de cadastro/login, geração do token e o cookie
    dto/           Formatos de entrada e saída (records)
  erro/            Respostas de erro em português
```

## Configurações (`application.properties`)

| Propriedade                     | Para quê                                                    |
| ------------------------------- | ----------------------------------------------------------- |
| `autenticacao.jwt.chave-secreta` | Assina o JWT. Em produção, use a variável `JWT_CHAVE_SECRETA` |
| `autenticacao.jwt.validade`      | Quanto tempo o login dura (hoje, 8 horas)                    |
| `autenticacao.cookie.seguro`     | `true` em produção (só envia o cookie por HTTPS)             |
| `autenticacao.front.origem`      | Endereço do front que pode chamar a API                      |

**Decisão em aberto:** o banco de dados definitivo. Hoje usamos o **H2 em memória**: os usuários
somem quando a API reinicia.
