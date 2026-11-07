# Course requirements (summary of the official project statement)

Summary written by the team from the course project statement. For the exact wording,
see the original course document.

## High-level requirements R1–R16

| ID | Title | Summary |
|----|-------|---------|
| R1 | Event Management | Authorized users create, modify, publish and manage events (title, description, date/time, location, organizing organization, capacity, registration information). |
| R2 | Organization and User Management | Manage student organizations and their members; users have different responsibilities and permissions; authentication, authorization and access control. |
| R3 | Participant Registration | Eligible users discover events and register; registration information is managed and business rules (capacity, registration status) are enforced. |
| R4 | Event Lifecycle | Support the lifecycle from creation and planning, through publication and registration, to the event itself and post-event activities; workflows and rules defined by the team. |
| R5 | Event Organization | Support organizers in preparing and managing events (responsibilities, event information, monitoring registrations, communication, attendance), justified by identified user needs. |
| R6 | Search and Information Access | Users find relevant events and access the information needed to decide whether to participate. |
| R7 | Data Persistence | Persistent storage; data model represents entities, relationships, constraints and business rules. |
| R8 | Enterprise Architecture | Multi-layer system separating presentation, business logic, persistence and external interfaces; decisions and technologies documented and justified. |
| R9 | APIs and Integration | Well-defined, documented APIs between components and, where appropriate, external systems. |
| R10 | User Interface | Usable interface for the main user categories, matching the documented workflows and use cases; important workflows validated with users. |
| R11 | Testing | Automated tests appropriate to the system: unit tests and, where appropriate, other test levels. |
| R12 | Deployment | Deployable and runnable outside the development environment; documented deployment approach and demonstration. |
| R13 | GenAI-Assisted Development | GenAI used throughout development as an engineering tool, with relevant uses documented. |
| R14 | Human Oversight and Engineering Judgment | Visible human-in-the-loop process: context, prompts, evaluation, corrections, rejections, validation. |
| R15 | Traceability | Requirements, use cases, workflows, architectural decisions, implementation and tests are connected. |
| R16 | Team-Based Development | Developed collaboratively; each student owns specific artifacts and at least one cross-layer workflow. |

## Mid-term deliverable

| Part | Content | Where in the repo |
|------|---------|-------------------|
| A. Code | Project structure and development environment configuration; initial backend and frontend; database configuration and initial data model; at least the main MVP workflows implemented | repository (backend/, frontend/, docker-compose.yml) |
| B. Documentation | 1. Problem statement and project scope; 2. High-level requirements; 3. Actors and roles; 4. Use cases; 5. Main application workflows | docs/midterm/01–05 |
| C. UX/User research | Short research note based on interviews with real users; main findings; changes to initial assumptions caused by the interviews; main product flows | docs/midterm/06-ux-research.md |
| D. GenAI declaration | Tools/models used; purpose of each; artifacts generated or modified with GenAI; how AI-generated code/docs were verified; examples of human decisions that materially changed the AI-generated solution | docs/midterm/07-genai-report.md |
| E. Individual contribution declaration | Per student: owned artifact(s); owned workflow; implemented functionality; documentation produced; tests written; architectural/design decisions contributed; GenAI-related work; others | docs/midterm/08-individual-contributions.md |
