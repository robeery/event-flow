package com.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Schema(description = "Package-Event association data transfer object")
public class PachetEvenimentCreateDTO extends RepresentationModel <PachetEvenimentCreateDTO>{

    @Schema(description = "Package ID", example = "2", required = true)
    private Integer pachetId;

    @Schema(description = "Event ID", example = "1", required = true)
    private Integer evenimentId;

}