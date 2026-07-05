package org.ups.citasalud.booking.domain.port;

import java.util.Optional;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.Medico;

public interface MedicoRepositoryPort {

    Optional<Medico> buscarPorId(UUID medicoId);
}
