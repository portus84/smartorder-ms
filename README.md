# SmartOrder — Distributed Order Management System

[![Build](https://github.com/portus84/smartorder/actions/workflows/ci.yml/badge.svg)](https://github.com/portus84/smartorder/actions)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Overview
**SmartOrder** is a distributed order and inventory management platform built with **Spring Boot**, **Kafka**, and **MongoDB** (order-service).  
The goal is to demonstrate enterprise-level architectural design and clean modular development.

**SmartOrder** is a monorepo demo of order and inventory management, showcasing Spring Boot architecture, messaging with Kafka, modular project design, and a mix of SQL/NoSQL persistence depending on the service.

---

## Architecture
```
                        ┌──────────────────────┐
                        │ API Gateway          │
                        │ (Spring Cloud GW)    │
                        └─────────┬────────────┘
                                  │
          ┌───────────────────────┼────────────────────────┐
          │                                                │
┌─────────▼──────────────┐                      ┌──────────▼──────────────┐
│ order-service          │                      │ inventory-service       │
│ REST API               │◄───────Kafka────────►│ Kafka consumer          │
│ (uses MongoDB)         │                      │ DB + stock management   │
└─────────┬──────────────┘                      └──────────┬──────────────┘
          │                                               │
          └──────────────────────────────┬────────────────┘
                                         │
                                  ┌──────▼───────┐
                                  │ Databases     │
                                  │ (MongoDB /    │
                                  │  PostgreSQL)  │
                                  └──────────────┘
```

---

## Tech Stack

- **Java 21**, Spring Boot (3.3+ / 4.x compatible)
- **Kafka** for messaging (event-driven communication between services)
- **MongoDB** for the order-service (other services may use SQL DBs such as PostgreSQL)
- **SQL/NoSQL** databases depending on the service
- **Spring Boot caching** (service-level caches used; implementation configurable)
- **Docker Compose** for local environment

---

## Run Locally
```bash
# Start required containers (see docker/ directory for compose files)
docker compose up -d
mvn clean package
```

To run a service locally:
```bash
cd services/order-service
mvn spring-boot:run
```

Note: the order-service uses MongoDB (embedded or containerized) and relies on Spring Cache annotations for caching. Check docker/ for the docker-compose files that start monitoring and persistence dependencies.

---

## Coding Standards

This project follows established coding and commit guidelines to ensure consistency and maintainability.

### Java Code Style

The codebase adheres to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) to maintain consistent formatting, naming conventions, and best practices across all modules.

### Commit Messages

All Git commits follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) standard, enabling semantic versioning, automated changelogs, and a clean commit history.

## Author
**Francesco Portus**  
Software Architect / Senior Java Developer  
[LinkedIn](https://www.linkedin.com/in/francesco-portus) | [GitHub](https://github.com/portus84)
