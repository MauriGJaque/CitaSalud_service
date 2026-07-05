package org.ups.citasalud.booking.adapter.out.persistence;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;

@Component
public class FranjaHorariaRepositoryAdapter implements FranjaHorariaRepositoryPort {

    private final FranjaHorariaJpaRepository repository;

    public FranjaHorariaRepositoryAdapter(FranjaHorariaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FranjaHoraria> buscarPorId(UUID franjaId) {
        return repository.findById(franjaId).map(this::toDomain);
    }

    @Override
    public List<FranjaHoraria> buscarPorMedicoDesde(UUID medicoId, LocalDateTime desde) {
        return repository
                .findByMedicoIdAndFechaHoraInicioGreaterThanEqualOrderByFechaHoraInicioAsc(medicoId, desde)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public FranjaHoraria retenerSiDisponible(UUID franjaId, LocalDateTime ahora, Duration duracionRetencion) {
        FranjaHorariaEntity entity = repository.findById(franjaId)
                .orElseThrow(() -> new FranjaNoDisponibleException("Franja horaria no encontrada: " + franjaId));

        if (entity.getEstado() != EstadoFranja.DISPONIBLE) {
            throw new FranjaNoDisponibleException("La franja no está disponible para retener: " + franjaId);
        }

        entity.setEstado(EstadoFranja.RETENIDA);
        entity.setRetenidaHasta(ahora.plus(duracionRetencion));
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public FranjaHoraria guardar(FranjaHoraria franja) {
        FranjaHorariaEntity entity = repository.findById(franja.getId())
                .orElseThrow(() -> new IllegalStateException("Franja horaria no encontrada: " + franja.getId()));
        entity.setEstado(franja.getEstado());
        entity.setRetenidaHasta(franja.getRetenidaHasta());
        return toDomain(repository.save(entity));
    }

    private FranjaHoraria toDomain(FranjaHorariaEntity e) {
        return new FranjaHoraria(e.getId(), e.getMedicoId(), e.getFechaHoraInicio(), e.getFechaHoraFin(),
                e.getEstado(), e.getRetenidaHasta());
    }
}
