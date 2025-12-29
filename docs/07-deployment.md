# 7. Deployment and Local Development

Local compose files
- docker/docker-compose.all.yml — top-level compose referencing persistence, monitoring and services
  - https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.all.yml
- docker/docker-compose.persistence.yml — MongoDB + exporter
  - https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.persistence.yml
- docker/docker-compose.monitoring.yml — monitoring stack
  - https://github.com/portus84/smartorder-ms/blob/develop/docker/docker-compose.monitoring.yml
- per-service compose files:
  - gateway/docker/docker-compose.yml (included by all.yml)
  - services/order-service/api/docker/docker-compose.yml (included by all.yml)
  - services/inventory-service/api/docker/docker-compose.yml (included by all.yml)

Start full local environment (from repo root):
- docker compose -f docker/docker-compose.all.yml up

Notes about persistence in compose
- MongoDB in compose uses tmpfs for /data/db (ephemeral local storage in this compose).
- RabbitMQ exposes management UI on port 15672.