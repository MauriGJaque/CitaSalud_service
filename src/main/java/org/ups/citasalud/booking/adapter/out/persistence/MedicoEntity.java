package org.ups.citasalud.booking.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "medico")
public class MedicoEntity {

    @Id
    private UUID id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "especialidad", nullable = false)
    private String especialidad;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medico_horario", joinColumns = @JoinColumn(name = "medico_id"))
    private List<RangoAtencionEmbeddable> horarioAtencion;

    protected MedicoEntity() {
    }

    public UUID getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public List<RangoAtencionEmbeddable> getHorarioAtencion() {
        return horarioAtencion;
    }
}
