package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import com.pos.dto.PachetEvenimentCreateDTO;
import com.pos.exception.ResourceNotFoundException;
import com.pos.security.AuthorizationHelper;
import com.pos.service.BiletService;
import com.pos.service.PachetEvenimentService;
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
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pos.service.EvenimentService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/event-manager/event-packets")
@RequiredArgsConstructor
public class PachetController {

    private final PachetService pachetService;
    private final HateoasHelper hateoasHelper;
    private final BiletService biletService;
    private final PachetEvenimentService pachetEvenimentService;
    private final EvenimentService evenimentService;
    private final AuthorizationHelper authHelper;


    // GET /api/event-manager/event-packets
    //
    @Operation(summary = "Retrieve all event packages",
               description = "Returns a collection of all event packages with optional pagination and filtering. Supports query parameters: ?page=..., ?items_per_page=..., ?available_tickets=..., ?type=...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all event packages")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<PachetDTO>> getAllPachete(
            @Parameter(description = "Page number for pagination", example = "1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Number of items per page (default is 2)", example = "3")
            @RequestParam(name = "items_per_page", required = false) Integer itemsPerPage,
            @Parameter(description = "Filter by available tickets count", example = "10")
            @RequestParam(name = "available_tickets", required = false) Integer availableTickets,
            @Parameter(description = "Filter by package type", example = "premium")
            @RequestParam(required = false) String type)

    {
        //valoare default
        if(page != null && itemsPerPage == null)
            itemsPerPage = 2;

        List<PachetDTO> pachete = pachetService.findAll(page, itemsPerPage, availableTickets, type);

        hateoasHelper.addLinksToPachete(pachete);

        CollectionModel<PachetDTO> collectionModel = CollectionModel.of(pachete);

        //  Link self --> exact request-ul cu parametri
        Link selfLink = Link.of(ServletUriComponentsBuilder.fromCurrentRequest().toUriString())
                .withSelfRel()
                .withType("GET");

        collectionModel.add(selfLink);

        /*
        //revin dupa sa vad cum fac pana la urma aici
        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getAllPachete(page, itemsPerPage)) //sau gol/null
                .withSelfRel()
                .withType("GET"));
        */

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .createPachet(null, null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }


     // GET /api/event-manager/event-packets/{id}

    @Operation(summary = "Retrieve event package by ID",
               description = "Returns a single event package identified by its unique ID with HATEOAS links")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the event package"),
            @ApiResponse(responseCode = "404", description = "Event package not found with the specified ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PachetDTO> getPachetById(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id) {
        PachetDTO pachet = pachetService.findById(id);
        hateoasHelper.addLinksToPachet(pachet);
        return ResponseEntity.ok(pachet);
    }


     //POST /api/event-manager/event-packets

    @Operation(summary = "Create a new event package",
               description = "Creates a new event package with the provided details. Returns the created package with HTTP 201 status. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event package successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PostMapping
    public ResponseEntity<PachetDTO> createPachet(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"idOwner\": 1, \"nume\": \"Pachet Festival Vara\", \"locatie\": \"Parc Central\", \"descriere\": \"Toate evenimentele de vara\", \"numarLocuri\": 500}"
                            )
                    )
            )
            @RequestBody PachetDTO pachetDTO,
            HttpServletRequest request) {
        // Authorization: only admin or owner-event can create
        authHelper.requireOwnershipForCreate(request, pachetDTO.getIdOwner());
        pachetDTO.setIdOwner(authHelper.getEffectiveOwnerId(request, pachetDTO.getIdOwner()));

        PachetDTO createdPachet = pachetService.create(pachetDTO);
        hateoasHelper.addLinksToPachet(createdPachet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPachet);
    }


    //  DELETE /api/event-manager/event-packets/{id}
    @Operation(summary = "Delete an event package",
               description = "Deletes the event package identified by {id} from the URL. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Event package successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Event package not found with the specified ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            HttpServletRequest request) {
        // Authorization: check ownership before deleting
        PachetDTO existing = pachetService.findById(id);
        authHelper.requireOwnership(request, existing.getIdOwner());

        pachetService.delete(id);
        return ResponseEntity.noContent().build();
    }


     //PUT /api/event-manager/event-packets/{id}

    @Operation(summary = "Update or create event package with explicit ID",
               description = "Updates an existing event package or creates a new one with the specified ID. Returns HTTP 200 if updated, HTTP 201 if created. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event package successfully updated"),
            @ApiResponse(responseCode = "201", description = "Event package successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PachetDTO> updatePachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"idOwner\": 1, \"nume\": \"Pachet Actualizat\", \"locatie\": \"Locatie Noua\", \"descriere\": \"Descriere actualizata\", \"numarLocuri\": 400}"
                            )
                    )
            )
            @RequestBody PachetDTO pachetDTO,
            HttpServletRequest request) {

        boolean exists = pachetService.existsById(id);

        if (exists) {
            // Authorization: check ownership for update
            PachetDTO existing = pachetService.findById(id);
            authHelper.requireOwnership(request, existing.getIdOwner());
        } else {
            // Authorization: check create permission for new resource
            authHelper.requireOwnershipForCreate(request, pachetDTO.getIdOwner());
            pachetDTO.setIdOwner(authHelper.getEffectiveOwnerId(request, pachetDTO.getIdOwner()));
        }

        PachetDTO updatedPachet = pachetService.update(id, pachetDTO);
        hateoasHelper.addLinksToPachet(updatedPachet);

        return exists
                ? ResponseEntity.ok(updatedPachet)
                : ResponseEntity.status(HttpStatus.CREATED).body(updatedPachet);
    }

    /**
     * GET /api/event-manager/event-packets/{id}/tickets
     * Returneaza toate biletele pentru un pachet
     */
    @Operation(summary = "Retrieve all tickets for an event package",
               description = "Returns a collection of all tickets associated with the specified event package ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all tickets for the event package"),
            @ApiResponse(responseCode = "404", description = "Event package not found with the specified ID")
    })
    @GetMapping("/{id}/tickets")
    public ResponseEntity<CollectionModel<BiletDTO>> getBileteForPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id) {

        pachetService.findById(id);

        List<BiletDTO> bilete = biletService.findByPachetId(id);
        hateoasHelper.addLinksToBilete(bilete);

        CollectionModel<BiletDTO> collectionModel = CollectionModel.of(bilete);

        // Link self pentru colectia de bilete
        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getBileteForPachet(id))
                .withSelfRel()
                .withType("GET"));

        // Link catre pachet
        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getPachetById(id))
                .withRel("packet")
                .withType("GET"));

        //link post
        collectionModel.add(linkTo(methodOn(PachetController.class)
                .createBiletForPachet(id, null, null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * GET /api/event-manager/event-packets/{id}/tickets/{ticketCod}
     * Returneaza un bilet specific pentru un pachet
     */
    @Operation(summary = "Retrieve specific ticket for an event package",
               description = "Returns a single ticket identified by its code that belongs to the specified event package ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the ticket for the event package"),
            @ApiResponse(responseCode = "404", description = "Event package not found or ticket not found or ticket does not belong to this package")
    })
    @GetMapping("/{id}/tickets/{ticketCod}")
    public ResponseEntity<BiletDTO> getBiletForPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Ticket code", example = "BILET-a1b2c3d4")
            @PathVariable String ticketCod) {


        pachetService.findById(id);


        BiletDTO bilet = biletService.findByCod(ticketCod);

        // Verifica ca biletul apartine acestui pachet
        if (bilet.getPachetId() == null || !bilet.getPachetId().equals(id)) {
            throw new ResourceNotFoundException("Bilet", "cod", ticketCod + " for packet " + id);
        }

        hateoasHelper.addLinksToBilet(bilet);
        return ResponseEntity.ok(bilet);
    }

    /**
     * POST /api/event-manager/event-packets/{id}/tickets
     * Creeaza un bilet pentru acest pachet
     * body poate fi gol {} sau cu altele pe viitor, vad
     */
    @Operation(summary = "Create a ticket for an event package",
               description = "Creates a new ticket associated with the specified event package ID. Request body is optional and can be empty. Returns HTTP 201 status. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket successfully created for the event package"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Event package not found with the specified ID"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PostMapping("/{id}/tickets")
    public ResponseEntity<BiletDTO> createBiletForPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{}")
                    )
            )
            @RequestBody(required = false) BiletDTO biletDTO,
            HttpServletRequest request) {

        // Authorization: only admin or the package owner can create tickets
        PachetDTO pachet = pachetService.findById(id);
        authHelper.requireOwnership(request, pachet.getIdOwner());


        if (biletDTO == null) {
            biletDTO = new BiletDTO();
        }
        biletDTO.setPachetId(id);
        biletDTO.setEvenimentId(null);

        BiletDTO createdBilet = biletService.create(biletDTO);
        hateoasHelper.addLinksToBilet(createdBilet);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdBilet);
    }
    /**
    GET /api/event-manager/event-packets/{id}/events
     returneaza evenimentele incluse in pachet
     */
    @Operation(summary = "Retrieve all events in an event package",
               description = "Returns a collection of all events included in the specified event package ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all events in the event package")
    })
    @GetMapping("/{id}/events")
    public ResponseEntity<CollectionModel<EvenimentDTO>> getEvenimenteForPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id) {
        List<EvenimentDTO> evenimente = pachetEvenimentService.findEvenimenteForPachet(id);
        hateoasHelper.addLinksToEvenimente(evenimente);

        CollectionModel<EvenimentDTO> collectionModel = CollectionModel.of(evenimente);

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getEvenimenteForPachet(id))
                .withSelfRel()
                .withType("GET"));

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getPachetById(id))
                .withRel("packet")
                .withType("GET"));

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .createEvenimentInPachet(id, null, null))
                .withRel("create-event")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * POST /api/event-manager/event-packets/{id}/events
     * Creeaza un eveniment NOU si il adauga in acest pachet
     * Body: EvenimentDTO complet
     */
    @Operation(summary = "Create a new event and add to package",
               description = "Creates a new event with the provided details and automatically associates it with the specified event package ID. Returns HTTP 201 status. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event successfully created and added to the package"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Event package not found with the specified ID"),
            @ApiResponse(responseCode = "409", description = "Business logic conflict"),
            @ApiResponse(responseCode = "415", description = "Unsupported media type - use application/json"),
            @ApiResponse(responseCode = "422", description = "Invalid JSON or incompatible data types")
    })
    @PostMapping("/{id}/events")
    public ResponseEntity<EvenimentDTO> createEvenimentInPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{\"idOwner\": 1, \"nume\": \"Eveniment Nou\", \"locatie\": \"Sala Concert\", \"descriere\": \"Un eveniment nou\", \"numarLocuri\": 200}"
                            )
                    )
            )
            @RequestBody EvenimentDTO evenimentDTO,
            HttpServletRequest request) {

        // Authorization: only admin or the package owner can create events in packet
        PachetDTO pachet = pachetService.findById(id);
        authHelper.requireOwnership(request, pachet.getIdOwner());

        // Set owner ID to the same as the package owner
        evenimentDTO.setIdOwner(pachet.getIdOwner());

        // 1. creez evenimentul
        EvenimentDTO createdEveniment = evenimentService.create(evenimentDTO);

        // 2. adaug asocierea ev<->pachet

        pachetEvenimentService.addEvenimentToPachet(
                id,
                createdEveniment.getId()

        );

        // 3. hateoas
        hateoasHelper.addLinksToEveniment(createdEveniment);

        createdEveniment.add(linkTo(methodOn(PachetController.class)
                .getPachetById(id))
                .withRel("parent-packet")
                .withType("GET"));

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEveniment);
    }


    /**
     * DELETE /api/event-manager/event-packets/{id}/events/{evenimentId}
     * sterge evenimentul specificat din acest pachet
     */

    @Operation(summary = "Remove association between event and package",
               description = "Removes the event with {evenimentId} from the URL from the package with {id} from the URL. Requires authentication.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Association successfully removed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions")
    })
    @DeleteMapping("/{id}/events/{evenimentId}")
    public ResponseEntity<Void> removeEvenimentFromPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Event ID", example = "2")
            @PathVariable Integer evenimentId,
            HttpServletRequest request) {

        // Authorization: check ownership of the package
        PachetDTO pachet = pachetService.findById(id);
        authHelper.requireOwnership(request, pachet.getIdOwner());

        pachetEvenimentService.removeEvenimentFromPachet(id, evenimentId);
        return ResponseEntity.noContent().build();
    }
}