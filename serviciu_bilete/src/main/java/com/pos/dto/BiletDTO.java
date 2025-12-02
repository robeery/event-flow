package com.pos.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class BiletDTO extends RepresentationModel<BiletDTO> {

    private String cod;
    private Integer evenimentId;
    private Integer pachetId;

    //campuri ajutatoare pentru readonly, might remove later
    private String evenimentNume;
    private String pachetNume;


}