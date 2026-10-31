# mcp

Spring Boot services (Java 17).

| Service         | Port | Endpoints                                        |
|-----------------|------|--------------------------------------------------|
| `mcp-server`    | 8080 | `GET /hello`                                     |
| `order-service` | 8081 | `GET /orders`, `GET /orders/{orderId}/status`    |

## Run locally

Start each service from its own directory:

```sh
cd mcp-server && ./mvnw spring-boot:run
cd order-service && ./mvnw spring-boot:run
```

Run tests with `./mvnw test` in either directory.
