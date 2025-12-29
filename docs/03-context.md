# 3. System Context and Scope

In-scope
- API Gateway (gateway/)
- Order service (services/order-service)
- Inventory service (services/inventory-service)
- Dev infra: RabbitMQ, MongoDB, Consul, Prometheus/Grafana/InfluxDB

Out-of-scope
- Production orchestration (no Kubernetes manifests)
- External payment/third-party integrations (not present)

External systems and actors
- Client (web/mobile)
- RabbitMQ — AMQP broker for domain events
- MongoDB — document database for services
- Consul — config / development service discovery
- Monitoring stack (Prometheus, Grafana, InfluxDB)

Key source references (examples)
- Gateway application: https://github.com/portus84/smartorder-ms/blob/develop/gateway/src/main/java/it/portus/smartorder/gateway/GatewayApplication.java
- Order service main: https://github.com/portus84/smartorder-ms/blob/develop/services/order-service/api/src/main/java/it/portus/smartorder/ms/orderservice/api/Application.java
- Inventory service main: https://github.com/portus84/smartorder-ms/blob/develop/services/inventory-service/api/src/main/java/it/portus/smartorder/ms/invservice/api/Application.java
- Docker orchestration: https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.all.yml
- Persistence compose: https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.persistence.yml
- Monitoring compose: https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.monitoring.yml