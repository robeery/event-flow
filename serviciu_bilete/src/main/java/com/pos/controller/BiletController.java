package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import com.pos.security.AuthorizationHelper;
import com.pos.service.BiletService;
import com.pos.service.EvenimentService;
import com.pos.service.PachetService;
import com.pos.util.HateoasHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/event-manager/tickets")
@RequiredArgsConstructor
public class BiletController {

    private final BiletService biletService;
    private final HateoasHelper hateoasHelper;
    private final AuthorizationHelper authHelper;
    private final EvenimentService evenimentService;
    private final PachetService pachetService;

    @Operation(summary = "Retrieve all tickets",
               description = "Returns a collection of all tickets in the system with HATEOAS links for navigation and ticket creation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all tickets")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<BiletDTO>> getAllBilete() {
        List<BiletDTO> bilete = biletService.findAll();
        hateoasHelper.addLinksToBilete(bilete);

        CollectionModel<BiletDTO> collectionModel = CollectionModel.of(bilete);
        collectionModel.add(linkTo(methodOn(BiletController.class).getAllBilete()).withSelfRel().withType("GET"));
        collectionModel.add(linkTo(methodOn(BiletController.class).createBilet(null, null)).withRel("create").withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Retrieve ticket by code",
               description = "Returns a single ticket identified by its unique code with HATEOAS links")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found with the specified code")
    })
    @GetMapping("/{cod}")
    public ResponseEntity<BiletDTO> getBiletByCod(
            @Parameter(description = "Unique ticket code identifier", example = "BILET-a1b2c3d4")
            @PathVariable String cod) {
        BiletDTO bilet = biletService.findByCod(cod);
        hateoasHelper.addLinksToBilet(bilet);
        return ResponseEntity.ok(bilet);
    }

    @Operation(summary = "Create a new ticket",
               description = "Creates a new ticket with an auto-generated unique code. Returns the created ticket with HTTP 201 status. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PostMapping
    public ResponseEntity<BiletDTO> createBilet(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"evenimentId\": 1}"
                            )
                    )
            )
            @RequestBody BiletDTO biletDTO,
            HttpServletRequest request) {
        // Authorization: any authenticated user can purchase tickets
        authHelper.requireCanPurchaseTicket(request);

        BiletDTO createdBilet = biletService.create(biletDTO);
        hateoasHelper.addLinksToBilet(createdBilet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBilet);
    }

    private Integer getOwnerIdForTicket(BiletDTO biletDTO) {
        if (biletDTO.getEvenimentId() != null) {
            EvenimentDTO ev = evenimentService.findById(biletDTO.getEvenimentId());
            return ev.getIdOwner();
        } else if (biletDTO.getPachetId() != null) {
            PachetDTO pachet = pachetService.findById(biletDTO.getPachetId());
            return pachet.getIdOwner();
        }
        return null;
    }

    @Operation(summary = "Delete a ticket",
               description = "Deletes a ticket identified by its unique code. Returns HTTP 204 No Content on success. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ticket successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Ticket not found with the specified code")
    })
    @DeleteMapping("/{cod}")
    public ResponseEntity<Void> deleteBilet(
            @Parameter(description = "Unique ticket code identifier", example = "BILET-a1b2c3d4")
            @PathVariable String cod,
            HttpServletRequest request) {
        // Authorization: any authenticated user can return/delete tickets
        // (serviciu_clienti manages ownership of tickets to clients)
        authHelper.requireCanPurchaseTicket(request);

        biletService.delete(cod);
        return ResponseEntity.noContent().build();
    }


     ///PUT /api/event-manager/tickets/{cod}
     //create/update cu cod explicit
     //exista -> update (200)
     //nu exista -> create (201)

    @Operation(summary = "Create or update ticket with explicit code",
               description = "Creates a new ticket with the specified code or updates an existing one. Returns HTTP 200 if updated, HTTP 201 if created. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket successfully updated"),
            @ApiResponse(responseCode = "201", description = "Ticket successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PutMapping("/{cod}")
    public ResponseEntity<BiletDTO> createOrUpdateBilet(
            @Parameter(description = "Unique ticket code to create or update", example = "BILET-a1b2c3d4")
            @PathVariable String cod,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"pachetId\": 2}"
                            )
                    )
            )
            @RequestBody BiletDTO biletDTO,
            HttpServletRequest request) {


        boolean exists = biletService.existsByCod(cod);

        if (exists) {
            // Authorization: check ownership of existing ticket (only owner can update)
            BiletDTO existing = biletService.findByCod(cod);
            Integer ownerId = getOwnerIdForTicket(existing);
            authHelper.requireOwnership(request, ownerId);
        } else {
            // Authorization: any authenticated user can purchase (create) a new ticket
            authHelper.requireCanPurchaseTicket(request);
        }


        BiletDTO savedBilet = biletService.createOrUpdate(cod, biletDTO);


        hateoasHelper.addLinksToBilet(savedBilet);


        HttpStatus status = exists ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(savedBilet);
    }

}