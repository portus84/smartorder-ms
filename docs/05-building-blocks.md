# 5. Building Block View (Static Structure)

High-level containers
- gateway — Spring Boot app acting as API entrypoint
- order-service — Spring Boot app: REST API, persistence layer, event publishing to RabbitMQ
- inventory-service — Spring Boot app: REST API, consumes events to update inventory
- rabbitmq — AMQP broker
- mongo — document DB
- consul — dev config/discovery
- monitoring stack — prometheus, grafana, influxdb, exporters

Componentization (example — order-service)
- API layer: controllers handling HTTP
- Business layer: services implementing use-cases (create order, cancel order)
- Persistence: Spring Data repositories backed by MongoDB
- Integration: RabbitMQ publisher/subscriber component
- Shared DTOs / events: services/events-model and services/commons modules

Diagrams
- docs/diagrams/context.puml — system context (C4 Context)
- docs/diagrams/container.puml — container-level C4 diagram
- docs/diagrams/component-order-service.puml — component diagram for order service