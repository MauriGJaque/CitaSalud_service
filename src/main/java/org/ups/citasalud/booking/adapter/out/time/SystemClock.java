package org.ups.citasalud.booking.adapter.out.time;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.ups.citasalud.booking.domain.port.Clock;

@Component
public class SystemClock implements Clock {

    @Override
    public LocalDateTime ahora() {
        return LocalDateTime.now();
    }
}
