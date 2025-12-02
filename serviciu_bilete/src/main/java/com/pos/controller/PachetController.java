package com.pos.controller;

import com.pos.dto.PachetDTO;
import com.pos.service.PachetService;
import com.pos.util.HateoasHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/event-manager/event-packets")
@RequiredArgsConstructor
public class PachetController {

    private final PachetService pachetService;
    private final HateoasHelper hateoasHelper;


    // GET /api/event-manager/event-packets

    @GetMapping
    public ResponseEntity<CollectionModel<PachetDTO>> getAllPachete() {
        List<PachetDTO> pachete = pachetService.findAll();

        hateoasHelper.addLinksToPachete(pachete);

        CollectionModel<PachetDTO> collectionModel = CollectionModel.of(pachete);

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .getAllPachete())
                .withSelfRel()
                .withType("GET"));

        collectionModel.add(linkTo(methodOn(PachetController.class)
                .createPachet(null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }


     // GET /api/event-manager/event-packets/{id}

    @GetMapping("/{id}")
    public ResponseEntity<PachetDTO> getPachetById(@PathVariable Integer id) {
        PachetDTO pachet = pachetService.findById(id);
        hateoasHelper.addLinksToPachet(pachet);
        return ResponseEntity.ok(pachet);
    }


     //POST /api/event-manager/event-packets

    @PostMapping
    public ResponseEntity<PachetDTO> createPachet(@RequestBody PachetDTO pachetDTO) {
        PachetDTO createdPachet = pachetService.create(pachetDTO);
        hateoasHelper.addLinksToPachet(createdPachet);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPachet);
    }


    //  DELETE /api/event-manager/event-packets/{id}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePachet(@PathVariable Integer id) {
        pachetService.delete(id);
        return ResponseEntity.noContent().build();
    }


     //PUT /api/event-manager/event-packets/{id}

    @PutMapping("/{id}")
    public ResponseEntity<PachetDTO> updatePachet(
            @PathVariable Integer id,
            @RequestBody PachetDTO pachetDTO) {

        boolean exists = pachetService.existsById(id);
        PachetDTO updatedPachet = pachetService.update(id, pachetDTO);
        hateoasHelper.addLinksToPachet(updatedPachet);

        return exists
                ? ResponseEntity.ok(updatedPachet)
                : ResponseEntity.status(HttpStatus.CREATED).body(updatedPachet);
    }
}