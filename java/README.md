# mcp

Spring Boot services (Java 17) and an Angular chat UI.

Architecture diagram: [architecture.html](architecture.html) (open it in a browser), also online at https://claude.ai/artifact/KjMkfbcErEZ4qChynaV9tm. It shows how the services connect and the path one chat question takes through them.

Tool calling diagram: [tool-calling.html](tool-calling.html), also online at https://claude.ai/artifact/R7BhjwaDELxBdibwtaZpNe. It shows how Claude learns about the MCP tools, the two Claude requests behind one order question, and what SYSTEM, USER, ASSISTANT and TOOL mean in the log.

MCP configuration diagrams (how to add the Atlassian MCP server):
- Claude Code: [claude-mcp-configuration.html](claude-mcp-configuration.html), also online at https://claude.ai/artifact/Fmcf1UaGvQ5dVnnGm7YVPJ. The CLI and the VS Code extension share `~/.claude.json`, so you add the server once.
- GitHub Copilot: [copilot-mcp-configuration.html](copilot-mcp-configuration.html), also online at https://claude.ai/artifact/EPeeLsitpRr4wnioSmQTcW. Copilot CLI and Copilot in VS Code each keep their own config file, so you add it in both.

| Service         | Port | Endpoints                                                       |
|-----------------|------|-----------------------------------------------------------------|
| `mcp-server`    | 8080 | `GET /hello`, MCP (Streamable HTTP) at `/mcp`                   |
| `order-service` | 8081 | `GET /orders`, `GET /orders/{orderId}/status`                   |
| `mcp-client`    | 8082 | `POST /api/chat` (MCP client of `mcp-server`, calls Claude)     |
| `chat-ui`       | 4200 | Angular chat UI at `/`, proxies `/api` to `mcp-client`          |

## Run locally

`mcp-client` calls Claude, so set your Anthropic API key first. Never commit it.

```sh
export ANTHROPIC_API_KEY=sk-ant-...            # PowerShell: $env:ANTHROPIC_API_KEY="sk-ant-..."
```

Start each service from its own directory, in this order (`chat-ui` needs Node.js 20.19+ and runs `npm install` once first):

```sh
cd order-service && ./mvnw spring-boot:run
cd mcp-server && ./mvnw spring-boot:run
cd mcp-client && ./mvnw spring-boot:run
cd chat-ui && npm install && npm start
```

On Windows, `start-all.bat` does this for you. It opens one window per service, waits for each service to answer before it starts the next, and then opens the chat UI. To stop a service, close its window. `stop-all.bat` stops them all.

Then open http://localhost:4200 and ask, for example, "What's the status of order 1003?".

Run tests with `./mvnw test` in any Spring Boot service directory, and `npm test` in `chat-ui`. They don't need an API key.

All services use [Lombok](https://projectlombok.org/) (`@RequiredArgsConstructor` for constructor injection). Maven needs no setup. VS Code's Java extension supports Lombok out of the box. In IntelliJ, enable annotation processing.

## MCP client

`mcp-client` is the backend for the chat. It has no UI of its own. It uses Spring AI's `ChatClient` with Claude and connects to `mcp-server` as an MCP client, so Claude answers order questions by calling the MCP tools below. Each browser session (the `JSESSIONID` cookie) keeps its own conversation history in memory, so follow-up questions such as "and order 1004?" work.

`POST /api/chat` takes `{"message": "..."}` and returns `{"reply": "..."}`. A blank message returns 400, and an unreachable `mcp-server` or Claude API returns 503, both with a readable `reply`.

| Environment variable | Default                 | Purpose                    |
|----------------------|-------------------------|----------------------------|
| `ANTHROPIC_API_KEY`  | none (required)         | Claude API key             |
| `ANTHROPIC_MODEL`    | `claude-haiku-5-5`      | Claude model to use        |
| `MCP_SERVER_URL`     | `http://localhost:8080` | Where `mcp-server` runs    |

`mcp-client` logs to the console and to `mcp-client/logs/mcp-client.log`, which is git-ignored. For every Claude API call it logs:

- the endpoint, HTTP status and duration, e.g. `<-- 200 POST https://api.anthropic.com/v1/messages (755 ms)`. The API key is never logged.
- the request and response content: messages, tool calls, tool results and token usage. This includes what users type. To turn it off, set `logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=INFO` in `mcp-client/src/main/resources/application.properties`.

`mcp-client` starts even when `mcp-server` is down, and connects on the first chat message. If `mcp-server` or the Claude API can't be reached, the chat shows a friendly error and the details go to the `mcp-client` log.

## Chat UI

`chat-ui` is an Angular 20 app (standalone components, signals, zoneless) in [chat-ui/](chat-ui/):

| File | Purpose |
|------|---------|
| [chat.service.ts](chat-ui/src/app/chat/chat.service.ts) | Posts to `/api/chat` and turns HTTP errors into messages for the user |
| [chat.ts](chat-ui/src/app/chat/chat.ts) | Chat screen: message list, sample questions, "Thinking..." while waiting |
| [proxy.conf.json](chat-ui/proxy.conf.json) | Dev-server proxy from `/api` to `mcp-client` on port 8082 |

The proxy keeps the browser on one origin, so the session cookie that holds the conversation works without CORS. If `mcp-client` runs elsewhere, change `target` in `proxy.conf.json`.

`npm test` runs the unit tests in headless Chrome. Without Chrome, point `CHROME_BIN` at Edge, e.g. `CHROME_BIN="C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe" npm test -- --watch=false --browsers=ChromeHeadless`.

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
