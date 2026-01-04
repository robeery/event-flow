package com.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import java.util.Map;


@Getter
@Setter
@Schema(description = "Event data transfer object")
public class EvenimentDTO extends RepresentationModel<EvenimentDTO>{

    @Schema(description = "Event ID (auto-generated)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "Owner ID", example = "1")
    private Integer idOwner;

    @Schema(description = "Event name", example = "Festival de Arta", required = true)
    private String nume;

    @Schema(description = "Event location", example = "Parc Vasile Alecsandri", required = true)
    private String locatie;

    @Schema(description = "Event description", example = "Pentru toata lumea")
    private String descriere;

    @Schema(description = "Total number of seats", example = "300", required = true)
    private Integer numarLocuri;

    @Schema(description = "Number of available tickets (auto-calculated)", example = "300", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer bileteDisponibile;



}