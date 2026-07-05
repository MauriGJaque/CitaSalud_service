package org.ups.citasalud.booking.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.ups.citasalud.booking.domain.model.Paciente;
import org.ups.citasalud.booking.domain.port.PacienteRepositoryPort;

@Component
public class PacienteRepositoryAdapter implements PacienteRepositoryPort {

    private final PacienteJpaRepository repository;

    public PacienteRepositoryAdapter(PacienteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Paciente> buscarPorId(UUID pacienteId) {
        return repository.findById(pacienteId)
                .map(e -> new Paciente(e.getId(), e.getNombreCompleto(), e.getNumeroWhatsapp()));
    }
}
