package org.ups.citasalud.booking.domain.port;

import java.util.UUID;

import org.ups.citasalud.booking.domain.model.Cita;

public interface CitaRepositoryPort {

    /**
     * Persiste la Cita y marca la franja asociada como OCUPADA, de forma
     * atómica, únicamente si la franja horaria sigue disponible en ese
     * instante; de lo contrario lanza {@link FranjaNoDisponibleException}
     * (FR-005, FR-006, FR-008).
     */
    Cita guardarSiFranjaDisponible(Cita cita);

    /** Actualiza el estado de notificación de una Cita ya registrada (FR-009). */
    Cita actualizarNotificacion(UUID citaId, boolean notificacionEnviada);
}
