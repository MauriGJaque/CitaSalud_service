DROP TABLE IF EXISTS cita;
DROP TABLE IF EXISTS franja_horaria;
DROP TABLE IF EXISTS medico_horario;
DROP TABLE IF EXISTS medico;
DROP TABLE IF EXISTS paciente;

CREATE TABLE paciente (
    id               UUID PRIMARY KEY,
    nombre_completo  VARCHAR(200) NOT NULL,
    numero_whatsapp  VARCHAR(20)  NOT NULL
);

CREATE TABLE medico (
    id               UUID PRIMARY KEY,
    nombre_completo  VARCHAR(200) NOT NULL,
    especialidad     VARCHAR(100) NOT NULL
);

CREATE TABLE medico_horario (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    medico_id    UUID        NOT NULL,
    dia_semana   VARCHAR(10) NOT NULL,
    hora_inicio  TIME        NOT NULL,
    hora_fin     TIME        NOT NULL,
    CONSTRAINT fk_medico_horario_medico FOREIGN KEY (medico_id) REFERENCES medico (id)
);

-- FR-005/FR-008/SC-003: la restricción de unicidad (medico_id, fecha_hora_inicio)
-- es la garantía real contra dobles reservas bajo concurrencia (ver research.md #2).
CREATE TABLE franja_horaria (
    id                 UUID PRIMARY KEY,
    medico_id          UUID      NOT NULL,
    fecha_hora_inicio  TIMESTAMP NOT NULL,
    fecha_hora_fin     TIMESTAMP NOT NULL,
    estado             VARCHAR(20) NOT NULL,
    retenida_hasta     TIMESTAMP,
    CONSTRAINT fk_franja_medico FOREIGN KEY (medico_id) REFERENCES medico (id),
    CONSTRAINT uq_franja_medico_inicio UNIQUE (medico_id, fecha_hora_inicio)
);

CREATE TABLE cita (
    id                   UUID PRIMARY KEY,
    paciente_id          UUID NOT NULL,
    medico_id            UUID NOT NULL,
    franja_horaria_id    UUID NOT NULL,
    estado               VARCHAR(20) NOT NULL,
    notificacion_enviada BOOLEAN NOT NULL,
    creada_en            TIMESTAMP NOT NULL,
    CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id) REFERENCES paciente (id),
    CONSTRAINT fk_cita_medico FOREIGN KEY (medico_id) REFERENCES medico (id),
    CONSTRAINT fk_cita_franja FOREIGN KEY (franja_horaria_id) REFERENCES franja_horaria (id),
    CONSTRAINT uq_cita_franja UNIQUE (franja_horaria_id)
);
