package org.ups.citasalud.booking.application.usecase;

import java.time.Duration;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;

/**
 * FR-010: marca una franja DISPONIBLE como RETENIDA por 5 minutos mientras
 * el paciente confirma la reserva.
 */
public class SeleccionarFranjaUseCase {

    public static final Duration DURACION_RETENCION = Duration.ofMinutes(5);

    private final FranjaHorariaRepositoryPort franjaHorariaRepository;
    private final Clock clock;

    public SeleccionarFranjaUseCase(FranjaHorariaRepositoryPort franjaHorariaRepository, Clock clock) {
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.clock = clock;
    }

    public FranjaHoraria ejecutar(UUID franjaId) {
        return franjaHorariaRepository.retenerSiDisponible(franjaId, clock.ahora(), DURACION_RETENCION);
    }
}
