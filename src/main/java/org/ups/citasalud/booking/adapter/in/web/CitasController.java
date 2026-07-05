package org.ups.citasalud.booking.adapter.in.web;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.ups.citasalud.booking.adapter.in.web.generated.api.DisponibilidadApi;
import org.ups.citasalud.booking.adapter.in.web.generated.api.ReservasApi;
import org.ups.citasalud.booking.adapter.in.web.generated.model.Cita;
import org.ups.citasalud.booking.adapter.in.web.generated.model.FranjaHoraria;
import org.ups.citasalud.booking.adapter.in.web.generated.model.ReservaCitaRequest;
import org.ups.citasalud.booking.application.usecase.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.booking.application.usecase.ReservarCitaUseCase;
import org.ups.citasalud.booking.application.usecase.SeleccionarFranjaUseCase;

/**
 * Adapter de entrada: implementa las interfaces generadas por
 * openapi-generator a partir de `citas-api.yaml` (Principio IV) y traduce
 * entre los DTO del contrato HTTP y los modelos de dominio.
 */
@RestController
public class CitasController implements ReservasApi, DisponibilidadApi {

    private static final ZoneId ZONA = ZoneId.systemDefault();

    private final ReservarCitaUseCase reservarCitaUseCase;
    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    private final SeleccionarFranjaUseCase seleccionarFranjaUseCase;

    public CitasController(ReservarCitaUseCase reservarCitaUseCase,
                            ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase,
                            SeleccionarFranjaUseCase seleccionarFranjaUseCase) {
        this.reservarCitaUseCase = reservarCitaUseCase;
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
        this.seleccionarFranjaUseCase = seleccionarFranjaUseCase;
    }

    @Override
    public ResponseEntity<Cita> reservarCita(ReservaCitaRequest reservaCitaRequest) {
        org.ups.citasalud.booking.domain.model.Cita cita = reservarCitaUseCase.ejecutar(
                reservaCitaRequest.getPacienteId(),
                reservaCitaRequest.getMedicoId(),
                reservaCitaRequest.getFranjaHorariaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(cita));
    }

    @Override
    public ResponseEntity<List<FranjaHoraria>> listarFranjasDisponibles(UUID medicoId, OffsetDateTime desde) {
        List<FranjaHoraria> franjas = consultarDisponibilidadUseCase
                .ejecutar(medicoId, desde == null ? null : desde.atZoneSameInstant(ZONA).toLocalDateTime())
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(franjas);
    }

    @Override
    public ResponseEntity<FranjaHoraria> seleccionarFranja(UUID franjaId) {
        org.ups.citasalud.booking.domain.model.FranjaHoraria franja = seleccionarFranjaUseCase.ejecutar(franjaId);
        return ResponseEntity.ok(toDto(franja));
    }

    private Cita toDto(org.ups.citasalud.booking.domain.model.Cita cita) {
        return new Cita(
                cita.getId(),
                cita.getPacienteId(),
                cita.getMedicoId(),
                cita.getFranjaHorariaId(),
                Cita.EstadoEnum.fromValue(cita.getEstado().name()),
                cita.isNotificacionEnviada(),
                cita.getCreadaEn().atZone(ZONA).toOffsetDateTime());
    }

    private FranjaHoraria toDto(org.ups.citasalud.booking.domain.model.FranjaHoraria franja) {
        return new FranjaHoraria(
                franja.getId(),
                franja.getMedicoId(),
                franja.getFechaHoraInicio().atZone(ZONA).toOffsetDateTime(),
                franja.getFechaHoraFin().atZone(ZONA).toOffsetDateTime(),
                FranjaHoraria.EstadoEnum.fromValue(franja.getEstado().name()));
    }
}
