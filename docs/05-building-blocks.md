# 5. Building Block View (Static Structure)

## High-level containers

- gateway — Spring Boot app acting as API entrypoint
- order-service — Spring Boot app: REST API, persistence layer, event publishing to RabbitMQ
- inventory-service — Spring Boot app: REST API, consumes events to update inventory
- rabbitmq — AMQP broker
- mongo — document DB
- consul — dev config/discovery
- monitoring stack — prometheus, grafana, influxdb, exporters

## Componentization (example — order-service)

- API layer: controllers handling HTTP
- Business layer: services implementing use-cases (create order, cancel order)
- Persistence: Spring Data repositories backed by MongoDB
- Integration: RabbitMQ publisher/subscriber component
- Shared DTOs / events: services/events-model and services/commons modules

## Diagrams

|               *System Context (C4 Context)*                |
|:----------------------------------------------------------:|
| ![System Context (C4 Context](./docs/diagrams/context.svg) |

|                 *Container Level C4 diagram*                 |
|:------------------------------------------------------------:|
| ![Container Level C4 diagram](./docs/diagrams/container.svg) |

|                        *Component Diagram for Order Service*                        |
|:-----------------------------------------------------------------------------------:|
| ![Component Diagram for Order Service](./docs/diagrams/component-order-service.svg) |
