# mcp

Spring Boot services (Java 17).

| Service         | Port | Endpoints                                                       |
|-----------------|------|-----------------------------------------------------------------|
| `mcp-server`    | 8080 | `GET /hello`, MCP (Streamable HTTP) at `/mcp`                   |
| `order-service` | 8081 | `GET /orders`, `GET /orders/{orderId}/status`                   |

## Run locally

Start each service from its own directory:

```sh
cd order-service && ./mvnw spring-boot:run
cd mcp-server && ./mvnw spring-boot:run
```

Run tests with `./mvnw test` in either directory.

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
