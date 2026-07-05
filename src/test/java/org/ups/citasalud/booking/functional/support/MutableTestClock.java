package org.ups.citasalud.booking.functional.support;

import java.time.Duration;
import java.time.LocalDateTime;

import org.ups.citasalud.booking.domain.port.Clock;

/**
 * Clock de prueba manipulable para los escenarios Cucumber, de forma que
 * el Escenario 3 (liberación tras 5 minutos, FR-010) no dependa de una
 * espera real de 5 minutos.
 */
public class MutableTestClock implements Clock {

    private LocalDateTime ahora = LocalDateTime.now();

    @Override
    public synchronized LocalDateTime ahora() {
        return ahora;
    }

    public synchronized void avanzar(Duration duracion) {
        this.ahora = this.ahora.plus(duracion);
    }

    public synchronized void reiniciar() {
        this.ahora = LocalDateTime.now();
    }
}
