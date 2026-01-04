package com.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Schema(description = "Event package data transfer object")
public class PachetDTO extends RepresentationModel<PachetDTO> {

    @Schema(description = "Package ID (auto-generated)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "Owner ID", example = "1")
    private Integer idOwner;

    @Schema(description = "Package name", example = "Pachet Festival Vara", required = true)
    private String nume;

    @Schema(description = "Package location", example = "Parc Central", required = true)
    private String locatie;

    @Schema(description = "Package description", example = "Toate evenimentele de vara")
    private String descriere;

    @Schema(description = "Total number of package seats", example = "500", required = true)
    private Integer numarLocuri;

    @Schema(description = "Number of available package tickets (auto-calculated)", example = "500", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer bileteDisponibile;
}