package com.pos.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "evenimente")
@Getter
@Setter
public class Eveniment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_owner", nullable = false)
    private int idOwner; // momentan int simplu

    @Column(unique = true, nullable = false)
    private String nume;

    private String locatie;

    private String descriere;

    private Integer numarLocuri;

    // relatia catre tabela de join 'PachetEveniment'
    @OneToMany(mappedBy = "eveniment")
    private Set<PachetEveniment> pacheteAsociate;

    // relatia catre bilete
    @OneToMany(mappedBy = "eveniment")
    private Set<Bilet> bilete;
}
