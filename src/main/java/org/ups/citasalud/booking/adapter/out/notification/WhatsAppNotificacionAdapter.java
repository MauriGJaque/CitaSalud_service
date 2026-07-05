package org.ups.citasalud.booking.adapter.out.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.ups.citasalud.booking.domain.model.Cita;
import org.ups.citasalud.booking.domain.model.Paciente;
import org.ups.citasalud.booking.domain.port.NotificacionPort;

/**
 * Adapter de salida hacia un proveedor de WhatsApp Business API. FR-009:
 * un fallo de envío NUNCA debe propagarse como excepción — se registra y se
 * devuelve {@code false} para que la reserva permanezca válida.
 */
@Component
public class WhatsAppNotificacionAdapter implements NotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificacionAdapter.class);

    @Override
    public boolean enviarConfirmacion(Cita cita, Paciente paciente) {
        try {
            enviar(paciente.getNumeroWhatsapp(), cita);
            return true;
        } catch (Exception e) {
            log.warn("Fallo al enviar confirmación por WhatsApp para la cita {} al número {}: {}",
                    cita.getId(), paciente.getNumeroWhatsapp(), e.getMessage());
            return false;
        }
    }

    /**
     * Punto de integración con el proveedor real de WhatsApp Business API.
     * En este entorno de desarrollo/demo no hay proveedor configurado, por
     * lo que solo se registra el intento (ver research.md #3).
     */
    private void enviar(String numeroWhatsapp, Cita cita) {
        if (numeroWhatsapp == null || numeroWhatsapp.isBlank()) {
            throw new IllegalArgumentException("El paciente no tiene un número de WhatsApp válido");
        }
        log.info("Notificación WhatsApp enviada a {} confirmando la cita {}", numeroWhatsapp, cita.getId());
    }
}
