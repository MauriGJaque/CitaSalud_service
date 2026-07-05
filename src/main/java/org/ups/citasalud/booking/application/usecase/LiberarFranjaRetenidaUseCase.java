package org.ups.citasalud.booking.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;

/**
 * FR-010: libera de forma perezosa las franjas RETENIDA cuyo plazo de
 * retención ya expiró, dejándolas nuevamente DISPONIBLE.
 */
public class LiberarFranjaRetenidaUseCase {

    private final FranjaHorariaRepositoryPort franjaHorariaRepository;
    private final Clock clock;

    public LiberarFranjaRetenidaUseCase(FranjaHorariaRepositoryPort franjaHorariaRepository, Clock clock) {
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.clock = clock;
    }

    public void ejecutar(UUID medicoId, LocalDateTime desde) {
        LocalDateTime ahora = clock.ahora();
        List<FranjaHoraria> franjas = franjaHorariaRepository.buscarPorMedicoDesde(medicoId, desde);
        for (FranjaHoraria franja : franjas) {
            if (franja.getEstado() == EstadoFranja.RETENIDA) {
                franja.liberarSiRetencionExpirada(ahora);
                if (franja.getEstado() == EstadoFranja.DISPONIBLE) {
                    franjaHorariaRepository.guardar(franja);
                }
            }
        }
    }
}
