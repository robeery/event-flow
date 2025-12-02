package com.pos.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class PachetDTO extends RepresentationModel<PachetDTO> {

    private Integer id;
    private Integer idOwner;
    private String nume;
    private String locatie;
    private String descriere;
    private Integer numarLocuri;
    private Integer bileteDisponibile;
}