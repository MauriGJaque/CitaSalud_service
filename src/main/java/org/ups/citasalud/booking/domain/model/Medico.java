package org.ups.citasalud.booking.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Medico {

    private final UUID id;
    private final String nombreCompleto;
    private final String especialidad;
    private final List<RangoAtencion> horarioAtencion;

    public Medico(UUID id, String nombreCompleto, String especialidad, List<RangoAtencion> horarioAtencion) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.horarioAtencion = List.copyOf(horarioAtencion);
    }

    public UUID getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public List<RangoAtencion> getHorarioAtencion() {
        return horarioAtencion;
    }

    /** FR-007: la franja debe estar dentro del horario de atención del médico. */
    public boolean atiendeEn(LocalDateTime fechaHora) {
        return horarioAtencion.stream()
                .anyMatch(rango -> rango.cubre(fechaHora.getDayOfWeek(), fechaHora.toLocalTime()));
    }
}
