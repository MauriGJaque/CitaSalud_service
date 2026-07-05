package org.ups.citasalud.booking.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.ups.citasalud.booking.domain.port.ReservaInvalidaException;

/**
 * FR-007, Edge Case 4: una franja pasada o fuera del horario del médico
 * responde 422.
 */
@WebMvcTest(CitasController.class)
class ReservaCitaFranjaInvalidaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservarCitaUseCase reservarCitaUseCase;
    @MockitoBean
    private ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    @MockitoBean
    private SeleccionarFranjaUseCase seleccionarFranjaUseCase;

    @Test
    void franjaPasadaOFueraDeHorarioResponde422() throws Exception {
        when(reservarCitaUseCase.ejecutar(any(), any(), any()))
                .thenThrow(new ReservaInvalidaException("La franja no es futura o está fuera del horario del médico"));

        String body = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/citas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }
}
