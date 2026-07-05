package org.ups.citasalud.booking.domain.port;

import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.Paciente;

public interface NotificacionPort {

    /**
     * Intenta enviar la confirmación de la Cita al paciente por WhatsApp.
     * MUST no lanzar excepción que revierta la reserva ante fallo (FR-009):
     * un fallo se refleja devolviendo {@code false}, nunca propagando una
     * excepción al llamador.
     *
     * @return true si la notificación se envió exitosamente
     */
    boolean enviarConfirmacion(Cita cita, Paciente paciente);
}
