package org.ups.citasalud.booking.adapter.out.persistence;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.port.CitaRepositoryPort;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;

import java.util.UUID;

/**
 * Traduce la violación de la restricción de unicidad
 * {@code cita.franja_horaria_id} (ver schema.sql) en
 * {@link FranjaNoDisponibleException}, que es la garantía real contra
 * dobles reservas bajo concurrencia (FR-008, SC-003).
 */
@Component
public class CitaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository citaJpaRepository;
    private final FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    public CitaRepositoryAdapter(CitaJpaRepository citaJpaRepository,
                                  FranjaHorariaJpaRepository franjaHorariaJpaRepository) {
        this.citaJpaRepository = citaJpaRepository;
        this.franjaHorariaJpaRepository = franjaHorariaJpaRepository;
    }

    @Override
    @Transactional
    public Cita guardarSiFranjaDisponible(Cita cita) {
        FranjaHorariaEntity franja = franjaHorariaJpaRepository.findById(cita.getFranjaHorariaId())
                .orElseThrow(() -> new FranjaNoDisponibleException(
                        "Franja horaria no encontrada: " + cita.getFranjaHorariaId()));

        if (franja.getEstado() == EstadoFranja.OCUPADA) {
            throw new FranjaNoDisponibleException("La franja ya está ocupada: " + cita.getFranjaHorariaId());
        }

        CitaEntity citaEntity = new CitaEntity(cita.getId(), cita.getPacienteId(), cita.getMedicoId(),
                cita.getFranjaHorariaId(), cita.getEstado(), cita.isNotificacionEnviada(), cita.getCreadaEn());

        try {
            citaJpaRepository.saveAndFlush(citaEntity);
        } catch (DataIntegrityViolationException e) {
            // Otra solicitud concurrente ganó la carrera por esta franja (FR-008, SC-003).
            throw new FranjaNoDisponibleException(
                    "La franja ya fue reservada por otra solicitud concurrente: " + cita.getFranjaHorariaId());
        }

        franja.setEstado(EstadoFranja.OCUPADA);
        franja.setRetenidaHasta(null);
        franjaHorariaJpaRepository.save(franja);

        return cita;
    }

    @Override
    @Transactional
    public Cita actualizarNotificacion(UUID citaId, boolean notificacionEnviada) {
        CitaEntity entity = citaJpaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalStateException("Cita no encontrada: " + citaId));
        entity.setNotificacionEnviada(notificacionEnviada);
        CitaEntity guardada = citaJpaRepository.save(entity);
        return toDomain(guardada);
    }

    private Cita toDomain(CitaEntity e) {
        return new Cita(e.getId(), e.getPacienteId(), e.getMedicoId(), e.getFranjaHorariaId(), e.getEstado(),
                e.isNotificacionEnviada(), e.getCreadaEn());
    }
}
