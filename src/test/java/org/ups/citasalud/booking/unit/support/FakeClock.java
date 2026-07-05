package org.ups.citasalud.booking.unit.support;

import java.time.LocalDateTime;

import org.ups.citasalud.booking.domain.port.Clock;

/**
 * Clock manipulable para pruebas deterministas de comportamiento
 * dependiente del tiempo (FR-007, FR-010) sin esperas reales.
 */
public class FakeClock implements Clock {

    private LocalDateTime ahora;

    public FakeClock(LocalDateTime inicial) {
        this.ahora = inicial;
    }

    @Override
    public LocalDateTime ahora() {
        return ahora;
    }

    public void avanzar(java.time.Duration duracion) {
        this.ahora = this.ahora.plus(duracion);
    }

    public void fijar(LocalDateTime nuevoAhora) {
        this.ahora = nuevoAhora;
    }
}
