package org.ups.citasalud.booking.domain.port;

import java.time.LocalDateTime;

/**
 * Abstrae la obtención del instante actual para que la lógica de dominio
 * dependiente del tiempo (FR-007, FR-010) sea determinística en pruebas.
 */
public interface Clock {

    LocalDateTime ahora();
}
