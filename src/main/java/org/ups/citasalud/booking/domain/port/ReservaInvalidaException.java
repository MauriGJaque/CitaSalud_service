package org.ups.citasalud.booking.domain.port;

/**
 * Señala que la reserva solicitada no cumple una condición obligatoria:
 * franja no futura, fuera del horario del médico (FR-007), o referencia a
 * un médico/paciente/franja inexistente.
 */
public class ReservaInvalidaException extends RuntimeException {

    public ReservaInvalidaException(String message) {
        super(message);
    }
}
