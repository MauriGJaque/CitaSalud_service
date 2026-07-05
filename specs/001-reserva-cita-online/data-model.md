# Data Model: Reserva de Cita en Línea 24/7

Modelos de dominio (capa `domain/model`, sin anotaciones de framework).
Las anotaciones JPA correspondientes viven exclusivamente en las entidades
de persistencia del adapter de salida (`adapter/out/persistence`), que
mapean hacia/desde estos modelos.

## Paciente

Persona que solicita la cita.

| Campo | Tipo | Reglas |
|-------|------|--------|
| `id` | identificador único | Requerido, inmutable |
| `nombreCompleto` | texto | Requerido |
| `numeroWhatsapp` | texto (formato E.164) | Requerido; usado por `NotificacionPort` (FR-004) |

## Médico

Profesional de salud con quien se agenda la cita.

| Campo | Tipo | Reglas |
|-------|------|--------|
| `id` | identificador único | Requerido, inmutable |
| `nombreCompleto` | texto | Requerido |
| `especialidad` | texto | Requerido |
| `horarioAtencion` | lista de rangos (día de semana, hora inicio, hora fin) | Requerido; acota qué `FranjaHoraria` son válidas (FR-007) |

## FranjaHoraria

Bloque de fecha/hora disponible u ocupado para un médico.

| Campo | Tipo | Reglas |
|-------|------|--------|
| `id` | identificador único | Requerido |
| `medicoId` | referencia a Médico | Requerido |
| `fechaHoraInicio` | fecha/hora | Requerido; MUST ser futura en el momento de la reserva (FR-007) |
| `fechaHoraFin` | fecha/hora | Requerido; posterior a `fechaHoraInicio` |
| `estado` | enum: `DISPONIBLE`, `RETENIDA`, `OCUPADA` | `DISPONIBLE → RETENIDA` al seleccionar (sin confirmar); `RETENIDA → OCUPADA` solo al confirmar una `Cita` (FR-005, FR-008); `RETENIDA → DISPONIBLE` automático (FR-010) |
| `retenidaHasta` | fecha/hora, nullable | Se establece al pasar a `RETENIDA`; si `ahora > retenidaHasta` y no hay `Cita` asociada, la franja se considera `DISPONIBLE` (FR-010) |

**Restricción de unicidad**: `(medicoId, fechaHoraInicio)` es única a nivel
de persistencia — ver [research.md](./research.md#2-prevención-de-dobles-reservas-bajo-concurrencia-sc-003).

## Cita

Reserva confirmada que vincula un paciente, un médico y una franja horaria.

| Campo | Tipo | Reglas |
|-------|------|--------|
| `id` | identificador único | Requerido, generado al confirmar |
| `pacienteId` | referencia a Paciente | Requerido |
| `medicoId` | referencia a Médico | Requerido |
| `franjaHorariaId` | referencia a FranjaHoraria | Requerido; única (ver arriba) |
| `estado` | enum: `CONFIRMADA` | Único valor válido para esta historia (cancelación/reprogramación fuera de alcance, ver Assumptions en spec.md) |
| `notificacionEnviada` | booleano | `false` por defecto; se marca `true` solo si `NotificacionPort` confirma el envío (FR-009) |
| `creadaEn` | fecha/hora | Requerido, asignada al confirmar |

### Relaciones

- Un `Paciente` puede tener 0..N `Cita`.
- Un `Médico` puede tener 0..N `Cita` y 0..N `FranjaHoraria`.
- Una `FranjaHoraria` tiene, como máximo, una `Cita` asociada (relación
  1:0..1), garantizado por la restricción de unicidad.

### Transiciones de estado

```text
FranjaHoraria: DISPONIBLE ──(seleccionar, no confirmar)──> RETENIDA
               RETENIDA   ──(confirmar Cita)─────────────> OCUPADA
               RETENIDA   ──(5 min sin confirmar, FR-010)─> DISPONIBLE
Cita:          (no existe) ──(confirmar)──────> CONFIRMADA
```

No existen transiciones adicionales (cancelar, reprogramar, completar) en
el alcance de esta historia de usuario.

## Puertos de dominio (interfaces)

- **CitaRepositoryPort**: `guardarSiFranjaDisponible(Cita): Cita` — MUST
  lanzar/retornar un resultado que distinga "franja no disponible" de
  otros errores, para soportar FR-005/FR-006/FR-008.
- **NotificacionPort**: `enviarConfirmacion(Cita, Paciente): ResultadoNotificacion`
  — MUST no lanzar excepción que revierta la reserva ante fallo (FR-009).
