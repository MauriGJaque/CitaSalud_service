package org.ups.citasalud.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.ups.citasalud.booking.adapter.in.web.generated.model.Error;
import org.ups.citasalud.booking.domain.port.FranjaNoDisponibleException;
import org.ups.citasalud.booking.domain.port.ReservaInvalidaException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(FranjaNoDisponibleException.class)
    public ResponseEntity<Error> handleFranjaNoDisponible(FranjaNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Error("FRANJA_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(ReservaInvalidaException.class)
    public ResponseEntity<Error> handleReservaInvalida(ReservaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new Error("RESERVA_INVALIDA", ex.getMessage()));
    }
}
