System Architecture Documentation
1. Introduction and Goals
   Briefly describe the system and its primary goals.
2. Architecture Constraints
   List technical, legal, or organizational constraints.
3. Context and Scope
   3.1 Business Context
   3.2 Technical Context (C4 Context)
   ```puml
   @startuml
   !include raw.githubusercontent.com
   Person(user, "User", "Primary user")
   System(system, "System Name", "The software system")
   System_Ext(ext, "External API", "Third-party service")
   Rel(user, system, "Uses", "HTTPS")
   Rel(system, ext, "Calls", "JSON/REST")
   @endum
   ```
4. Solution Strategy
   Key technical decisions.
5. Building Block View
   5.1 Level 1: System Decomposition (C4 Container)
   ```puml
   @startuml
   !include raw.githubusercontent.com
   Container(web, "Web App", "Tech Stack", "UI")
   Container(api, "API", "Tech Stack", "Logic")
   ContainerDb(db, "DB", "Tech Stack", "Data")
   Rel(web, api, "Uses", "HTTPS")
   Rel(api, db, "Writes", "SQL")
   @endum
   ```
6. Runtime View
   6.1 Core Process Sequence
   ```puml
   @startuml
   actor User
   User -> API : Request
   API -> DB : Query
   DB --> API : Data
   API --> User : Response
   @endum
   ```
7. Deployment View
8. Cross-cutting Concepts
9. Architecture Decisions (ADR)
10. Quality Requirements
11. Risks and Technical Debt
12. Glossary
