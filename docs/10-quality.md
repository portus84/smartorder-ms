# 10. Quality Goals & Scenarios

Primary quality attributes
- Availability: tolerate single-service restarts; broker or DB outage should degrade gracefully
- Performance: keep median and 95th percentile response times within targets (use jmeter/ for load tests)
- Operability: observability through Prometheus/Grafana and health checks

Quality scenarios
- Q1: When a create order request arrives, the system should return 201 within X ms under normal load (define X via benchmarking)
- Q2: If RabbitMQ is down, order creation must still persist locally and retry publishing (design pattern)