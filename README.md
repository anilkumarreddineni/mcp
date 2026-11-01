# mcp

Spring Boot services (Java 17).

| Service         | Port | Endpoints                                                       |
|-----------------|------|-----------------------------------------------------------------|
| `mcp-server`    | 8080 | `GET /hello`, MCP (Streamable HTTP) at `/mcp`                   |
| `order-service` | 8081 | `GET /orders`, `GET /orders/{orderId}/status`                   |
| `chat-app`      | 8082 | Chat UI at `/`, `POST /api/chat`                                |

`python-llm-client` is a separate Python command-line chat with Claude. It doesn't use the services above. See [python-llm-client/README.md](python-llm-client/README.md).

## Run locally

`chat-app` calls Claude, so set your Anthropic API key first. Never commit it.

```sh
export ANTHROPIC_API_KEY=sk-ant-...            # PowerShell: $env:ANTHROPIC_API_KEY="sk-ant-..."
```

Start each service from its own directory, in this order:

```sh
cd order-service && ./mvnw spring-boot:run
cd mcp-server && ./mvnw spring-boot:run
cd chat-app && ./mvnw spring-boot:run
```

Then open http://localhost:8082 and ask, for example, "What's the status of order 1003?".

Run tests with `./mvnw test` in any service directory. They don't need an API key.

All services use [Lombok](https://projectlombok.org/) (`@RequiredArgsConstructor` for constructor injection). Maven needs no setup. VS Code's Java extension supports Lombok out of the box. In IntelliJ, enable annotation processing.

## Chat app

`chat-app` uses Spring AI's `ChatClient` with Claude and connects to `mcp-server` as an MCP client, so Claude answers order questions by calling the MCP tools below. Each browser session keeps its own conversation history in memory, so follow-up questions such as "and order 1004?" work.

| Environment variable | Default                 | Purpose                    |
|----------------------|-------------------------|----------------------------|
| `ANTHROPIC_API_KEY`  | none (required)         | Claude API key             |
| `ANTHROPIC_MODEL`    | `claude-haiku-5-5`      | Claude model to use        |
| `MCP_SERVER_URL`     | `http://localhost:8080` | Where `mcp-server` runs    |

`chat-app` logs to the console and to `chat-app/logs/chat-app.log`, which is git-ignored. For every Claude API call it logs:

- the endpoint, HTTP status and duration, e.g. `<-- 200 POST https://api.anthropic.com/v1/messages (755 ms)`. The API key is never logged.
- the request and response content: messages, tool calls, tool results and token usage. This includes what users type. To turn it off, set `logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=INFO` in `chat-app/src/main/resources/application.properties`.

`chat-app` starts even when `mcp-server` is down, and connects on the first chat message. If `mcp-server` or the Claude API can't be reached, the chat shows a friendly error and the details go to the `chat-app` log.

## MCP tools

`mcp-server` exposes `order-service` to MCP clients through these tools:

| Tool             | Arguments | Calls                                   |
|------------------|-----------|-----------------------------------------|
| `listOrders`     | none      | `GET /orders`                           |
| `getOrderStatus` | `orderId` | `GET /orders/{orderId}/status`          |

An unknown order ID or an unreachable `order-service` is returned as a tool error (`isError: true`) with a readable message.

`mcp-server` finds `order-service` through `order-service.base-url` (default `http://localhost:8081`). Override it with, for example, `--order-service.base-url=http://orders.internal:8081`.

### Connect an MCP client

With both services running:

- **Claude Code:** `claude mcp add --transport http orders http://localhost:8080/mcp`
- **MCP Inspector:** `npx @modelcontextprotocol/inspector`, then choose transport *Streamable HTTP* and URL `http://localhost:8080/mcp`.
