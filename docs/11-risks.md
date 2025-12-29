# 11. Risks & Technical Debt

- Eventual consistency can cause complicated reconciliation flows.
- Docker Compose environment may hide production orchestration differences.
- No Kubernetes manifests or CI/CD pipeline included (adds manual operational effort).
- Verify version pinning for docker images in docker/ to avoid unexpected upgrades.