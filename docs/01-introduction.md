# 1. Introduction and Goals

System name: smartorder-ms

Short description:
A small microservices-based platform for order and inventory management. The codebase is organized as a Maven multi-module project; services are Spring Boot applications with a gateway and local developer orchestration using Docker Compose.

Primary goals
- Provide a maintainable and testable microservice architecture for orders and inventory.
- Support asynchronous decoupling using RabbitMQ.
- Provide an easy local development environment using Docker Compose and local monitoring.
- Observe and measure system behavior with Prometheus / Grafana / InfluxDB.

Stakeholders
- Developers, QA engineers, DevOps / operators, product owners.