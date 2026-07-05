package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.booking.domain.model.Medico;
import org.ups.citasalud.booking.domain.model.RangoAtencion;

class MedicoTest {

    @Test
    void atiendeEnEsTrueDentroDelRangoYFalseFueraDeEl() {
        UUID id = UUID.randomUUID();
        Medico medico = new Medico(id, "Dra. Prueba", "Medicina General",
                List.of(new RangoAtencion(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))));

        LocalDateTime dentro = LocalDateTime.of(2026, 7, 6, 10, 0); // lunes
        LocalDateTime fueraDeHora = LocalDateTime.of(2026, 7, 6, 20, 0); // lunes, fuera de horario
        LocalDateTime otroDia = LocalDateTime.of(2026, 7, 7, 10, 0); // martes

        assertThat(medico.atiendeEn(dentro)).isTrue();
        assertThat(medico.atiendeEn(fueraDeHora)).isFalse();
        assertThat(medico.atiendeEn(otroDia)).isFalse();

        assertThat(medico.getId()).isEqualTo(id);
        assertThat(medico.getNombreCompleto()).isEqualTo("Dra. Prueba");
        assertThat(medico.getEspecialidad()).isEqualTo("Medicina General");
        assertThat(medico.getHorarioAtencion()).hasSize(1);
    }
}
