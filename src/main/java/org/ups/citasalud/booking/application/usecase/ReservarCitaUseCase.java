package org.ups.citasalud.booking.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.EstadoCita;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.model.Medico;
import org.ups.citasalud.booking.domain.model.Paciente;
import org.ups.citasalud.booking.domain.port.CitaRepositoryPort;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.domain.port.FranjaHorariaRepositoryPort;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;
import org.ups.citasalud.booking.domain.port.MedicoRepositoryPort;
import org.ups.citasalud.booking.domain.port.NotificacionPort;
import org.ups.citasalud.booking.domain.port.PacienteRepositoryPort;
import org.ups.citasalud.booking.domain.port.ReservaInvalidaException;

/**
 * US-01: reserva de cita en línea 24/7 (FR-001 a FR-009).
 */
public class ReservarCitaUseCase {

    private final CitaRepositoryPort citaRepository;
    private final FranjaHorariaRepositoryPort franjaHorariaRepository;
    private final MedicoRepositoryPort medicoRepository;
    private final PacienteRepositoryPort pacienteRepository;
    private final NotificacionPort notificacionPort;
    private final Clock clock;

    public ReservarCitaUseCase(CitaRepositoryPort citaRepository,
                                FranjaHorariaRepositoryPort franjaHorariaRepository,
                                MedicoRepositoryPort medicoRepository,
                                PacienteRepositoryPort pacienteRepository,
                                NotificacionPort notificacionPort,
                                Clock clock) {
        this.citaRepository = citaRepository;
        this.franjaHorariaRepository = franjaHorariaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.notificacionPort = notificacionPort;
        this.clock = clock;
    }

    public Cita ejecutar(UUID pacienteId, UUID medicoId, UUID franjaHorariaId) {
        FranjaHoraria franja = franjaHorariaRepository.buscarPorId(franjaHorariaId)
                .orElseThrow(() -> new FranjaNoDisponibleException("Franja horaria no encontrada: " + franjaHorariaId));

        Medico medico = medicoRepository.buscarPorId(medicoId)
                .orElseThrow(() -> new ReservaInvalidaException("Médico no encontrado: " + medicoId));

        Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new ReservaInvalidaException("Paciente no encontrado: " + pacienteId));

        LocalDateTime ahora = clock.ahora();

        // FR-007: solo franjas futuras y dentro del horario del médico.
        if (!franja.esFutura(ahora) || !medico.atiendeEn(franja.getFechaHoraInicio())) {
            throw new ReservaInvalidaException(
                    "La franja no es futura o está fuera del horario del médico: " + franjaHorariaId);
        }

        Cita nuevaCita = new Cita(UUID.randomUUID(), pacienteId, medicoId, franjaHorariaId,
                EstadoCita.CONFIRMADA, false, ahora);

        // FR-003, FR-005, FR-006, FR-008: solo se confirma si la franja sigue disponible.
        Cita citaGuardada = citaRepository.guardarSiFranjaDisponible(nuevaCita);

        // FR-004, FR-009: el fallo de notificación no invalida la reserva ya registrada.
        boolean notificada = notificacionPort.enviarConfirmacion(citaGuardada, paciente);
        return citaRepository.actualizarNotificacion(citaGuardada.getId(), notificada);
    }
}
