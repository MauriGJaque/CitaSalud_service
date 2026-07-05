package org.ups.citasalud.booking.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FranjaHorariaJpaRepository extends JpaRepository<FranjaHorariaEntity, UUID> {

    List<FranjaHorariaEntity> findByMedicoIdAndFechaHoraInicioGreaterThanEqualOrderByFechaHoraInicioAsc(
            UUID medicoId, LocalDateTime desde);
}
