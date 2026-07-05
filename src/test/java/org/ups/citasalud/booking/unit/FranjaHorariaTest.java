package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.booking.domain.model.EstadoFranja;
import org.ups.citasalud.booking.domain.model.FranjaHoraria;

class FranjaHorariaTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 7, 6, 10, 0);

    @Test
    void esFuturaSoloCuandoInicioEsPosteriorAAhora() {
        FranjaHoraria futura = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.DISPONIBLE, null);
        FranjaHoraria pasada = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.minusHours(1),
                T0.minusHours(1).plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        assertThat(futura.esFutura(T0)).isTrue();
        assertThat(pasada.esFutura(T0)).isFalse();
    }

    @Test
    void retenerCambiaEstadoYFijaVencimiento() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        franja.retener(T0, Duration.ofMinutes(5));

        assertThat(franja.getEstado()).isEqualTo(EstadoFranja.RETENIDA);
        assertThat(franja.getRetenidaHasta()).isEqualTo(T0.plusMinutes(5));
        assertThat(franja.estaDisponible()).isFalse();
    }

    @Test
    void retenerFranjaNoDisponibleLanzaExcepcion() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.OCUPADA, null);

        assertThatThrownBy(() -> franja.retener(T0, Duration.ofMinutes(5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void confirmarMarcaLaFranjaComoOcupadaYLimpiaRetencion() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.RETENIDA, T0.plusMinutes(5));

        franja.confirmar();

        assertThat(franja.getEstado()).isEqualTo(EstadoFranja.OCUPADA);
        assertThat(franja.getRetenidaHasta()).isNull();
    }

    @Test
    void confirmarFranjaYaOcupadaLanzaExcepcion() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.OCUPADA, null);

        assertThatThrownBy(franja::confirmar).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void liberarSiRetencionExpiradaNoCambiaEstadoDisponible() {
        FranjaHoraria franja = new FranjaHoraria(UUID.randomUUID(), UUID.randomUUID(), T0.plusHours(1),
                T0.plusHours(1).plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        franja.liberarSiRetencionExpirada(T0);

        assertThat(franja.getEstado()).isEqualTo(EstadoFranja.DISPONIBLE);
    }

    @Test
    void gettersExponenLosCamposDeLaFranja() {
        UUID id = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        FranjaHoraria franja = new FranjaHoraria(id, medicoId, T0, T0.plusMinutes(30), EstadoFranja.DISPONIBLE, null);

        assertThat(franja.getId()).isEqualTo(id);
        assertThat(franja.getMedicoId()).isEqualTo(medicoId);
        assertThat(franja.getFechaHoraInicio()).isEqualTo(T0);
        assertThat(franja.getFechaHoraFin()).isEqualTo(T0.plusMinutes(30));
    }
}
