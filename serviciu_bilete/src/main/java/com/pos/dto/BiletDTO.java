package com.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Schema(description = "Ticket data transfer object")
public class BiletDTO extends RepresentationModel<BiletDTO> {

    @Schema(description = "Unique ticket code (auto-generated)", example = "BILET-a1b2c3d4", accessMode = Schema.AccessMode.READ_ONLY)
    private String cod;

    @Schema(description = "Event ID associated with this ticket (null if associated with package)", example = "1")
    private Integer evenimentId;

    @Schema(description = "Package ID associated with this ticket (null if associated with event)", example = "2")
    private Integer pachetId;

    @Schema(description = "Event name (read-only helper field)", example = "Summer Concert", accessMode = Schema.AccessMode.READ_ONLY)
    private String evenimentNume;

    @Schema(description = "Package name (read-only helper field)", example = "Premium Festival Pass", accessMode = Schema.AccessMode.READ_ONLY)
    private String pachetNume;


}