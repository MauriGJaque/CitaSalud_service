# Feature Specification: Reserva de Cita en Línea 24/7

**Feature Branch**: `001-reserva-cita-online`

**Created**: 2026-07-04

**Status**: Draft

**Input**: User description: "US-01 · Reserva de cita en linea 24/7 · epica E-01 · 8 pts — Como paciente, quiero reservar una cita en linea en cualquier momento del dia, para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reserva de cita en línea 24/7 (Priority: P1)

Como paciente, quiero reservar una cita en línea en cualquier momento del día,
para no tener que llamar durante mi horario de almuerzo ni acumular intentos
fallidos de contacto telefónico.

**Why this priority**: Es la funcionalidad central de la épica de reservas
en línea: sin ella el paciente sigue dependiendo del canal telefónico durante
horario de atención, que es exactamente el problema que se busca resolver.
Es el mínimo viable para liberar al paciente de la restricción horaria.

**Independent Test**: Puede probarse por completo haciendo que un paciente
acceda al sistema fuera del horario de atención telefónica, seleccione
médico, fecha y hora disponibles, confirme la reserva, y verifique que la
cita quede registrada y reciba una confirmación por WhatsApp — sin depender
de ninguna otra historia de usuario.

**Acceptance Scenarios**:

1. **Given** el paciente accede al sistema fuera del horario de atención
   telefónica, **When** elige médico, fecha y hora disponibles y confirma,
   **Then** la cita queda registrada y el paciente recibe confirmación por
   WhatsApp.
2. **Given** el paciente intenta seleccionar una franja ya ocupada, **When**
   intenta confirmarla, **Then** el sistema la muestra como no disponible y
   lo invita a elegir otra franja.
3. **Given** el paciente selecciona una franja horaria pero no confirma la
   reserva, **When** transcurren 5 minutos sin confirmación, **Then** la
   franja vuelve al estado `DISPONIBLE` y queda disponible para otros
   pacientes.

---

### Edge Cases

- ¿Qué ocurre si dos pacientes seleccionan e intentan confirmar la misma
  franja horaria de forma simultánea? El sistema MUST garantizar que solo
  una de las dos reservas se confirme y la otra reciba el estado "no
  disponible".
- ¿Qué ocurre si el envío de la confirmación por WhatsApp falla (número
  inválido, servicio no disponible) después de que la cita ya quedó
  registrada? La reserva MUST permanecer válida y el sistema MUST registrar
  el fallo de notificación para reintento o canal alterno.
- ¿Qué ocurre si el paciente abandona el proceso antes de confirmar (por
  ejemplo, cierra la sesión después de elegir médico y horario)? La franja
  seleccionada MUST liberarse automáticamente a los 5 minutos (FR-010) para
  no bloquearla indefinidamente.
- ¿Qué ocurre si el paciente selecciona una fecha/hora en el pasado o fuera
  del horario de atención del médico elegido? El sistema MUST impedir la
  selección y mostrar únicamente franjas válidas y futuras.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir a un paciente reservar una cita en
  línea en cualquier momento, sin restricción de horario de atención
  telefónica.
- **FR-002**: El sistema MUST permitir al paciente elegir un médico, una
  fecha y una hora entre las franjas disponibles antes de confirmar la
  reserva.
- **FR-003**: El sistema MUST registrar la cita únicamente cuando el
  paciente confirme explícitamente su selección.
- **FR-004**: El sistema MUST enviar una confirmación al paciente por
  WhatsApp inmediatamente después de registrar la cita.
- **FR-005**: El sistema MUST impedir que una franja horaria ya ocupada sea
  confirmada por otro paciente, mostrándola como no disponible.
- **FR-006**: El sistema MUST invitar al paciente a elegir otra franja
  cuando la que intentó confirmar ya no está disponible.
- **FR-007**: El sistema MUST mostrar en todo momento únicamente franjas de
  fecha/hora futuras y dentro del horario de atención del médico
  seleccionado.
- **FR-008**: El sistema MUST garantizar que, ante intentos simultáneos
  sobre la misma franja, solo una reserva quede confirmada.
- **FR-009**: El sistema MUST conservar la reserva como válida aun cuando el
  envío de la confirmación por WhatsApp falle, y MUST dejar constancia del
  fallo de notificación.
- **FR-010**: El sistema MUST liberar automáticamente una franja horaria
  seleccionada (pero no confirmada) 5 minutos después de la última
  interacción del paciente con esa selección, dejándola nuevamente
  `DISPONIBLE` para otros pacientes.

### Key Entities *(include if feature involves data)*

- **Paciente**: Persona que solicita la cita; se identifica de forma única
  y debe contar con un número de contacto válido para recibir la
  confirmación por WhatsApp.
- **Médico**: Profesional de salud con quien se agenda la cita; tiene un
  horario de atención propio que acota las franjas ofrecidas.
- **Cita**: Reserva confirmada que vincula un paciente, un médico y una
  franja horaria específica; tiene un estado (por ejemplo, confirmada) y un
  registro de si la confirmación fue notificada.
- **Franja Horaria**: Bloque de fecha/hora disponible u ocupado para un
  médico determinado; es la unidad mínima que un paciente puede
  seleccionar y reservar.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un paciente puede completar una reserva de cita, desde la
  selección del médico hasta la confirmación, en menos de 3 minutos.
- **SC-002**: El 100% de las citas confirmadas generan un intento de
  confirmación por WhatsApp en menos de 1 minuto después del registro.
- **SC-003**: 0% de dobles reservas sobre una misma franja horaria bajo
  intentos concurrentes.
- **SC-004**: El paciente puede reservar exitosamente fuera del horario de
  atención telefónica en el 100% de los casos en que existan franjas
  disponibles, sin necesidad de contacto telefónico.

## Assumptions

- El horario de atención telefónica es distinto (más acotado) que la
  disponibilidad del sistema de reserva en línea, que opera 24/7.
- El paciente ya cuenta con un número de WhatsApp válido asociado a su
  perfil o lo provee al momento de reservar.
- La gestión de cancelación o reprogramación de citas no está incluida en
  esta historia de usuario y se abordará en una historia separada.
- La autenticación/identificación del paciente antes de reservar usa el
  mecanismo estándar ya existente del sistema y no se detalla aquí.
- Las franjas horarias disponibles por médico ya existen o se derivan de un
  calendario/agenda médica previamente configurada; su creación y
  mantenimiento no es parte de esta historia.
