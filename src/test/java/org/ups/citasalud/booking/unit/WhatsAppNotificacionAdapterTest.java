package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.booking.adapter.out.notification.WhatsAppNotificacionAdapter;
import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.EstadoCita;
import org.ups.citasalud.booking.domain.model.Paciente;

/**
 * FR-004, FR-009: el envío de confirmación nunca lanza excepción; un
 * número de contacto inválido se refleja como fallo (false), no como error.
 */
class WhatsAppNotificacionAdapterTest {

    private final WhatsAppNotificacionAdapter adapter = new WhatsAppNotificacionAdapter();

    @Test
    void enviarConfirmacionConNumeroValidoRetornaTrue() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                EstadoCita.CONFIRMADA, false, LocalDateTime.now());
        Paciente paciente = new Paciente(UUID.randomUUID(), "Ana Torres", "+593987654321");

        assertThat(adapter.enviarConfirmacion(cita, paciente)).isTrue();
    }

    @Test
    void enviarConfirmacionConNumeroInvalidoRetornaFalseSinLanzarExcepcion() {
        Cita cita = new Cita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                EstadoCita.CONFIRMADA, false, LocalDateTime.now());
        Paciente paciente = new Paciente(UUID.randomUUID(), "Paciente Sin Numero", null);

        assertThat(adapter.enviarConfirmacion(cita, paciente)).isFalse();
    }
}
