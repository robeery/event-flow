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
    @Setter
    //AICI MA MAI GANDESC PANA LA URMA CE FEL DE GENERATIONTYPE FOLOSESC
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "evenimente_seq")
    @SequenceGenerator(
            name = "evenimente_seq",
            sequenceName = "evenimente_id_seq",
            allocationSize = 1
    )
    private Integer id;

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
