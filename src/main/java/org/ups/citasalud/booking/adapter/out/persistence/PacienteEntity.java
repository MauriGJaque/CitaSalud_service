package org.ups.citasalud.booking.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "paciente")
public class PacienteEntity {

    @Id
    private UUID id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "numero_whatsapp", nullable = false)
    private String numeroWhatsapp;

    protected PacienteEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getNumeroWhatsapp() {
        return numeroWhatsapp;
    }
}
