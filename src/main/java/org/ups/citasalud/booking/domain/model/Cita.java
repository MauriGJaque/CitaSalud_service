package org.ups.citasalud.booking.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Cita {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID franjaHorariaId;
    private final EstadoCita estado;
    private final boolean notificacionEnviada;
    private final LocalDateTime creadaEn;

    public Cita(UUID id, UUID pacienteId, UUID medicoId, UUID franjaHorariaId, EstadoCita estado,
                boolean notificacionEnviada, LocalDateTime creadaEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.franjaHorariaId = franjaHorariaId;
        this.estado = estado;
        this.notificacionEnviada = notificacionEnviada;
        this.creadaEn = creadaEn;
    }

    /** Copia esta Cita marcando (o no) la notificación como enviada (FR-009). */
    public Cita conNotificacionEnviada(boolean enviada) {
        return new Cita(id, pacienteId, medicoId, franjaHorariaId, estado, enviada, creadaEn);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public UUID getMedicoId() {
        return medicoId;
    }

    public UUID getFranjaHorariaId() {
        return franjaHorariaId;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public boolean isNotificacionEnviada() {
        return notificacionEnviada;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}
