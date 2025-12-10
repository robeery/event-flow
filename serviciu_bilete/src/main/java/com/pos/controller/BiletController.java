package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.service.BiletService;
import com.pos.util.HateoasHelper;
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

    @GetMapping
    public ResponseEntity<CollectionModel<BiletDTO>> getAllBilete() {
        List<BiletDTO> bilete = biletService.findAll();
        hateoasHelper.addLinksToBilete(bilete);

        CollectionModel<BiletDTO> collectionModel = CollectionModel.of(bilete);
        collectionModel.add(linkTo(methodOn(BiletController.class).getAllBilete()).withSelfRel().withType("GET"));
        collectionModel.add(linkTo(methodOn(BiletController.class).createBilet(null)).withRel("create").withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{cod}")
    public ResponseEntity<BiletDTO> getBiletByCod(@PathVariable String cod) {
        BiletDTO bilet = biletService.findByCod(cod);
        hateoasHelper.addLinksToBilet(bilet);
        return ResponseEntity.ok(bilet);
    }

    @PostMapping
    public ResponseEntity<BiletDTO> createBilet(@RequestBody BiletDTO biletDTO) {
        BiletDTO createdBilet = biletService.create(biletDTO);
        hateoasHelper.addLinksToBilet(createdBilet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBilet);
    }

    @DeleteMapping("/{cod}")
    public ResponseEntity<Void> deleteBilet(@PathVariable String cod) {
        biletService.delete(cod);
        return ResponseEntity.noContent().build();
    }


     ///PUT /api/event-manager/tickets/{cod}
     //create/update cu cod explicit
     //exista -> update (200)
     //nu exista -> create (201)

    @PutMapping("/{cod}")
    public ResponseEntity<BiletDTO> createOrUpdateBilet(
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