package com.pos.dto;

import lombok.Data;

@Data
public class EvenimentDTO {

    private Integer id;
    private Integer idOwner;
    private String nume;
    private String locatie;
    private String descriere;
    private Integer numarLocuri;
    private Integer bileteDisponibile;

}