package org.ups.citasalud.booking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaEntity;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaJpaRepository;
import org.ups.citasalud.booking.domain.model.EstadoFranja;

/**
 * FR-008, SC-003: ante dos solicitudes de reserva concurrentes sobre la
 * misma franja, exactamente una debe confirmarse (201) y la otra debe
 * rechazarse (409) — nunca ambas.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ReservaCitaConcurrenciaIT {

    private static final UUID MEDICO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    @Test
    void dosSolicitudesConcurrentesSobreLaMismaFranjaSoloUnaSeConfirma() throws Exception {
        UUID franjaId = UUID.randomUUID();
        LocalDateTime inicio = LocalDateTime.now().plusDays(30).withNano(0);
        franjaHorariaJpaRepository.saveAndFlush(new FranjaHorariaEntity(franjaId, MEDICO_ID, inicio,
                inicio.plusMinutes(30), EstadoFranja.DISPONIBLE, null));

        String body = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(PACIENTE_ID, MEDICO_ID, franjaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<ResponseEntity<String>> reservar = () ->
                restTemplate.postForEntity("http://localhost:" + port + "/api/v1/citas", request, String.class);

        Future<ResponseEntity<String>> resultado1 = executor.submit(reservar);
        Future<ResponseEntity<String>> resultado2 = executor.submit(reservar);

        List<HttpStatus> estados = List.of(
                HttpStatus.valueOf(resultado1.get().getStatusCode().value()),
                HttpStatus.valueOf(resultado2.get().getStatusCode().value()));
        executor.shutdown();

        assertThat(estados).containsExactlyInAnyOrder(HttpStatus.CREATED, HttpStatus.CONFLICT);
    }
}
