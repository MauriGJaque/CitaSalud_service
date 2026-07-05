package org.ups.citasalud.booking.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;

/**
 * FR-002, FR-007: lista las franjas DISPONIBLE futuras de un médico,
 * liberando primero (de forma perezosa) cualquier retención expirada (FR-010).
 */
public class ConsultarDisponibilidadUseCase {

    private final FranjaHorariaRepositoryPort franjaHorariaRepository;
    private final LiberarFranjaRetenidaUseCase liberarFranjaRetenidaUseCase;
    private final Clock clock;

    public ConsultarDisponibilidadUseCase(FranjaHorariaRepositoryPort franjaHorariaRepository,
                                           LiberarFranjaRetenidaUseCase liberarFranjaRetenidaUseCase,
                                           Clock clock) {
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.liberarFranjaRetenidaUseCase = liberarFranjaRetenidaUseCase;
        this.clock = clock;
    }

    public List<FranjaHoraria> ejecutar(UUID medicoId, LocalDateTime desde) {
        LocalDateTime ahora = clock.ahora();
        LocalDateTime efectivoDesde = desde != null && desde.isAfter(ahora) ? desde : ahora;

        liberarFranjaRetenidaUseCase.ejecutar(medicoId, efectivoDesde);

        return franjaHorariaRepository.buscarPorMedicoDesde(medicoId, efectivoDesde).stream()
                .filter(FranjaHoraria::estaDisponible)
                .filter(franja -> franja.esFutura(ahora))
                .toList();
    }
}
