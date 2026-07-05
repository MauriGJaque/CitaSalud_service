package org.ups.citasalud.booking.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Rango de horario de atención de un médico para un día de la semana dado.
 */
public record RangoAtencion(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {

    public boolean cubre(DayOfWeek dia, LocalTime hora) {
        return diaSemana == dia && !hora.isBefore(horaInicio) && hora.isBefore(horaFin);
    }
}
