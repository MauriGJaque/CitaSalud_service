package org.ups.citasalud.booking.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FranjaHoraria {

    private final UUID id;
    private final UUID medicoId;
    private final LocalDateTime fechaHoraInicio;
    private final LocalDateTime fechaHoraFin;
    private EstadoFranja estado;
    private LocalDateTime retenidaHasta;

    public FranjaHoraria(UUID id, UUID medicoId, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
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

    public LocalDateTime getRetenidaHasta() {
        return retenidaHasta;
    }

    /** FR-007: solo franjas futuras (respecto al instante dado) pueden reservarse. */
    public boolean esFutura(LocalDateTime ahora) {
        return fechaHoraInicio.isAfter(ahora);
    }

    /** FR-010: expira la retención de forma perezosa si ya pasó el plazo. */
    public void liberarSiRetencionExpirada(LocalDateTime ahora) {
        if (estado == EstadoFranja.RETENIDA && retenidaHasta != null && !ahora.isBefore(retenidaHasta)) {
            this.estado = EstadoFranja.DISPONIBLE;
            this.retenidaHasta = null;
        }
    }

    /** FR-010: marca la franja como retenida por el paciente mientras confirma. */
    public void retener(LocalDateTime ahora, java.time.Duration duracionRetencion) {
        if (estado != EstadoFranja.DISPONIBLE) {
            throw new IllegalStateException("La franja no está disponible para retener");
        }
        this.estado = EstadoFranja.RETENIDA;
        this.retenidaHasta = ahora.plus(duracionRetencion);
    }

    /** FR-005/FR-008: solo se puede confirmar una franja disponible o retenida por el mismo flujo. */
    public void confirmar() {
        if (estado == EstadoFranja.OCUPADA) {
            throw new IllegalStateException("La franja ya está ocupada");
        }
        this.estado = EstadoFranja.OCUPADA;
        this.retenidaHasta = null;
    }

    public boolean estaDisponible() {
        return estado == EstadoFranja.DISPONIBLE;
    }
}
