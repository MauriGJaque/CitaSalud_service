package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import org.ups.citasalud.booking.domain.port.MedicoRepositoryPort;
import org.ups.citasalud.booking.domain.port.NotificacionPort;
import org.ups.citasalud.booking.domain.port.PacienteRepositoryPort;
import org.ups.citasalud.booking.unit.support.FakeClock;

/**
 * FR-009: la reserva permanece CONFIRMADA aun cuando falla el envío de la
 * confirmación por WhatsApp.
 */
@ExtendWith(MockitoExtension.class)
class ReservarCitaUseCaseNotificacionFallidaTest {

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

    @Test
    void reservaSiguesConfirmadaCuandoNotificacionFalla() {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();

        FakeClock clock = new FakeClock(AHORA);
        ReservarCitaUseCase useCase = new ReservarCitaUseCase(citaRepositoryPort, franjaHorariaRepositoryPort,
                medicoRepositoryPort, pacienteRepositoryPort, notificacionPort, clock);

        Medico medico = new Medico(medicoId, "Dra. Prueba", "Medicina General",
                List.of(new RangoAtencion(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))));
        Paciente paciente = new Paciente(pacienteId, "Paciente Prueba", "+593999999999");
        FranjaHoraria franja = new FranjaHoraria(franjaId, medicoId, AHORA.plusHours(1),
                AHORA.plusHours(1).plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        when(franjaHorariaRepositoryPort.buscarPorId(franjaId)).thenReturn(Optional.of(franja));
        when(medicoRepositoryPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
        when(pacienteRepositoryPort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));

        Cita citaSinNotificar = new Cita(UUID.randomUUID(), pacienteId, medicoId, franjaId,
                EstadoCita.CONFIRMADA, false, AHORA);
        when(citaRepositoryPort.guardarSiFranjaDisponible(any())).thenReturn(citaSinNotificar);
        // FR-009: el adapter de notificación NUNCA lanza excepción; un fallo se refleja como false.
        when(notificacionPort.enviarConfirmacion(any(), any())).thenReturn(false);
        when(citaRepositoryPort.actualizarNotificacion(eq(citaSinNotificar.getId()), eq(false)))
                .thenReturn(citaSinNotificar.conNotificacionEnviada(false));

        Cita resultado = useCase.ejecutar(pacienteId, medicoId, franjaId);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCita.CONFIRMADA);
        assertThat(resultado.isNotificacionEnviada()).isFalse();
    }
}
