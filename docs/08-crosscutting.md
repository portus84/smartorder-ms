# 8. Cross-cutting Concepts

Technologies
- Java + Spring Boot (check individual pom.xmls for exact versions)
- Maven multi-module build (root pom.xml)
- RabbitMQ (AMQP) — event-driven integration
- MongoDB — document storage
- Consul — dev config
- Prometheus / Grafana / InfluxDB — metrics & visualization

Observability and logging
- Expect standard actuator endpoints and Prometheus metrics endpoint
- Monitoring compose provisions exporters and dashboards; see docker/config-services/

Security
- Gateway is appropriate place for authentication & authorization. No global security configuration is enforced by the docs — consult gateway module for actual implementation details.