package org.ups.citasalud.booking.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.ups.citasalud.booking.domain.model.Paciente;

class PacienteTest {

    @Test
    void gettersExponenLosCamposDelPaciente() {
        UUID id = UUID.randomUUID();
        Paciente paciente = new Paciente(id, "Ana Torres", "+593987654321");

        assertThat(paciente.getId()).isEqualTo(id);
        assertThat(paciente.getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(paciente.getNumeroWhatsapp()).isEqualTo("+593987654321");
    }
}
