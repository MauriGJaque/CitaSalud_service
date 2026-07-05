# Implementation Plan: Reserva de Cita en Línea 24/7

**Branch**: `001-reserva-cita-online` | **Date**: 2026-07-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-reserva-cita-online/spec.md`

## Summary

Permitir que un paciente reserve una cita médica en línea en cualquier
momento (24/7), eligiendo médico, fecha y hora entre franjas disponibles,
con confirmación registrada de forma transaccional y notificación al
paciente por WhatsApp. El sistema debe impedir dobles reservas sobre la
misma franja bajo concurrencia y mantener la cita válida aunque la
notificación falle. Se implementa como un caso de uso de dominio
(`ReservarCita`) aislado de frameworks, expuesto vía un endpoint REST
generado a partir de un contrato OpenAPI, persistido con Spring Data JPA, y
notificado a través de un puerto de notificación desacoplado del proveedor
concreto de WhatsApp.

## Technical Context

**Language/Version**: Java 25 (Gradle toolchain), Spring Boot 4.1.0

**Primary Dependencies**: Spring Web MVC, Spring Data JPA, Lombok (ya
presentes en `build.gradle`); a incorporar: `openapi-generator-gradle-plugin`
(generación de interfaces del contrato), `jacoco` (Gradle plugin, cobertura),
Cucumber-JVM + `cucumber-junit-platform-engine` (BDD funcional), JUnit 5 +
AssertJ/Mockito (unitario), `spring-boot-starter-data-jpa-test` /
`spring-boot-starter-webmvc-test` (ya presentes, integración)

**Storage**: Relacional vía Spring Data JPA; H2 para desarrollo/pruebas
(ya configurado en `build.gradle`); esquema con restricción de unicidad
sobre (médico, franja horaria) para impedir dobles reservas

**Testing**: JUnit 5 (unitario, capas dominio/caso de uso sin Spring
context), `@DataJpaTest`/`@WebMvcTest` (integración, capas adaptador),
Cucumber-JVM sobre `@SpringBootTest` (funcional/aceptación, extremo a
extremo contra el contrato OpenAPI)

**Target Platform**: Servicio backend JVM desplegado en servidor Linux
(contenedor), consumido por clientes web/móvil vía HTTP/REST

**Project Type**: Web-service (proyecto único Spring Boot, sin frontend en
este repositorio)

**Performance Goals**: Confirmación de reserva persistida y notificación
por WhatsApp disparada en menos de 1 minuto (SC-002); flujo completo de
reserva completable por el paciente en menos de 3 minutos (SC-001) — meta
de experiencia de usuario, no un SLA de latencia de API específico

**Constraints**: 0% de dobles reservas sobre la misma franja horaria bajo
intentos concurrentes (SC-003); la reserva debe permanecer válida aunque
falle el envío de la notificación (FR-009); solo deben ofrecerse franjas
futuras y dentro del horario del médico (FR-007)

**Scale/Scope**: Alcance de una sola historia de usuario (US-01, épica
E-01): flujo de reserva de cita para un paciente autenticado; volumen
esperado acorde a una clínica/red de consultorios, no a escala masiva

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Estado |
|-----------|------------|--------|
| I. Clean Architecture | El diseño separa `domain` (Paciente, Médico, Cita, FranjaHoraria + puertos), `application` (caso de uso `ReservarCita`), `adapter` (entrada web, salida persistencia/notificación) e `infrastructure` (config Spring). El dominio no depende de JPA/Spring. | PASS |
| II. BDD (unit/integration/functional) | Se planifican los tres niveles: JUnit puro para dominio/caso de uso, `@DataJpaTest`/`@WebMvcTest` para adaptadores, Cucumber + `@SpringBootTest` para los escenarios Given/When/Then del spec. | PASS |
| III. SOLID/YAGNI/DRY | Un único caso de uso con puertos segregados (`CitaRepositoryPort`, `NotificacionPort`); no se introducen abstracciones sin un segundo consumidor real. | PASS |
| IV. API-First (OpenAPI + openapi-generator) | Contrato `contracts/citas-api.yaml` se define antes que el controlador; las interfaces del controlador se generan con `openapi-generator-gradle-plugin`. | PASS |
| V. Cobertura JaCoCo (>80% clase, ≥80% global) | Se añade el plugin JaCoCo con regla de verificación bloqueante en `check`; código generado por `openapi-generator` se excluye del cálculo. | PASS |

No se identifican violaciones; no aplica Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-reserva-cita-online/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
│   └── citas-api.yaml
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/org/ups/citasalud/
├── booking/
│   ├── domain/
│   │   ├── model/            # Paciente, Medico, Cita, FranjaHoraria (POJOs, sin anotaciones de framework)
│   │   └── port/              # CitaRepositoryPort, NotificacionPort (interfaces)
│   ├── application/
│   │   └── usecase/           # ReservarCitaUseCase, ConsultarDisponibilidadUseCase, SeleccionarFranjaUseCase, LiberarFranjaRetenidaUseCase (FR-010)
│   └── adapter/
│       ├── in/web/            # Controlador que implementa la interfaz generada por openapi-generator, mappers DTO<->dominio
│       └── out/
│           ├── persistence/    # Entidades JPA, Spring Data repositories, adapter que implementa CitaRepositoryPort
│           └── notification/   # Adapter que implementa NotificacionPort (proveedor WhatsApp)
├── config/                     # Configuración Spring (beans, manejo de excepciones)
└── CitasaludServiceApplication.java

src/main/resources/
├── openapi/
│   └── citas-api.yaml          # Contrato fuente (copiado/enlazado desde contracts/)
├── db/
│   ├── schema.sql               # DDL: tablas paciente, medico, franja_horaria, cita + restricción de unicidad (medico_id, fecha_hora_inicio)
│   └── data.sql                 # Datos precargados: médicos y franjas horarias de ejemplo para desarrollo/demo/pruebas
└── application.yaml             # `spring.sql.init.mode=always` y rutas de schema.sql/data.sql

src/test/java/org/ups/citasalud/booking/
├── unit/                       # Dominio y casos de uso, sin Spring context
├── integration/                # Adaptadores (@DataJpaTest, @WebMvcTest)
└── functional/                 # Cucumber: features/ + step definitions, @SpringBootTest
```

**Structure Decision**: Proyecto único (no hay frontend en este repositorio).
Se introduce un subpaquete `booking` bajo `org.ups.citasalud` que aplica
Clean Architecture (dominio → aplicación → adaptadores → infraestructura),
manteniendo el punto de entrada `CitasaludServiceApplication` existente. El
contrato OpenAPI vive en `src/main/resources/openapi/` (consistente con el
archivo ya presente `dashboard-api.yaml` en proyectos hermanos de este
workspace) y alimenta `openapi-generator` para producir las interfaces de
controlador que `adapter/in/web` implementa. El esquema de base de datos y
los datos precargados viven en `src/main/resources/db/` (`schema.sql` +
`data.sql`), inicializados automáticamente por Spring Boot sobre H2 al
arrancar — ver [research.md](./research.md#6-esquema-y-datos-precargados-en-resourcesdb).

## Complexity Tracking

> No hay violaciones de la Constitution Check; esta sección no aplica.
