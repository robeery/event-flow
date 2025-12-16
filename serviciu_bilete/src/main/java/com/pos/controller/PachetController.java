package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import com.pos.dto.PachetEvenimentCreateDTO;
import com.pos.exception.ResourceNotFoundException;
import com.pos.service.BiletService;
import com.pos.service.PachetEvenimentService;
import com.pos.service.PachetService;
import com.pos.util.HateoasHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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


    // GET /api/event-manager/event-packets
    //
    @Operation(summary = "Retrieve all event packages",
               description = "Returns a collection of all event packages with optional pagination and filtering. Supports query parameters: ?page=..., ?items_per_page=..., ?available_tickets=..., ?type=...")
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
                .createPachet(null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }


     // GET /api/event-manager/event-packets/{id}

    @Operation(summary = "Retrieve event package by ID",
               description = "Returns a single event package identified by its unique ID with HATEOAS links")
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
               description = "Creates a new event package with the provided details. Returns the created package with HTTP 201 status")
    @PostMapping
    public ResponseEntity<PachetDTO> createPachet(@RequestBody PachetDTO pachetDTO) {
        PachetDTO createdPachet = pachetService.create(pachetDTO);
        hateoasHelper.addLinksToPachet(createdPachet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPachet);
    }


    //  DELETE /api/event-manager/event-packets/{id}
    @Operation(summary = "Delete an event package",
               description = "Deletes the event package identified by {id} from the URL")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id) {
        pachetService.delete(id);
        return ResponseEntity.noContent().build();
    }


     //PUT /api/event-manager/event-packets/{id}

    @Operation(summary = "Update or create event package with explicit ID",
               description = "Updates an existing event package or creates a new one with the specified ID. Returns HTTP 200 if updated, HTTP 201 if created")
    @PutMapping("/{id}")
    public ResponseEntity<PachetDTO> updatePachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @RequestBody PachetDTO pachetDTO) {

        boolean exists = pachetService.existsById(id);
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
                .createBiletForPachet(id, null))
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
               description = "Creates a new ticket associated with the specified event package ID. Request body is optional and can be empty. Returns HTTP 201 status")
    @PostMapping("/{id}/tickets")
    public ResponseEntity<BiletDTO> createBiletForPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @RequestBody(required = false) BiletDTO biletDTO) {


        pachetService.findById(id);


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
                .createEvenimentInPachet(id, null))
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
               description = "Creates a new event with the provided details and automatically associates it with the specified event package ID. Returns HTTP 201 status")
    @PostMapping("/{id}/events")
    public ResponseEntity<EvenimentDTO> createEvenimentInPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @RequestBody EvenimentDTO evenimentDTO) {


        pachetService.findById(id);

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
               description = "Removes the event with {evenimentId} from the URL from the package with {id} from the URL")

    @DeleteMapping("/{id}/events/{evenimentId}")
    public ResponseEntity<Void> removeEvenimentFromPachet(
            @Parameter(description = "Package ID", example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Event ID", example = "2")
            @PathVariable Integer evenimentId) {

        pachetEvenimentService.removeEvenimentFromPachet(id, evenimentId);
        return ResponseEntity.noContent().build();
    }
}