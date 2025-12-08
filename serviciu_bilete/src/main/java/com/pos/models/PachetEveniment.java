package com.pos.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Aceasta este entitatea pentru tabela JOIN_PE
// Are o cheie primară compusa (PachetID, EvenimentID) si o coloana extra (numarLocuri)
@Entity
@Table(name = "join_pe")
@Getter
@Setter
public class PachetEveniment {

    @EmbeddedId
    private PachetEvenimentId id;

    // relatia ManyToOne catre Pachet
    @ManyToOne
    @MapsId("pachetId") // Mapează partea 'pachetId' a @EmbeddedId
    @JoinColumn(name = "pachet_id")
    private Pachet pachet;

    // Relatia ManyToOne catre Eveniment
    @ManyToOne
    @MapsId("evenimentId") // Mapeaza partea 'evenimentId' a @EmbeddedId
    @JoinColumn(name = "eveniment_id")
    private Eveniment eveniment;

    @Column(name = "numar_locuri")
    private Integer numarLocuri;

    public PachetEveniment(Pachet pachetSalvat, Eveniment evenimentSalvat, int i) {
    }

    public PachetEveniment() {

    }
}
