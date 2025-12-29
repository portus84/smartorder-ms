# SmartOrder Microservices Platform

SmartOrder is a **microservices-based reference platform** built with **Spring Boot and Spring Cloud**, designed to demonstrate a **production-ready architecture** including service discovery, API Gateway, messaging, observability, monitoring, and local development tooling via Docker Compose.

The project emphasizes **clean architecture**, **event-driven communication**, **cloud-native patterns**, and **developer experience**.

---
## 🎯 Project Goals

- Provide a realistic microservices reference architecture
- Enable one-command local startup
- Showcase cloud-native and observability-first design
- Serve as a learning and experimentation platform

---
## 🧱 Architecture Overview

The platform is composed of:

- **Spring Cloud Gateway** as the API Gateway
- **Multiple Spring Boot microservices**
- **RabbitMQ** for asynchronous messaging
- **Consul** for service discovery and configuration
- **MongoDB** as the primary datastore
- **Docker & Docker Compose** for local orchestration
- **Full observability stack** (Prometheus, Grafana, InfluxDB, Dozzle, etc.)

The system follows **Domain-Driven Design (DDD)** and **REST + HATEOAS** principles.

---

## 📦 Microservices

### Gateway
- **Spring Cloud Gateway**
- Dynamic routing via Consul
- Circuit breaker fallback endpoints
- CORS configuration
- Central entry point for all APIs

### Business Services
Each service is:
- A standalone **Spring Boot application**
- Registered to **Consul**
- Exposing REST APIs with **Spring HATEOAS**
- Instrumented with **Micrometer**

Services include:
- Order Service
- Inventory Service
- Product Service
- (others depending on branch evolution)

---

## 🔄 Communication

### Synchronous
- REST over HTTP
- Gateway → Services
- HATEOAS-enabled responses

### Asynchronous
- **RabbitMQ**
- Event-based messaging
- Decoupled service interactions
- Prepared for CQRS / eventual consistency patterns

> Kafka is intentionally **not used** in this project. RabbitMQ was chosen for simplicity, local development, and classic messaging semantics.

---

## 🧠 Service Discovery & Configuration

### Consul
- Service registration
- Health checks
- Configuration management
- Centralized discovery for Gateway routing

All services auto-register themselves to Consul at startup.

---

## 🗄️ Data Layer

### MongoDB
- Used by business services
- Dockerized
- Schema-less persistence
- Indexing configured per service responsibility

---

## 📊 Observability & Monitoring

The project includes a **complete observability stack**, fully dockerized.

### Prometheus
- Metrics scraping via Micrometer
- JVM metrics
- HTTP metrics
- Custom application metrics

### Grafana
- Pre-provisioned dashboards:
    - JVM Micrometer Dashboard
    - MongoDB Dashboard
    - JMeter Load Testing Dashboard
- Auto-loaded dashboards via provisioning
- Ready-to-use visualization layer

### InfluxDB
- Time Series Database (TSDB) for storing high-frequency data like metrics, events, and logs.
- Query languages: InfluxQL (SQL-like) and Flux for advanced analytics.
- Use cases & advantages: Fast read/write, time-based aggregations, retention policies, integrates easily with Grafana and monitoring tools.

### Dozzle
- Real-time Docker log viewer
- Centralized log streaming
- Useful for local debugging

### Dashy
- Unified developer dashboard
- Entry point to all tools (Grafana, Prometheus, Consul, InfluxDB, etc.)

---

## 🐳 Docker & Local Development

The `docker/` directory is **highly structured** and represents a key strength of this repository.

### Dockerized Components
- Gateway
- All microservices
- RabbitMQ
- MongoDB
- Consul
- Prometheus
- Grafana
- InfluxDB
- Dozzle
- Dashy

### Docker Compose
- Multi-compose setup
- Config services separated from business services
- Reproducible local environment
- Zero external dependencies required


### 🐳 Docker Structure

The Docker setup is a core part of the project, not an afterthought.
```
docker
├── config-services
│   ├── dashy
│   ├── grafana
│   ├── influxdb
│   ├── jmeter
│   ├── prometheus
├── docker-compose.all.yml
├── docker-compose.monitoring.yml
├── docker-compose.persistence.yml
```
docker-compose.all.yml orchestrates **the entire ecosystem.**

## 🚀 How to Run the Platform

### Prerequisites
- Docker
- Docker Compose (v2)

### Start the entire platform

The **whole SmartOrder platform** (infrastructure + services + observability) can be started using:

```bash
docker-compose -f docker-compose.all.yml up -d
```

## 👤 Author

Francesco Portus
Software Architect / Solution Architect
