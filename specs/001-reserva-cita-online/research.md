# Research: Reserva de Cita en Línea 24/7

No quedaron marcadores `NEEDS CLARIFICATION` en el Technical Context del
plan; las siguientes decisiones documentan las elecciones técnicas
concretas necesarias para pasar de la especificación al diseño (Phase 1).

## 1. Framework BDD para pruebas funcionales

- **Decision**: Cucumber-JVM (`io.cucumber:cucumber-java`,
  `io.cucumber:cucumber-junit-platform-engine`) ejecutado sobre JUnit 5
  Platform, con los escenarios Given/When/Then del spec (`spec.md`)
  traducidos directamente a archivos `.feature`.
- **Rationale**: La constitución exige pruebas funcionales bajo el
  concepto BDD explícitamente. Cucumber es el estándar de facto en el
  ecosistema JVM/Spring para BDD, se integra con `@SpringBootTest` y
  permite mantener trazabilidad 1:1 entre los "Acceptance Scenarios" del
  spec y los escenarios ejecutables.
- **Alternatives considered**: (a) JUnit 5 con nombres de métodos en
  estilo Given/When/Then sin Cucumber — descartado porque no separa el
  lenguaje de negocio (feature files) de la implementación, dificultando
  que un no-desarrollador lea/valide los escenarios; (b) Spock/Groovy —
  descartado por introducir un segundo lenguaje (Groovy) sin necesidad
  concreta (YAGNI).

## 2. Prevención de dobles reservas bajo concurrencia (SC-003)

- **Decision**: Restricción de unicidad a nivel de base de datos sobre
  `(medico_id, fecha_hora_inicio)` en la tabla de citas, combinada con una
  transacción que capture la violación de restricción y la traduzca a un
  resultado de dominio "franja no disponible" (FR-005, FR-006, FR-008).
- **Rationale**: Una restricción de unicidad en la base de datos es la
  única garantía verdadera contra condiciones de carrera entre instancias
  concurrentes de la aplicación; la validación exclusivamente en memoria
  o a nivel de aplicación no es suficiente si hay más de un nodo o hilo.
- **Alternatives considered**: (a) Bloqueo pesimista (`SELECT ... FOR
  UPDATE`) sobre la franja antes de confirmar — viable pero más costoso y
  redundante si ya existe la restricción de unicidad; se mantiene como
  opción de refuerzo pero no como mecanismo primario. (b) Bloqueo
  optimista con versión — no aplica directamente porque la colisión es
  entre dos registros nuevos (inserciones), no una actualización
  concurrente del mismo registro.

## 3. Notificación por WhatsApp (FR-004, FR-009)

- **Decision**: Definir un puerto de dominio `NotificacionPort` con un
  único método de envío de confirmación; la implementación concreta
  (adapter de salida) integra con un proveedor de WhatsApp Business API
  configurado externamente (credenciales/endpoint en `application.yaml`).
  Los fallos de envío se capturan en el adapter y se registran (log +
  marca de estado "no notificado" en la Cita) sin revertir la reserva.
- **Rationale**: Aislar el proveedor de notificación detrás de un puerto
  cumple Clean Architecture (Principio I) y evita acoplar el caso de uso
  a un SDK específico; permite cambiar de proveedor sin tocar el dominio.
  El comportamiento de "reserva válida aunque falle la notificación" es
  un requisito explícito (FR-009) que se resuelve en el adapter, no en el
  caso de uso.
- **Alternatives considered**: Envío síncrono bloqueante que revierta la
  reserva si falla — descartado porque contradice FR-009 y acopla la
  disponibilidad del proveedor externo a la disponibilidad del negocio
  crítico (registrar la cita).

## 4. Contrato OpenAPI y generación de código

- **Decision**: `openapi-generator-gradle-plugin` configurado para generar
  únicamente interfaces de servidor (`interfaceOnly` / `library: spring`)
  a partir de `src/main/resources/openapi/citas-api.yaml`; el controlador
  en `adapter/in/web` implementa la interfaz generada.
- **Rationale**: Cumple el Principio IV (API-First) tal como exige la
  constitución: el contrato es la fuente de verdad y el código de
  interfaz nunca se escribe a mano.
- **Alternatives considered**: Generar también los DTO de dominio desde
  el contrato — descartado; los DTO generados se usan solo en la
  frontera del adapter web y se mapean a los modelos de dominio internos,
  para no filtrar el contrato HTTP hacia el dominio (Clean Architecture).

## 5. Cobertura de pruebas (JaCoCo)

- **Decision**: Plugin `jacoco` de Gradle con `jacocoTestCoverageVerification`
  enlazado a la tarea `check`, reglas: `element = CLASS` con mínimo 0.80
  y regla global de bundle con mínimo 0.80; se excluyen los paquetes
  generados por `openapi-generator` (típicamente bajo
  `build/generated/...`) del cálculo de cobertura.
- **Rationale**: Cumple el Principio V de forma automática y bloqueante,
  consistente con las métricas exigidas por la constitución.
- **Alternatives considered**: Umbral único global sin verificación por
  clase — descartado porque la constitución exige explícitamente ambos
  niveles (por clase y global).

## 6. Esquema y datos precargados en `resources/db`

- **Decision**: Definir el esquema relacional (tablas `paciente`, `medico`,
  `franja_horaria`, `cita`, incluyendo la restricción de unicidad
  `(medico_id, fecha_hora_inicio)`) en `src/main/resources/db/schema.sql`,
  y datos de ejemplo (médicos y franjas horarias `DISPONIBLE`) en
  `src/main/resources/db/data.sql`. Se habilita la inicialización nativa
  de Spring Boot (`spring.sql.init.mode=always`, con
  `spring.jpa.hibernate.ddl-auto=validate` para que Hibernate valide el
  esquema en vez de generarlo) apuntando a ambos archivos.
- **Rationale**: Tener el DDL explícito y versionado (en vez de dejar que
  Hibernate lo autogenere) hace visible la restricción de unicidad que
  garantiza SC-003 (0% de dobles reservas) y permite que los tests de
  integración (`@DataJpaTest`) y el entorno de desarrollo/demo arranquen
  con datos consistentes sin pasos manuales. Mantiene el modelo de datos
  como una fuente de verdad auditable, alineado con `data-model.md`.
- **Alternatives considered**: (a) `ddl-auto=update`/`create` dejando que
  Hibernate genere el esquema — descartado porque no garantiza de forma
  explícita y revisable la restricción de unicidad crítica para SC-003 y
  dificulta el control de versiones del esquema. (b) Migraciones con
  Flyway/Liquibase — más apropiado a mediano plazo, pero se descarta por
  ahora (YAGNI) al no existir todavía una necesidad concreta de
  versionado incremental entre entornos; puede introducirse más adelante
  sin cambiar el modelo de datos.
