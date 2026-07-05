package org.ups.citasalud.booking.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaEntity;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaJpaRepository;
import org.ups.citasalud.booking.domain.model.EstadoFranja;

/**
 * FR-005, FR-008, SC-003: verifica que la restricción de unicidad
 * (medico_id, fecha_hora_inicio) impide crear dos franjas duplicadas para
 * el mismo médico e instante.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CitaRepositoryAdapterIT {

    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    @Test
    void noPermiteDosFranjasParaElMismoMedicoYMismoInstante() {
        UUID medicoId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        LocalDateTime inicio = LocalDateTime.now().plusDays(10).withNano(0);

        FranjaHorariaEntity primera = new FranjaHorariaEntity(UUID.randomUUID(), medicoId, inicio,
                inicio.plusMinutes(30), EstadoFranja.DISPONIBLE, null);
        franjaHorariaJpaRepository.saveAndFlush(primera);

        FranjaHorariaEntity duplicada = new FranjaHorariaEntity(UUID.randomUUID(), medicoId, inicio,
                inicio.plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        assertThatThrownBy(() -> franjaHorariaJpaRepository.saveAndFlush(duplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void franjasPrecargadasDeDataSqlEstanDisponibles() {
        UUID medicoId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        var franjas = franjaHorariaJpaRepository
                .findByMedicoIdAndFechaHoraInicioGreaterThanEqualOrderByFechaHoraInicioAsc(
                        medicoId, LocalDateTime.now());
        assertThat(franjas).isNotEmpty();
    }
}
