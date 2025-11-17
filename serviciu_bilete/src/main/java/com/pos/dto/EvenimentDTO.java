package com.pos.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import java.util.Map;


@Getter
@Setter
public class EvenimentDTO extends RepresentationModel<EvenimentDTO>{

    private Integer id;
    private Integer idOwner;
    private String nume;
    private String locatie;
    private String descriere;
    private Integer numarLocuri;
    private Integer bileteDisponibile;



}