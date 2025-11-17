package com.pos.controller;

import com.pos.dto.EvenimentDTO;
import com.pos.service.EvenimentService;
import com.pos.util.HateoasHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/event-manager/events")
@RequiredArgsConstructor
public class EvenimentController {

    private final EvenimentService evenimentService;
    private final HateoasHelper hateoasHelper;



     // GET /api/event-manager/events
     //  returneaza toate evenimentele

    @GetMapping
    public ResponseEntity<CollectionModel<EvenimentDTO>> getAllEvenimente() {
        List<EvenimentDTO> evenimente = evenimentService.findAll();

        // adauga linkuri la fiecare eveniment
        hateoasHelper.addLinksToEvenimente(evenimente);

        // face CollectionModel cu link-uri la nivel de colectie
        CollectionModel<EvenimentDTO> collectionModel = CollectionModel.of(evenimente);

        // Link self pentru colecție
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .getAllEvenimente())
                .withSelfRel()
                .withType("GET"));

        // Link create pentru a crea un eveniment nou
        collectionModel.add(linkTo(methodOn(EvenimentController.class)
                .createEveniment(null))
                .withRel("create")
                .withType("POST"));

        return ResponseEntity.ok(collectionModel);
    }


     // GET /api/event-manager/events/{id}
     // returneaza un eveniment specific

    @GetMapping("/{id}")
    public ResponseEntity<EvenimentDTO> getEvenimentById(@PathVariable Integer id) {
        EvenimentDTO eveniment = evenimentService.findById(id);
        hateoasHelper.addLinksToEveniment(eveniment);
        return ResponseEntity.ok(eveniment);
    }


     //POST /api/event-manager/events
     //creaza un eveniment nou

    @PostMapping
    public ResponseEntity<EvenimentDTO> createEveniment(@RequestBody EvenimentDTO evenimentDTO) {
        EvenimentDTO createdEveniment = evenimentService.create(evenimentDTO);


        hateoasHelper.addLinksToEveniment(createdEveniment);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdEveniment);
    }

    //DELETE /api/event-manager/events/{id}
    //sterge un eveniment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEveniment(@PathVariable Integer id) {
        evenimentService.delete(id);
        return ResponseEntity.noContent().build();
    }


    //PUT /api/event-manager/events/{id}

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
}