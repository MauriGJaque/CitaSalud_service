package org.ups.citasalud.booking.domain.model;

import java.util.UUID;

public class Paciente {

    private final UUID id;
    private final String nombreCompleto;
    private final String numeroWhatsapp;

    public Paciente(UUID id, String nombreCompleto, String numeroWhatsapp) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.numeroWhatsapp = numeroWhatsapp;
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
