package org.ups.citasalud.booking.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.ups.citasalud.booking.domain.model.EstadoCita;

@Entity
@Table(name = "cita")
public class CitaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "franja_horaria_id", nullable = false, unique = true)
    private UUID franjaHorariaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado;

    @Column(name = "notificacion_enviada", nullable = false)
    private boolean notificacionEnviada;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    protected CitaEntity() {
    }

    public CitaEntity(UUID id, UUID pacienteId, UUID medicoId, UUID franjaHorariaId, EstadoCita estado,
                       boolean notificacionEnviada, LocalDateTime creadaEn) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.franjaHorariaId = franjaHorariaId;
        this.estado = estado;
        this.notificacionEnviada = notificacionEnviada;
        this.creadaEn = creadaEn;
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

    public void setNotificacionEnviada(boolean notificacionEnviada) {
        this.notificacionEnviada = notificacionEnviada;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }
}
