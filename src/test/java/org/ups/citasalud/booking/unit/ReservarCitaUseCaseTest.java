package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ups.citasalud.booking.application.usecase.ReservarCitaUseCase;
import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.EstadoCita;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.model.Medico;
import org.ups.citasalud.booking.domain.model.Paciente;
import org.ups.citasalud.booking.domain.model.RangoAtencion;
import org.ups.citasalud.booking.domain.port.CitaRepositoryPort;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;
import org.ups.citasalud.booking.domain.port.MedicoRepositoryPort;
import org.ups.citasalud.booking.domain.port.NotificacionPort;
import org.ups.citasalud.booking.domain.port.PacienteRepositoryPort;
import org.ups.citasalud.booking.unit.support.FakeClock;

/**
 * FR-001 a FR-006, FR-008: reserva exitosa y rechazo de franja ocupada.
 */
@ExtendWith(MockitoExtension.class)
class ReservarCitaUseCaseTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 7, 6, 10, 0); // lunes

    @Mock
    private CitaRepositoryPort citaRepositoryPort;
    @Mock
    private FranjaHorariaRepositoryPort franjaHorariaRepositoryPort;
    @Mock
    private MedicoRepositoryPort medicoRepositoryPort;
    @Mock
    private PacienteRepositoryPort pacienteRepositoryPort;
    @Mock
    private NotificacionPort notificacionPort;

    private FakeClock clock;
    private ReservarCitaUseCase useCase;

    private UUID pacienteId;
    private UUID medicoId;
    private UUID franjaId;
    private Medico medico;
    private Paciente paciente;
    private FranjaHoraria franjaDisponible;

    @BeforeEach
    void setUp() {
        clock = new FakeClock(AHORA);
        useCase = new ReservarCitaUseCase(citaRepositoryPort, franjaHorariaRepositoryPort, medicoRepositoryPort,
                pacienteRepositoryPort, notificacionPort, clock);

        pacienteId = UUID.randomUUID();
        medicoId = UUID.randomUUID();
        franjaId = UUID.randomUUID();

        medico = new Medico(medicoId, "Dra. Prueba", "Medicina General",
                List.of(new RangoAtencion(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))));
        paciente = new Paciente(pacienteId, "Paciente Prueba", "+593999999999");
        franjaDisponible = new FranjaHoraria(franjaId, medicoId, AHORA.plusHours(1), AHORA.plusHours(1).plusMinutes(30),
                EstadoFranja.DISPONIBLE, null);
    }

    @Test
    void reservaExitosaRegistraCitaConfirmadaYNotifica() {
        when(franjaHorariaRepositoryPort.buscarPorId(franjaId)).thenReturn(Optional.of(franjaDisponible));
        when(medicoRepositoryPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepositoryPort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));

        Cita citaSinNotificar = new Cita(UUID.randomUUID(), pacienteId, medicoId, franjaId,
                EstadoCita.CONFIRMADA, false, AHORA);
        when(citaRepositoryPort.guardarSiFranjaDisponible(any())).thenReturn(citaSinNotificar);
        when(notificacionPort.enviarConfirmacion(any(), any())).thenReturn(true);
        when(citaRepositoryPort.actualizarNotificacion(any(), anyBoolean()))
                .thenReturn(citaSinNotificar.conNotificacionEnviada(true));

        Cita resultado = useCase.ejecutar(pacienteId, medicoId, franjaId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
        assertThat(resultado.isNotificacionEnviada()).isTrue();
        verify(citaRepositoryPort).guardarSiFranjaDisponible(any());
        verify(notificacionPort).enviarConfirmacion(any(), any());
    }

    @Test
    void franjaYaOcupadaPropagaFranjaNoDisponibleException() {
        when(franjaHorariaRepositoryPort.buscarPorId(franjaId)).thenReturn(Optional.of(franjaDisponible));
        when(medicoRepositoryPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepositoryPort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
        when(citaRepositoryPort.guardarSiFranjaDisponible(any()))
                .thenThrow(new FranjaNoDisponibleException("La franja ya está ocupada"));

        assertThatThrownBy(() -> useCase.ejecutar(pacienteId, medicoId, franjaId))
                .isInstanceOf(FranjaNoDisponibleException.class);
    }
}
