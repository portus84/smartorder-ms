# Architecture documentation (arc42) for smartorder-ms

This folder contains arc42-based architecture documentation and PlantUML diagrams for the smartorder-ms project.

How the documentation is organized
- docs/01-introduction.md — system goals and scope
- docs/02-constraints.md — technical and organizational constraints
- docs/03-context.md — system context and external actors (with references)
- docs/04-solution-strategy.md — high-level architecture and trade-offs
- docs/05-building-blocks.md — static structure, C4 container/component diagrams
- docs/06-runtime.md — runtime scenarios and sequence diagrams
- docs/07-deployment.md — deployment and Docker Compose mapping
- docs/08-crosscutting.md — cross-cutting concerns and technologies
- docs/09-decisions.md — architectural decisions
- docs/10-quality.md — quality goals and scenarios
- docs/11-risks.md — known risks and technical debt
- docs/12-glossary.md — terms and acronyms
- docs/references.md — file references in the repo
- docs/diagrams/*.puml — PlantUML diagrams (C4 and sequences)

Rendering diagrams
- Install PlantUML (and Graphviz). From repository root:
  - plantuml -tpng docs/diagrams/*.puml
- Or use online PlantUML renderers that accept the .puml sources.

Purpose
- Provide architects, developers and operators an immediate reference to system structure, runtime scenarios and local deployment topology.