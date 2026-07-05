package org.ups.citasalud.booking.domain.port;

import java.util.Optional;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.Paciente;

public interface PacienteRepositoryPort {

    Optional<Paciente> buscarPorId(UUID pacienteId);
}
