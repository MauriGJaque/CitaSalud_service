package org.ups.citasalud.booking.domain.port;

/**
 * Señala que una franja horaria ya no está DISPONIBLE cuando se intenta
 * retener o confirmar (FR-005, FR-006, FR-008).
 */
public class FranjaNoDisponibleException extends RuntimeException {

    public FranjaNoDisponibleException(String message) {
        super(message);
    }
}
