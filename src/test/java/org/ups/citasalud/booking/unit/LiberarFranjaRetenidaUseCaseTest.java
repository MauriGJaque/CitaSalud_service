package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.booking.application.usecase.LiberarFranjaRetenidaUseCase;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;
import org.ups.citasalud.booking.unit.support.FakeClock;

/**
 * FR-010: una franja RETENIDA vuelve a DISPONIBLE tras 5 minutos sin confirmar.
 */
@ExtendWith(MockitoExtension.class)
class LiberarFranjaRetenidaUseCaseTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 7, 6, 10, 0);

    @Mock
    private FranjaHorariaRepositoryPort franjaHorariaRepositoryPort;

    @Test
    void franjaRetenidaExpiradaSeLiberaYSePersiste() {
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        FakeClock clock = new FakeClock(T0);

        FranjaHoraria retenida = new FranjaHoraria(franjaId, medicoId, T0.plusHours(2), T0.plusHours(2).plusMinutes(30),
                EstadoFranja.RETENIDA, T0.minusSeconds(1)); // el plazo de retención ya venció

        when(franjaHorariaRepositoryPort.buscarPorMedicoDesde(medicoId, T0)).thenReturn(List.of(retenida));

        new LiberarFranjaRetenidaUseCase(franjaHorariaRepositoryPort, clock).ejecutar(medicoId, T0);

        assertThat(retenida.getEstado()).isEqualTo(EstadoFranja.DISPONIBLE);
        assertThat(retenida.getRetenidaHasta()).isNull();
        verify(franjaHorariaRepositoryPort).guardar(retenida);
    }

    @Test
    void franjaRetenidaVigenteNoSeLibera() {
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        FakeClock clock = new FakeClock(T0);

        FranjaHoraria retenida = new FranjaHoraria(franjaId, medicoId, T0.plusHours(2), T0.plusHours(2).plusMinutes(30),
                EstadoFranja.RETENIDA, T0.plus(Duration.ofMinutes(5)));

        when(franjaHorariaRepositoryPort.buscarPorMedicoDesde(medicoId, T0)).thenReturn(List.of(retenida));

        new LiberarFranjaRetenidaUseCase(franjaHorariaRepositoryPort, clock).ejecutar(medicoId, T0);

        assertThat(retenida.getEstado()).isEqualTo(EstadoFranja.RETENIDA);
        verify(franjaHorariaRepositoryPort, never()).guardar(any());
    }
}
