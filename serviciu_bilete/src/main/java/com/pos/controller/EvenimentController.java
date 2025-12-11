package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.dto.PachetDTO;
import com.pos.exception.ResourceNotFoundException;
import com.pos.service.BiletService;
import com.pos.dto.EvenimentDTO;
import com.pos.service.EvenimentService;
import com.pos.service.PachetEvenimentService;
import com.pos.util.HateoasHelper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pos.dto.PachetEvenimentCreateDTO;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager/events")
@RequiredArgsConstructor
public class EvenimentController {

    private final EvenimentService evenimentService;
    private final HateoasHelper hateoasHelper;
    private final BiletService biletService;
    private final PachetEvenimentService pachetEvenimentService;



     // GET /api/event-manager/events
     // returneaza toate evenimentele
    //optional: ?location=... sau ?name=...

    @Operation(summary = "Retrieve all events",
               description = "Returns a collection of all events with optional filtering by location or name. Supports query parameters: ?location=... or ?name=...")
    @GetMapping
    public ResponseEntity<CollectionModel<EvenimentDTO>> getAllEvenimente(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String name)
    {
        List<EvenimentDTO> evenimente = evenimentService.findAll(location, name);

        // adauga linkuri la fiecare eveniment
        hateoasHelper.addLinksToEvenimente(evenimente);

        // face CollectionModel cu link-uri la nivel de colectie
        CollectionModel<EvenimentDTO> collectionModel = CollectionModel.of(evenimente);


        // Link self pentru colectie
        if (location == null && name == null) {
            // Link simplu fara parametri
            collectionModel.add(linkTo(methodOn(EvenimentController.class)
                    .getAllEvenimente(null, null))
                    .withSelfRel()
                    .withType("GET"));
        } else {
            // Link cu parametrii actuali
            collectionModel.add(linkTo(methodOn(EvenimentController.class)
                    .getAllEvenimente(location, name))
                    .withSelfRel()
                    .withType("GET"));
        }



        //  face singur direct
        //to do and mess around with this later
        /*
        Link selfLink = Link.of(ServletUriComponentsBuilder.fromCurrentRequest().toUriString())
                .withSelfRel()
                .withType("GET");

        collectionModel.add(selfLink);
        */


        // Link create pentru a crea un eveniment nou
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .createEveniment(null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }


     // GET /api/event-manager/events/{id}
     // returneaza un eveniment specific

    @Operation(summary = "Retrieve event by ID",
               description = "Returns a single event identified by its unique ID with HATEOAS links")
    @GetMapping("/{id}")
    public ResponseEntity<EvenimentDTO> getEvenimentById(@PathVariable Integer id) {
        EvenimentDTO eveniment = evenimentService.findById(id);
        hateoasHelper.addLinksToEveniment(eveniment);
        return ResponseEntity.ok(eveniment);
    }


     //POST /api/event-manager/events
     //creaza un eveniment nou

    @Operation(summary = "Create a new event",
               description = "Creates a new event with the provided details. Returns the created event with HTTP 201 status")
    @PostMapping
    public ResponseEntity<EvenimentDTO> createEveniment(@RequestBody EvenimentDTO evenimentDTO) {
        EvenimentDTO createdEveniment = evenimentService.create(evenimentDTO);


        hateoasHelper.addLinksToEveniment(createdEveniment);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEveniment);
    }

    //DELETE /api/event-manager/events/{id}
    //sterge un eveniment
    @Operation(summary = "Delete an event",
               description = "Deletes an event identified by its ID. Returns HTTP 204 No Content on success")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEveniment(@PathVariable Integer id) {
        evenimentService.delete(id);
        return ResponseEntity.noContent().build();
    }


    //PUT /api/event-manager/events/{id}

    @Operation(summary = "Update or create event with explicit ID",
               description = "Updates an existing event or creates a new one with the specified ID. Returns HTTP 200 if updated, HTTP 201 if created")
    @PutMapping("/{id}")
    public ResponseEntity<EvenimentDTO> updateEveniment(
            @PathVariable Integer id,
            @RequestBody EvenimentDTO evenimentDTO) {

        boolean exists = evenimentService.existsById(id);
        EvenimentDTO updatedEveniment = evenimentService.update(id, evenimentDTO);


        hateoasHelper.addLinksToEveniment(updatedEveniment);

        return exists
                ? ResponseEntity.ok(updatedEveniment)
                : ResponseEntity.status(HttpStatus.CREATED).body(updatedEveniment);
    }


    /**
     * GET /api/event-manager/events/{id}/tickets
     * Returneaza toate biletele pentru un eveniment
     */
    @Operation(summary = "Retrieve all tickets for an event",
               description = "Returns a collection of all tickets associated with the specified event ID")
    @GetMapping("/{id}/tickets")
    public ResponseEntity<CollectionModel<BiletDTO>> getBileteForEveniment(@PathVariable Integer id) {
        // Verifica dacă evenimentul exista
        evenimentService.findById(id);

        List<BiletDTO> bilete = biletService.findByEvenimentId(id);
        hateoasHelper.addLinksToBilete(bilete);

        CollectionModel<BiletDTO> collectionModel = CollectionModel.of(bilete);

        // Link self pentru colectia de bilete
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .getBileteForEveniment(id))
                .withSelfRel()
                .withType("GET"));

        // Link catre eveniment
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .getEvenimentById(id))
                .withRel("event")
                .withType("GET"));

        //link post
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .createBiletForEveniment(id, null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * GET /api/event-manager/events/{id}/tickets/{ticketCod}
     * returneaza un bilet specific pentru un eveniment
     */
    @Operation(summary = "Retrieve specific ticket for an event",
               description = "Returns a single ticket identified by its code that belongs to the specified event ID")
    @GetMapping("/{id}/tickets/{ticketCod}")
    public ResponseEntity<BiletDTO> getBiletForEveniment(
            @PathVariable Integer id,
            @PathVariable String ticketCod) {


        evenimentService.findById(id);


        BiletDTO bilet = biletService.findByCod(ticketCod);

        // Verifica ca biletul aparține acestui eveniment
        if (bilet.getEvenimentId() == null || !bilet.getEvenimentId().equals(id)) {
            throw new ResourceNotFoundException("Bilet", "cod", ticketCod + " for event " + id);
        }

        hateoasHelper.addLinksToBilet(bilet);
        return ResponseEntity.ok(bilet);
    }

    /**
     * POST /api/event-manager/events/{id}/tickets
     * creeaza un bilet pentru acest eveniment
     * body poate fi gol {} sau cu altele pe viitor, vad
     */
    @Operation(summary = "Create a ticket for an event",
               description = "Creates a new ticket associated with the specified event ID. Request body is optional and can be empty. Returns HTTP 201 status")
    @PostMapping("/{id}/tickets")
    public ResponseEntity<BiletDTO> createBiletForEveniment(
            @PathVariable Integer id,
            @RequestBody(required = false) BiletDTO biletDTO) {


        evenimentService.findById(id);


        if (biletDTO == null) {
            biletDTO = new BiletDTO();
        }
        biletDTO.setEvenimentId(id);
        biletDTO.setPachetId(null);

        BiletDTO createdBilet = biletService.create(biletDTO);
        hateoasHelper.addLinksToBilet(createdBilet);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdBilet);
    }

    /**
     * GET /api/event-manager/events/{id}/event-packets
     * returneaza pachetele care contin acest eveniment
     */
    @Operation(summary = "Retrieve event packages containing an event",
               description = "Returns a collection of all event packages that contain the specified event ID")
    @GetMapping("/{id}/event-packets")
    public ResponseEntity<CollectionModel<PachetDTO>> getPacheteForEveniment(@PathVariable Integer id) {
        List<PachetDTO> pachete = pachetEvenimentService.findPacheteForEveniment(id);
        hateoasHelper.addLinksToPachete(pachete);

        CollectionModel<PachetDTO> collectionModel = CollectionModel.of(pachete);

        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .getPacheteForEveniment(id))
                .withSelfRel()
                .withType("GET"));

        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .getEvenimentById(id))
                .withRel("event")
                .withType("GET"));

        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .addEvenimentToPachet(id, null))
                .withRel("add-to-packet")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * POST /api/event-manager/events/{id}/event-packets
     * Asociaza acest eveniment existent intr-un pachet
     * Body: { "pachetId": 2 }
     */

    @Operation(summary = "Assign event to a package",
               description = "Assigns the event with {id} from the URL with the package with {pachetId} from the request body")

    @PostMapping("/{id}/event-packets")
    public ResponseEntity<PachetEvenimentCreateDTO> addEvenimentToPachet(
            @PathVariable Integer id,
            @RequestBody PachetEvenimentCreateDTO dto) {

        // Validare: pachetId e obligatoriu
        if (dto.getPachetId() == null) {
            throw new IllegalArgumentException("pachetId este obligatoriu");
        }

        PachetEvenimentCreateDTO result = pachetEvenimentService.addPachetToEveniment(id, dto.getPachetId());

        //hateoas
        hateoasHelper.addLinksToPachetEveniment(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * DELETE /api/event-manager/events/{id}/event-packets/{pachetId}
     * sterge acest eveniment din pachetul specificat
     */

    @Operation(summary = "Remove assignment between event and package",
               description = "Removes the event with {id} from the URL from the package with {pachetId} from the URL")

    @DeleteMapping("/{id}/event-packets/{pachetId}")
    public ResponseEntity<Void> removeEvenimentFromPachet(
            @PathVariable Integer id,
            @PathVariable Integer pachetId) {

        pachetEvenimentService.removePachetFromEveniment(id, pachetId);
        return ResponseEntity.noContent().build();
    }


}