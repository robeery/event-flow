package com.pos.dto;



import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class PachetEvenimentCreateDTO extends RepresentationModel <PachetEvenimentCreateDTO>{
    private Integer pachetId;
    private Integer evenimentId;

}