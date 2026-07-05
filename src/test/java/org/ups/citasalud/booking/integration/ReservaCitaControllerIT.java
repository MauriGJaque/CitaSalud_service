package org.ups.citasalud.booking.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ups.citasalud.booking.adapter.in.web.CitasController;
import org.ups.citasalud.booking.application.usecase.ConsultarDisponibilidadUseCase;
import org.ups.citasalud.booking.application.usecase.ReservarCitaUseCase;
import org.ups.citasalud.booking.application.usecase.SeleccionarFranjaUseCase;
import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.EstadoCita;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;

/**
 * FR-002 a FR-006, FR-010: contrato HTTP de disponibilidad, selección y
 * reserva (códigos 200/201/409).
 */
@WebMvcTest(CitasController.class)
class ReservaCitaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservarCitaUseCase reservarCitaUseCase;
    @MockitoBean
    private ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    @MockitoBean
    private SeleccionarFranjaUseCase seleccionarFranjaUseCase;

    @Test
    void listarFranjasDisponiblesRespondeConLasFranjasDelMedico() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(consultarDisponibilidadUseCase.ejecutar(any(), any())).thenReturn(List.of(
                new FranjaHoraria(franjaId, medicoId, inicio, inicio.plusMinutes(30), EstadoFranja.DISPONIBLE, null)));

        mockMvc.perform(get("/medicos/{medicoId}/franjas", medicoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(franjaId.toString()))
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
    }

    @Test
    void reservarCitaExitosaResponde201ConCitaConfirmada() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        UUID citaId = UUID.randomUUID();

        when(reservarCitaUseCase.ejecutar(pacienteId, medicoId, franjaId)).thenReturn(
                new Cita(citaId, pacienteId, medicoId, franjaId, EstadoCita.CONFIRMADA, true, LocalDateTime.now()));

        String body = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(pacienteId, medicoId, franjaId);

        mockMvc.perform(post("/citas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(citaId.toString()))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.notificacionEnviada").value(true));
    }

    @Test
    void reservarCitaSobreFranjaOcupadaResponde409() throws Exception {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();

        when(reservarCitaUseCase.ejecutar(any(), any(), any()))
                .thenThrow(new FranjaNoDisponibleException("La franja ya está ocupada"));

        String body = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(pacienteId, medicoId, franjaId);

        mockMvc.perform(post("/citas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void seleccionarFranjaExitosaResponde200ConFranjaRetenida() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        when(seleccionarFranjaUseCase.ejecutar(franjaId)).thenReturn(
                new FranjaHoraria(franjaId, medicoId, inicio, inicio.plusMinutes(30), EstadoFranja.RETENIDA,
                        LocalDateTime.now().plusMinutes(5)));

        mockMvc.perform(post("/franjas/{franjaId}/seleccion", franjaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RETENIDA"));
    }

    @Test
    void seleccionarFranjaYaRetenidaResponde409() throws Exception {
        UUID franjaId = UUID.randomUUID();
        when(seleccionarFranjaUseCase.ejecutar(franjaId))
                .thenThrow(new FranjaNoDisponibleException("La franja no está disponible para retener"));

        mockMvc.perform(post("/franjas/{franjaId}/seleccion", franjaId))
                .andExpect(status().isConflict());
    }
}
