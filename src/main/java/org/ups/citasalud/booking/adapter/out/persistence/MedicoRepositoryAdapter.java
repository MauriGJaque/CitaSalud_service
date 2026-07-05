package org.ups.citasalud.booking.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.ups.citasalud.booking.domain.model.Medico;
import org.ups.citasalud.booking.domain.model.RangoAtencion;
import org.ups.citasalud.booking.domain.port.MedicoRepositoryPort;

@Component
public class MedicoRepositoryAdapter implements MedicoRepositoryPort {

    private final MedicoJpaRepository repository;

    public MedicoRepositoryAdapter(MedicoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Medico> buscarPorId(UUID medicoId) {
        return repository.findById(medicoId).map(this::toDomain);
    }

    private Medico toDomain(MedicoEntity e) {
        List<RangoAtencion> horario = e.getHorarioAtencion().stream()
                .map(r -> new RangoAtencion(r.getDiaSemana(), r.getHoraInicio(), r.getHoraFin()))
                .toList();
        return new Medico(e.getId(), e.getNombreCompleto(), e.getEspecialidad(), horario);
    }
}
