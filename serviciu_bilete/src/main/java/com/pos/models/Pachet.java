package com.pos.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "pachete")
@Getter
@Setter
public class Pachet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_owner", nullable = false)
    private int idOwner;

    @Column(unique = true, nullable = false)
    private String nume;

    @Column(nullable = false)
    private String locatie;

    private String descriere;

    // relatia catre tabela de join 'PachetEveniment'
    @OneToMany(mappedBy = "pachet")
    private Set<PachetEveniment> evenimenteAsociate;

    // relatia catre bilete
    @OneToMany(mappedBy = "pachet")
    private Set<Bilet> bilete;
}
