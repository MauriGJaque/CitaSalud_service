package org.ups.citasalud.booking.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.ups.citasalud.booking.domain.model.EstadoFranja;

@Entity
@Table(name = "franja_horaria")
public class FranjaHorariaEntity {

    @Id
    private UUID id;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin", nullable = false)
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoFranja estado;

    @Column(name = "retenida_hasta")
    private LocalDateTime retenidaHasta;

    protected FranjaHorariaEntity() {
    }

    public FranjaHorariaEntity(UUID id, UUID medicoId, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                                EstadoFranja estado, LocalDateTime retenidaHasta) {
        this.id = id;
        this.medicoId = medicoId;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.estado = estado;
        this.retenidaHasta = retenidaHasta;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMedicoId() {
        return medicoId;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoFranja getEstado() {
        return estado;
    }

    public void setEstado(EstadoFranja estado) {
        this.estado = estado;
    }

    public LocalDateTime getRetenidaHasta() {
        return retenidaHasta;
    }

    public void setRetenidaHasta(LocalDateTime retenidaHasta) {
        this.retenidaHasta = retenidaHasta;
    }
}
