package org.ups.citasalud.booking.domain.port;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.FranjaHoraria;

public interface FranjaHorariaRepositoryPort {

    Optional<FranjaHoraria> buscarPorId(UUID franjaId);

    /** Todas las franjas de un médico desde el instante dado, en cualquier estado. */
    List<FranjaHoraria> buscarPorMedicoDesde(UUID medicoId, LocalDateTime desde);

    /**
     * Persiste la transición DISPONIBLE → RETENIDA únicamente si la franja
     * sigue DISPONIBLE en ese instante; de lo contrario lanza
     * {@link FranjaNoDisponibleException} (FR-010).
     */
    FranjaHoraria retenerSiDisponible(UUID franjaId, LocalDateTime ahora, java.time.Duration duracionRetencion);

    FranjaHoraria guardar(FranjaHoraria franja);
}
