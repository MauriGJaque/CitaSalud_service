---

description: "Task list for Reserva de Cita en Línea 24/7"
---

# Tasks: Reserva de Cita en Línea 24/7

**Input**: Design documents from `/specs/001-reserva-cita-online/`

**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/citas-api.yaml, quickstart.md

**Tests**: Incluidas y obligatorias — la Constitution (Principio II) exige
pruebas unitarias, de integración y funcionales (BDD) para toda historia
de usuario; no son opcionales en este proyecto.

**Organization**: Esta historia tiene una única user story (US1, P1), por
lo que las fases de Setup y Foundational entregan toda la infraestructura
compartida y la Fase 3 entrega US1 de punta a punta.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias)
- **[Story]**: US1 (única historia en este plan)
- Cada tarea incluye la ruta de archivo exacta

## Path Conventions

Proyecto único Spring Boot: `src/main/java/org/ups/citasalud/`,
`src/test/java/org/ups/citasalud/`, `src/test/resources/` (ver
`plan.md` → Project Structure).

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Preparar el build y el esqueleto de paquetes antes de tocar cualquier historia

- [X] T001 Añadir plugins `jacoco`, `org.openapi.generator` (openapi-generator-gradle-plugin) y dependencias de Cucumber (`io.cucumber:cucumber-java`, `io.cucumber:cucumber-junit-platform-engine`) en `build.gradle`
- [X] T002 [P] Configurar la tarea `openApiGenerate` en `build.gradle` para generar solo interfaces de servidor (`library=spring`, `interfaceOnly=true`) desde `src/main/resources/openapi/citas-api.yaml`
- [X] T003 [P] Configurar `jacocoTestCoverageVerification` en `build.gradle` con reglas `element=CLASS` (mínimo 0.80) y de bundle global (mínimo 0.80), excluyendo el paquete generado por openapi-generator, y enlazarla a la tarea `check`
- [X] T004 [P] Crear el esqueleto de paquetes vacíos `src/main/java/org/ups/citasalud/booking/{domain/model,domain/port,application/usecase,adapter/in/web,adapter/out/persistence,adapter/out/notification}`

**Checkpoint**: `./gradlew build` compila (sin lógica todavía) y genera el código del contrato OpenAPI

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modelos de dominio, puertos, esquema/datos y persistencia base que toda historia necesita

**⚠️ CRITICAL**: Ninguna tarea de US1 puede iniciarse hasta completar esta fase

- [X] T005 Crear los modelos de dominio `Paciente`, `Medico`, `FranjaHoraria` (con estados `DISPONIBLE`/`RETENIDA`/`OCUPADA` y campo `retenidaHasta`), `Cita` (POJOs sin anotaciones de framework, según `data-model.md`) en `src/main/java/org/ups/citasalud/booking/domain/model/`
- [X] T006 [P] Definir la interfaz `CitaRepositoryPort` (método `guardarSiFranjaDisponible`) en `src/main/java/org/ups/citasalud/booking/domain/port/CitaRepositoryPort.java`
- [X] T007 [P] Definir la interfaz `NotificacionPort` (método `enviarConfirmacion`) en `src/main/java/org/ups/citasalud/booking/domain/port/NotificacionPort.java`
- [X] T008 Crear el DDL del esquema (`paciente`, `medico`, `franja_horaria` incluyendo columnas `estado` y `retenida_hasta`, `cita`, con restricción de unicidad `(medico_id, fecha_hora_inicio)`) en `src/main/resources/db/schema.sql`, según `data-model.md` (depende de T005)
- [X] T009 [P] Crear datos precargados de ejemplo (médicos y franjas `DISPONIBLE`) en `src/main/resources/db/data.sql`, y configurar en `src/main/resources/application.yaml` la inicialización nativa de Spring (`spring.sql.init.mode=always`, `spring.jpa.hibernate.ddl-auto=validate`) apuntando a `db/schema.sql` y `db/data.sql` (depende de T008)
- [X] T010 Crear las entidades JPA `PacienteEntity`, `MedicoEntity`, `FranjaHorariaEntity`, `CitaEntity` mapeadas al esquema de T008 en `src/main/java/org/ups/citasalud/booking/adapter/out/persistence/` (depende de T005, T008)
- [X] T011 Crear los repositorios Spring Data JPA (`FranjaHorariaJpaRepository`, `CitaJpaRepository`) en `src/main/java/org/ups/citasalud/booking/adapter/out/persistence/` (depende de T010)
- [X] T012 Configurar manejo global de excepciones (`@ControllerAdvice`, mapeo a `Error` del contrato) en `src/main/java/org/ups/citasalud/config/ApiExceptionHandler.java`
- [X] T013 [P] Implementar `LiberarFranjaRetenidaUseCase` (marca `RETENIDA → DISPONIBLE` cuando `ahora > retenidaHasta` sin `Cita` asociada; verificación perezosa al consultar disponibilidad, según `research.md`) en `src/main/java/org/ups/citasalud/booking/application/usecase/LiberarFranjaRetenidaUseCase.java` (depende de T005, T006) — cubre FR-010

**Checkpoint**: Infraestructura lista (incluye esquema, datos precargados y liberación automática de franjas retenidas) — la implementación de US1 puede comenzar

---

## Phase 3: User Story 1 - Reserva de cita en línea 24/7 (Priority: P1) 🎯 MVP

**Goal**: Un paciente reserva una cita eligiendo médico, fecha y hora
disponibles; la cita queda registrada y se dispara una confirmación por
WhatsApp; una franja ya ocupada se rechaza e invita a elegir otra; una
franja retenida sin confirmar se libera a los 5 minutos.

**Independent Test**: Ejecutar los Escenarios 1, 2 y 3 de `quickstart.md`
contra el servicio levantado localmente (usando los datos precargados de
`data.sql`), sin depender de ninguna otra historia.

### Tests for User Story 1 (obligatorias por Constitution, Principio II)

> **NOTE**: Escribir estas pruebas primero y verificar que fallan antes de implementar

- [X] T014 [P] [US1] Test unitario de `ReservarCitaUseCase` (reserva exitosa, franja ocupada, sin Spring context) en `src/test/java/org/ups/citasalud/booking/unit/ReservarCitaUseCaseTest.java`
- [X] T015 [P] [US1] Test unitario: `ReservarCitaUseCase` mantiene la `Cita` en estado `CONFIRMADA` con `notificacionEnviada=false` cuando `NotificacionPort.enviarConfirmacion` falla (FR-009) en `src/test/java/org/ups/citasalud/booking/unit/ReservarCitaUseCaseNotificacionFallidaTest.java`
- [X] T016 [P] [US1] Test unitario de `LiberarFranjaRetenidaUseCase` (una franja `RETENIDA` vuelve a `DISPONIBLE` tras 5 minutos sin confirmar; FR-010) en `src/test/java/org/ups/citasalud/booking/unit/LiberarFranjaRetenidaUseCaseTest.java`
- [X] T017 [P] [US1] Test de integración `@DataJpaTest` que verifica la restricción de unicidad `(medico_id, fecha_hora_inicio)` en `src/test/java/org/ups/citasalud/booking/integration/CitaRepositoryAdapterIT.java`
- [X] T018 [P] [US1] Test de integración `@WebMvcTest`: `POST /api/v1/citas` con una franja pasada o fuera del horario del médico responde `422` (FR-007, Edge Case 4) en `src/test/java/org/ups/citasalud/booking/integration/ReservaCitaFranjaInvalidaControllerIT.java`
- [X] T019 [P] [US1] Test de integración `@WebMvcTest` del controlador `POST /api/v1/citas`, `GET /api/v1/medicos/{id}/franjas` y `POST /api/v1/franjas/{id}/seleccion` (códigos 200/201/409) en `src/test/java/org/ups/citasalud/booking/integration/ReservaCitaControllerIT.java`
- [X] T020 [US1] Test de integración/concurrencia: disparar dos solicitudes `POST /api/v1/citas` simultáneas sobre la misma `franjaHorariaId` y verificar que exactamente una responde `201` y la otra `409` (SC-003, FR-008) en `src/test/java/org/ups/citasalud/booking/integration/ReservaCitaConcurrenciaIT.java` (requiere el endpoint completo; se escribe ahora pero solo puede ejecutarse en verde tras T029)
- [X] T021 [P] [US1] Feature Cucumber con los tres Acceptance Scenarios del spec (reserva fuera de horario telefónico; franja ya ocupada; liberación de franja retenida tras 5 min), ejecutado contra los datos precargados de `data.sql` en `src/test/resources/features/reserva_cita.feature`
- [X] T022 [US1] Step definitions + runner `@SpringBootTest` para el feature anterior en `src/test/java/org/ups/citasalud/booking/functional/ReservaCitaStepDefinitions.java` (depende de T021)

### Implementation for User Story 1

- [X] T023 [P] [US1] Implementar `ReservarCitaUseCase` (valida franja futura/dentro de horario del médico, usa `CitaRepositoryPort` y `NotificacionPort`) en `src/main/java/org/ups/citasalud/booking/application/usecase/ReservarCitaUseCase.java` (depende de T005, T006, T007)
- [X] T024 [P] [US1] Implementar `ConsultarDisponibilidadUseCase` (lista franjas `DISPONIBLE` futuras de un médico, integrando la liberación perezosa de T013) en `src/main/java/org/ups/citasalud/booking/application/usecase/ConsultarDisponibilidadUseCase.java` (depende de T005, T006, T013)
- [X] T025 [P] [US1] Implementar `SeleccionarFranjaUseCase` (transición `DISPONIBLE → RETENIDA` con `retenidaHasta = ahora + 5min`, FR-010) en `src/main/java/org/ups/citasalud/booking/application/usecase/SeleccionarFranjaUseCase.java` (depende de T005, T006)
- [X] T026 [US1] Implementar `CitaRepositoryAdapter` (implementa `CitaRepositoryPort`, traduce la violación de restricción de unicidad a resultado "franja no disponible") en `src/main/java/org/ups/citasalud/booking/adapter/out/persistence/CitaRepositoryAdapter.java` (depende de T010, T011, T006)
- [X] T027 [US1] Implementar `WhatsAppNotificacionAdapter` (implementa `NotificacionPort`, no relanza excepción ante fallo, marca `notificacionEnviada`) en `src/main/java/org/ups/citasalud/booking/adapter/out/notification/WhatsAppNotificacionAdapter.java` (depende de T007)
- [X] T028 [US1] Implementar el controlador REST que implementa la interfaz generada por openapi-generator para `POST /citas`, `GET /medicos/{id}/franjas` y `POST /franjas/{id}/seleccion` (mapeo DTO↔dominio, respuestas 200/201/409/422) en `src/main/java/org/ups/citasalud/booking/adapter/in/web/CitasController.java` (depende de T023, T024, T025, T026, T027)
- [X] T029 [US1] Configurar los beans Spring que conectan casos de uso con adapters (`@Configuration`) en `src/main/java/org/ups/citasalud/config/BookingBeansConfig.java` (depende de T023-T028)

**Checkpoint**: US1 es completamente funcional y probable de forma
independiente ejecutando `quickstart.md`

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Verificar las métricas de calidad exigidas por la Constitution y validar el flujo completo

- [X] T030 [P] Ejecutar `./gradlew jacocoTestCoverageVerification` y corregir cualquier clase por debajo de 80% o cobertura global por debajo de 80%
- [X] T031 [P] Ejecutar la validación end-to-end de `quickstart.md` (los tres escenarios) contra el servicio levantado con `./gradlew bootRun`, usando los datos precargados de `data.sql`
- [X] T032 Revisar logs/registro de fallos de notificación (FR-009) como verificación manual complementaria al test automatizado T015

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — puede iniciar de inmediato
- **Foundational (Phase 2)**: Depende de Setup — bloquea toda la Fase 3
- **User Story 1 (Phase 3)**: Depende de Foundational
- **Polish (Phase 4)**: Depende de que Fase 3 esté completa

### Within Foundational

- Esquema (T008) antes que datos precargados (T009) y antes que las entidades JPA (T010)
- Entidades JPA (T010) antes que los repositorios Spring Data (T011)
- Modelos (T005) y puerto de repositorio (T006) antes que `LiberarFranjaRetenidaUseCase` (T013)

### Within User Story 1

- Tests (T014-T022) deben escribirse y fallar antes de la implementación (T023-T029)
- Modelos/puertos (Foundational) antes que casos de uso (T023, T024, T025)
- Casos de uso antes que adapters que los invocan (T026 depende conceptualmente de T023 vía el puerto)
- Adapters (T026, T027) antes que el controlador (T028)
- Controlador antes que el wiring final de beans (T029)
- El test de concurrencia (T020) depende de que el adapter de persistencia (T027, alias del wiring completo) esté disponible; ejecutar después de T029 en la práctica aunque se liste en la fase de tests

### Parallel Opportunities

- T002, T003, T004 en paralelo tras T001
- T006, T007 en paralelo tras T005
- T009 en paralelo con la creación de entidades JPA una vez completado T008
- T014, T015, T016, T017, T018, T019, T021 en paralelo (archivos de test distintos); T020 y T022 tienen dependencias explícitas
- T023, T024, T025 en paralelo tras completarse Foundational
- T030, T031 en paralelo tras completarse Fase 3

---

## Parallel Example: User Story 1

```bash
# Lanzar juntas las pruebas de US1 (archivos distintos, sin dependencias entre sí):
Task: "Unit test de ReservarCitaUseCase en src/test/java/org/ups/citasalud/booking/unit/ReservarCitaUseCaseTest.java"
Task: "Unit test de fallo de notificación en src/test/java/org/ups/citasalud/booking/unit/ReservarCitaUseCaseNotificacionFallidaTest.java"
Task: "Unit test de liberación de franja retenida en src/test/java/org/ups/citasalud/booking/unit/LiberarFranjaRetenidaUseCaseTest.java"
Task: "Integration test @DataJpaTest en src/test/java/org/ups/citasalud/booking/integration/CitaRepositoryAdapterIT.java"
Task: "Integration test @WebMvcTest (franja inválida, 422) en src/test/java/org/ups/citasalud/booking/integration/ReservaCitaFranjaInvalidaControllerIT.java"
Task: "Integration test @WebMvcTest (controlador) en src/test/java/org/ups/citasalud/booking/integration/ReservaCitaControllerIT.java"
Task: "Feature Cucumber en src/test/resources/features/reserva_cita.feature"

# Lanzar juntos los casos de uso (tras Foundational):
Task: "ReservarCitaUseCase en src/main/java/org/ups/citasalud/booking/application/usecase/ReservarCitaUseCase.java"
Task: "ConsultarDisponibilidadUseCase en src/main/java/org/ups/citasalud/booking/application/usecase/ConsultarDisponibilidadUseCase.java"
Task: "SeleccionarFranjaUseCase en src/main/java/org/ups/citasalud/booking/application/usecase/SeleccionarFranjaUseCase.java"
```

---

## Implementation Strategy

### MVP First (única historia de usuario)

1. Completar Fase 1: Setup
2. Completar Fase 2: Foundational (incluye esquema, datos precargados y liberación de franjas retenidas, bloqueante)
3. Completar Fase 3: User Story 1 (tests primero, luego implementación)
4. **DETENER y VALIDAR**: ejecutar `quickstart.md` de punta a punta (3 escenarios)
5. Completar Fase 4: Polish (cobertura JaCoCo, validación funcional BDD)

---

## Notes

- [P] = archivos distintos, sin dependencias entre sí
- [US1] mapea cada tarea de historia a la única user story de este plan
- Verificar que las pruebas fallan antes de implementar (Principio II, TDD/BDD)
- Confirmar en cada checkpoint que `quickstart.md` sigue siendo válido
- Evitar: abstracciones especulativas no requeridas por esta historia (Principio III, YAGNI)
