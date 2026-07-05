-- Datos precargados de ejemplo para desarrollo/demo/pruebas.
-- Los horarios de atención cubren todos los días 00:00-23:59 para simplificar
-- la demo; un despliegue real ajustaría esto por médico.

INSERT INTO paciente (id, nombre_completo, numero_whatsapp) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Ana Torres', '+593987654321'),
    ('22222222-2222-2222-2222-222222222222', 'Luis Vega', '+593987654322');

INSERT INTO medico (id, nombre_completo, especialidad) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Dra. María Salazar', 'Medicina General'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Dr. Pedro Ramírez', 'Odontología');

INSERT INTO medico_horario (medico_id, dia_semana, hora_inicio, hora_fin) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'MONDAY',    '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'TUESDAY',   '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'WEDNESDAY', '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'THURSDAY',  '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'FRIDAY',    '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SATURDAY',  '00:00:00', '23:59:59'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SUNDAY',    '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'MONDAY',    '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'TUESDAY',   '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'WEDNESDAY', '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'THURSDAY',  '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'FRIDAY',    '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SATURDAY',  '00:00:00', '23:59:59'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'SUNDAY',    '00:00:00', '23:59:59');

-- Franjas futuras (relativas al arranque) en estado DISPONIBLE.
INSERT INTO franja_horaria (id, medico_id, fecha_hora_inicio, fecha_hora_fin, estado, retenida_hasta) VALUES
    ('c1111111-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('DAY', 1, CURRENT_TIMESTAMP)), 'DISPONIBLE', NULL),
    ('c1111111-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('DAY', 2, CURRENT_TIMESTAMP)), 'DISPONIBLE', NULL),
    ('c1111111-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('DAY', 1, CURRENT_TIMESTAMP)), 'DISPONIBLE', NULL),
    ('c1111111-0000-0000-0000-000000000004', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('DAY', 3, CURRENT_TIMESTAMP)), 'DISPONIBLE', NULL);

-- Franja de ejemplo ya en el pasado, útil para validar FR-007 (rechazo de franjas no futuras).
INSERT INTO franja_horaria (id, medico_id, fecha_hora_inicio, fecha_hora_fin, estado, retenida_hasta) VALUES
    ('c1111111-0000-0000-0000-000000000005', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('MINUTE', 30, DATEADD('DAY', -1, CURRENT_TIMESTAMP)), 'DISPONIBLE', NULL);
