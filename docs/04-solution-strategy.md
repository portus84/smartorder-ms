# 4. Solution Strategy

- Design: separate Spring Boot services for bounded contexts (order and inventory).
- Communication: synchronous HTTP between gateway and services; asynchronous events via RabbitMQ for domain notification/decoupling.
- Data: each service uses MongoDB for persistence (document model).
- Local dev: Docker Compose files (docker/) provide a full local stack for development, tests and quick demos.
- Observability: instrument services and expose metrics for Prometheus scraping; dashboards provided via Grafana.

Trade-offs and rationale
- MongoDB for flexible schema and fast iteration; trade-off: eventual consistency and schema management.
- Docker Compose chosen for low friction; trade-off: differences from cloud orchestration in production.