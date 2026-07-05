<!--
Sync Impact Report
Version change: [TEMPLATE] → 1.0.0 (initial ratification)
Modified principles: N/A (first concrete adoption of the template)
Added sections:
  - Core Principles: I. Clean Architecture (Robert Martin), II. BDD Testing Discipline,
    III. SOLID / YAGNI / DRY, IV. API-First with OpenAPI Contracts, V. Coverage Quality Gates (JaCoCo)
  - Technology & Tooling Constraints
  - Development Workflow & Quality Gates
  - Governance
Removed sections: None (template placeholders replaced)
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ no changes required (Constitution Check gate is generic and
    already reads from this file)
  - .specify/templates/spec-template.md ✅ no changes required (Acceptance Scenarios are already
    Given/When/Then, compatible with BDD principle)
  - .specify/templates/tasks-template.md ✅ no changes required (already supports contract/integration/unit
    test phases per user story)
  - .specify/templates/checklist-template.md ⚠ pending manual review (not inspected in this pass)
Follow-up TODOs: None
-->

# citasalud-service Constitution

## Core Principles

### I. Clean Architecture (Robert C. Martin)
Every feature MUST be structured in concentric layers — Entities, Use Cases,
Interface Adapters, and Frameworks/Drivers — with dependencies pointing strictly
inward. Domain and use-case layers MUST NOT import framework, persistence, or web
types (e.g., no JPA/Spring annotations on domain entities or use-case interfaces).
Boundaries between layers MUST be expressed as interfaces (ports) defined by the
inner layer and implemented by the outer layer (adapters/gateways). Frameworks
(Spring, JPA, web) are treated as replaceable details, never as the core of the
design.

**Rationale**: Isolating business rules from delivery mechanisms keeps the domain
testable without a container, allows infrastructure (database, web framework) to
be swapped with minimal churn, and prevents architectural erosion as the codebase
grows.

### II. BDD Testing Discipline (Unit, Integration, Functional)
Every user-facing behavior MUST be captured as Given/When/Then scenarios before
implementation. Three test levels are mandatory for every feature:
unit tests (domain/use-case logic, no framework, no I/O), integration tests
(persistence, messaging, and other adapter boundaries against real or
containerized dependencies), and functional/acceptance tests (end-to-end,
exercising the feature through its API contract, expressed as BDD scenarios).
Tests MUST be written from the Given/When/Then scenarios in the spec before or
alongside implementation; a scenario without a corresponding automated test is
not considered done.

**Rationale**: BDD keeps tests traceable to business intent, prevents drift
between specification and implementation, and ensures each architectural layer
(unit), each boundary (integration), and each user journey (functional) is
independently verified.

### III. SOLID, YAGNI, DRY
All code MUST follow the SOLID principles (Single Responsibility, Open/Closed,
Liskov Substitution, Interface Segregation, Dependency Inversion). Speculative
abstractions, configuration options, or extension points MUST NOT be added
without a current, concrete requirement (YAGNI). Duplicated logic MUST be
consolidated once a genuine repeated pattern exists (DRY) — but a single or
incidental duplication does not justify a premature abstraction. When SOLID and
YAGNI appear to conflict (e.g., adding an interface "just in case"), YAGNI wins
unless a second concrete consumer already exists.

**Rationale**: These practices keep the Clean Architecture layers thin, testable,
and free of unnecessary indirection, while still respecting object-oriented
design discipline where it earns its cost.

### IV. API-First with OpenAPI Contracts
Every API MUST be designed contract-first: an OpenAPI (3.x) specification MUST
exist and be reviewed/approved before controller or client code is written.
Server interfaces (and, where applicable, client stubs) MUST be generated from
the OpenAPI contract using `openapi-generator`; hand-written interface code that
duplicates what the generator produces is prohibited. Changes to behavior that
affect the API MUST start with a contract change, followed by regeneration,
followed by implementation — never the reverse.

**Rationale**: A single source of truth for the API prevents drift between
documentation and implementation, enables consumer-driven contract testing, and
lets client teams work in parallel against a stable, generated interface.

### V. Coverage Quality Gates (JaCoCo)
JaCoCo MUST be configured to produce coverage reports on every build. Per-class
line coverage MUST be greater than 80%, and the global project coverage MUST be
greater than or equal to 80%. The build MUST fail (via JaCoCo coverage
verification rules) when either threshold is not met. Coverage gates apply to
production code in all Clean Architecture layers; generated code (e.g.,
openapi-generator output) MAY be excluded from coverage calculation.

**Rationale**: Enforced, automated coverage thresholds catch untested classes
before merge and keep the BDD test discipline (Principle II) honest with an
objective, tool-verified metric rather than self-reported testing effort.

## Technology & Tooling Constraints

- **Build tooling**: Gradle is the build system of record; JaCoCo MUST be applied
  as a Gradle plugin with a coverage verification task wired into `check`.
- **API generation**: `openapi-generator` (Gradle plugin or CLI) MUST be used to
  generate server interfaces/models from the OpenAPI contract stored in the
  repository (e.g., under `src/main/resources/openapi/`).
- **Test frameworks**: BDD-style scenarios MUST map to the project's test stack
  (e.g., JUnit 5 + a BDD-flavored library such as Cucumber, or spec-style naming
  that mirrors Given/When/Then) for unit, integration, and functional suites.
- **Layering**: Package structure MUST make Clean Architecture layers explicit
  (e.g., `domain`/`usecase`/`adapter`/`infrastructure` or equivalent), so layer
  violations are visible in code review without tooling.

## Development Workflow & Quality Gates

- Every feature's plan MUST include a Constitution Check confirming: layering
  respects Clean Architecture, BDD scenarios exist for the feature's user
  stories, an OpenAPI contract change is included when the API surface changes,
  and coverage gates are expected to hold.
- Pull requests MUST NOT merge if: the OpenAPI contract was not updated ahead of
  implementation, any mandatory test level (unit/integration/functional) is
  missing for new behavior, or JaCoCo reports per-class or global coverage below
  the 80% thresholds.
- Reviewers MUST reject speculative abstractions or duplicated logic introduced
  without justification, per Principle III.

## Governance

This constitution supersedes ad-hoc conventions and prior undocumented practice
for this repository. Amendments require: (1) a documented rationale for the
change, (2) a version bump following semantic versioning (MAJOR for backward
incompatible principle removal/redefinition, MINOR for new principles or
materially expanded guidance, PATCH for clarifications/wording), and (3)
propagation of any impacted guidance to `.specify/templates/*` and agent context
files in the same change. All PRs and code reviews MUST verify compliance with
the Core Principles above; any deviation MUST be justified in the plan's
Complexity Tracking section. Complexity or deviation without a documented,
approved justification MUST be rejected.

**Version**: 1.0.0 | **Ratified**: 2026-07-04 | **Last Amended**: 2026-07-04
