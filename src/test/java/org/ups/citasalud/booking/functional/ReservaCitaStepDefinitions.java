package org.ups.citasalud.booking.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaEntity;
import org.ups.citasalud.booking.adapter.out.persistence.FranjaHorariaJpaRepository;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.port.Clock;
import org.ups.citasalud.booking.functional.support.MutableTestClock;

public class ReservaCitaStepDefinitions {

    private static final UUID MEDICO_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    @Autowired
    private Clock clock;

    private UUID franjaId;
    private ResponseEntity<Map> ultimaRespuestaReserva;
    private ResponseEntity<Map[]> ultimaRespuestaFranjas;

    @Before
    public void reiniciarClock() {
        if (clock instanceof MutableTestClock mutableTestClock) {
            mutableTestClock.reiniciar();
        }
    }

    @Dado("que existe un médico con franjas horarias disponibles")
    public void queExisteUnMedicoConFranjasHorariasDisponibles() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(60).withNano(0).plusSeconds(new java.util.Random().nextInt(100000));
        franjaId = UUID.randomUUID();
        franjaHorariaJpaRepository.saveAndFlush(new FranjaHorariaEntity(franjaId, MEDICO_ID, inicio,
                inicio.plusMinutes(30), EstadoFranja.DISPONIBLE, null));
    }

    @Dado("que el paciente accede al sistema fuera del horario de atención telefónica")
    public void queElPacienteAccedeFueraDeHorarioTelefonico() {
        // FR-001: el sistema no impone restricción horaria; no se requiere preparación adicional.
    }

    @Cuando("elige médico, fecha y hora disponibles y confirma")
    public void eligeMedicoFechaYHoraDisponiblesYConfirma() {
        ultimaRespuestaReserva = reservar(franjaId);
    }

    @Entonces("la cita queda registrada")
    public void laCitaQuedaRegistrada() {
        assertThat(ultimaRespuestaReserva.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(ultimaRespuestaReserva.getBody().get("estado")).isEqualTo("CONFIRMADA");
    }

    @Y("el paciente recibe confirmación por WhatsApp")
    public void elPacienteRecibeConfirmacionPorWhatsapp() {
        assertThat(ultimaRespuestaReserva.getBody().get("notificacionEnviada")).isEqualTo(true);
    }

    @Dado("que el paciente intenta seleccionar una franja ya ocupada")
    public void queElPacienteIntentaSeleccionarUnaFranjaYaOcupada() {
        ResponseEntity<Map> primeraReserva = reservar(franjaId);
        assertThat(primeraReserva.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Cuando("intenta confirmarla")
    public void intentaConfirmarla() {
        ultimaRespuestaReserva = reservar(franjaId);
    }

    @Entonces("el sistema la muestra como no disponible")
    public void elSistemaLaMuestraComoNoDisponible() {
        assertThat(ultimaRespuestaReserva.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Y("lo invita a elegir otra franja")
    public void loInvitaAElegirOtraFranja() {
        assertThat(ultimaRespuestaReserva.getBody().get("codigo")).isEqualTo("FRANJA_NO_DISPONIBLE");
    }

    @Dado("que el paciente selecciona una franja horaria pero no confirma la reserva")
    public void queElPacienteSeleccionaUnaFranjaHorariaPeroNoConfirmaLaReserva() {
        ResponseEntity<Map> respuesta = restTemplate.postForEntity(
                baseUrl() + "/franjas/" + franjaId + "/seleccion", null, Map.class);
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody().get("estado")).isEqualTo("RETENIDA");
    }

    @Cuando("transcurren 5 minutos sin confirmación")
    public void transcurren5MinutosSinConfirmacion() {
        ((MutableTestClock) clock).avanzar(Duration.ofMinutes(5).plusSeconds(1));
        ultimaRespuestaFranjas = restTemplate.getForEntity(
                baseUrl() + "/medicos/" + MEDICO_ID + "/franjas", Map[].class);
    }

    @Entonces("la franja vuelve al estado DISPONIBLE")
    public void laFranjaVuelveAlEstadoDisponible() {
        assertThat(ultimaRespuestaFranjas.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map> franjas = List.of(ultimaRespuestaFranjas.getBody());
        boolean disponibleDeNuevo = franjas.stream()
                .anyMatch(f -> franjaId.toString().equals(f.get("id")) && "DISPONIBLE".equals(f.get("estado")));
        assertThat(disponibleDeNuevo).isTrue();
    }

    private ResponseEntity<Map> reservar(UUID franjaId) {
        String body = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(PACIENTE_ID, MEDICO_ID, franjaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(baseUrl() + "/citas", new HttpEntity<>(body, headers), Map.class);
    }

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }
}
