# Quickstart: Reserva de Cita en Línea 24/7

Guía de validación de extremo a extremo para US-01. No incluye código de
implementación — ver [data-model.md](./data-model.md) para el modelo y
[contracts/citas-api.yaml](./contracts/citas-api.yaml) para el contrato.

## Prerrequisitos

- JDK 25 configurado (toolchain de Gradle ya lo resuelve).
- Sin servicios externos: H2 en memoria y un adapter de notificación
  "stub"/mock son suficientes para validar el flujo localmente.

## Preparación

```bash
./gradlew clean build
```

Esto debe: generar las interfaces de servidor a partir de
`src/main/resources/openapi/citas-api.yaml` (Principio IV), compilar,
ejecutar las tres suites de pruebas (unit/integration/functional) y
verificar las reglas de cobertura JaCoCo (Principio V).

## Arrancar el servicio localmente

```bash
./gradlew bootRun
```

## Escenario 1 — Reserva exitosa fuera de horario telefónico (Acceptance Scenario 1)

1. Consultar franjas disponibles de un médico de prueba:
   ```bash
   curl -s http://localhost:8080/api/v1/medicos/{medicoId}/franjas
   ```
   **Esperado**: `200 OK` con al menos una franja en estado `DISPONIBLE`.
2. Confirmar la reserva sobre una franja disponible:
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/citas \
     -H 'Content-Type: application/json' \
     -d '{"pacienteId":"<uuid>","medicoId":"<uuid>","franjaHorariaId":"<uuid>"}'
   ```
   **Esperado**: `201 Created`, cuerpo con `estado: CONFIRMADA`. El adapter
   de notificación (stub en entorno local) debe registrar un intento de
   envío de confirmación por WhatsApp.

## Escenario 2 — Franja ya ocupada (Acceptance Scenario 2)

1. Repetir el paso 2 anterior usando la misma `franjaHorariaId` ya
   confirmada (o ejecutar dos solicitudes concurrentes sobre la misma
   franja, para validar SC-003).
2. **Esperado**: la segunda solicitud responde `409 Conflict`; la primera
   sigue siendo la única `Cita` registrada para esa franja.

## Escenario 3 — Liberación automática de franja retenida (Acceptance Scenario 3, FR-010)

1. Marcar una franja disponible como retenida (selección sin confirmar):
   ```bash
   curl -s -X POST http://localhost:8080/api/v1/franjas/{franjaId}/seleccion
   ```
   **Esperado**: `200 OK`, la franja pasa a `RETENIDA`.
2. Sin confirmar la reserva, esperar 5 minutos y volver a consultar las
   franjas del médico:
   ```bash
   curl -s http://localhost:8080/api/v1/medicos/{medicoId}/franjas
   ```
   **Esperado**: la franja vuelve a aparecer como `DISPONIBLE`.

## Validación de cobertura

```bash
./gradlew jacocoTestCoverageVerification
```

**Esperado**: build exitoso solo si la cobertura por clase es > 80% y la
cobertura global es ≥ 80% (excluyendo código generado por
openapi-generator).

## Validación funcional BDD

```bash
./gradlew test --tests "*CucumberFunctionalTest*"
```

**Esperado**: los escenarios Cucumber correspondientes a los tres
Acceptance Scenarios de `spec.md` pasan en verde.
