# 6. Runtime View (Scenarios)

## Primary scenario: Create Order

1. Client -> Gateway: HTTP POST /orders
2. Gateway -> Order service: HTTP POST
3. Order service validates and persists order in MongoDB
4. Order service publishes OrderCreated event to RabbitMQ
5. Inventory service consumes OrderCreated, reserves items and updates MongoDB
6. Inventory service may emit InventoryUpdated event

## Sequence diagram

|                       *Sequence diagram*                        |
|:---------------------------------------------------------------:|
| ![Sequence diagram](./docs/diagrams//sequence-order-create.svg) |

## Operational notes

- Services should expose health and metrics endpoints (Actuator).
- Compose uses service depends_on conditions for a local ordering, but robust services must handle restarts and backoff.
