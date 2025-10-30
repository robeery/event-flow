package com.pos.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


//to do's
//vezi daca pot pune constrangeri la coloane/relatii la jakarta/lombok (i.e. pachet > 10 sau ceva similar)
//director model si arhitectura mvc
//spring hateoas

@Entity
@Table(name = "bilete")
@Getter
@Setter
public class Bilet {

    @Id
    @Column(name = "cod")
    private String cod; // primary key (nu auto-increment)

    // relatia ManyToOne catre Pachet (un bilet poate fi pentru un pachet)
    @ManyToOne
    @JoinColumn(name = "pachet_id") // Numele coloanei FK
    private Pachet pachet;

    // Relația ManyToOne catre Eveniment (un bilet poate fi pentru un eveniment)
    @ManyToOne
    @JoinColumn(name = "eveniment_id") // Numele coloanei FK
    private Eveniment eveniment;
}

