package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.service.BiletService;
import com.pos.util.HateoasHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Retrieve all tickets",
               description = "Returns a collection of all tickets in the system with HATEOAS links for navigation and ticket creation")
    @GetMapping
    public ResponseEntity<CollectionModel<BiletDTO>> getAllBilete() {
        List<BiletDTO> bilete = biletService.findAll();
        hateoasHelper.addLinksToBilete(bilete);

        CollectionModel<BiletDTO> collectionModel = CollectionModel.of(bilete);
        collectionModel.add(linkTo(methodOn(BiletController.class).getAllBilete()).withSelfRel().withType("GET"));
        collectionModel.add(linkTo(methodOn(BiletController.class).createBilet(null)).withRel("create").withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Retrieve ticket by code",
               description = "Returns a single ticket identified by its unique code with HATEOAS links")
    @GetMapping("/{cod}")
    public ResponseEntity<BiletDTO> getBiletByCod(
            @Parameter(description = "Unique ticket code identifier", example = "BILET-a1b2c3d4")
            @PathVariable String cod) {
        BiletDTO bilet = biletService.findByCod(cod);
        hateoasHelper.addLinksToBilet(bilet);
        return ResponseEntity.ok(bilet);
    }

    @Operation(summary = "Create a new ticket",
               description = "Creates a new ticket with an auto-generated unique code. Returns the created ticket with HTTP 201 status")
    @PostMapping
    public ResponseEntity<BiletDTO> createBilet(@RequestBody BiletDTO biletDTO) {
        BiletDTO createdBilet = biletService.create(biletDTO);
        hateoasHelper.addLinksToBilet(createdBilet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBilet);
    }

    @Operation(summary = "Delete a ticket",
               description = "Deletes a ticket identified by its unique code. Returns HTTP 204 No Content on success")
    @DeleteMapping("/{cod}")
    public ResponseEntity<Void> deleteBilet(
            @Parameter(description = "Unique ticket code identifier", example = "BILET-a1b2c3d4")
            @PathVariable String cod) {
        biletService.delete(cod);
        return ResponseEntity.noContent().build();
    }


     ///PUT /api/event-manager/tickets/{cod}
     //create/update cu cod explicit
     //exista -> update (200)
     //nu exista -> create (201)

    @Operation(summary = "Create or update ticket with explicit code",
               description = "Creates a new ticket with the specified code or updates an existing one. Returns HTTP 200 if updated, HTTP 201 if created")
    @PutMapping("/{cod}")
    public ResponseEntity<BiletDTO> createOrUpdateBilet(
            @Parameter(description = "Unique ticket code to create or update", example = "BILET-a1b2c3d4")
            @PathVariable String cod,
            @RequestBody BiletDTO biletDTO) {


        boolean exists = biletService.existsByCod(cod);


        BiletDTO savedBilet = biletService.createOrUpdate(cod, biletDTO);


        hateoasHelper.addLinksToBilet(savedBilet);


        HttpStatus status = exists ? HttpStatus.OK : HttpStatus.CREATED;

        return ResponseEntity.status(status).body(savedBilet);
    }

    //ma mai gandesc daca vreau sau nu
    //PUT - TO DO
}