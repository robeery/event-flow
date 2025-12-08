package com.pos.controller;

import com.pos.dto.BiletDTO;
import com.pos.dto.PachetDTO;
import com.pos.exception.ResourceNotFoundException;
import com.pos.service.BiletService;
import com.pos.dto.EvenimentDTO;
import com.pos.service.EvenimentService;
import com.pos.service.PachetEvenimentService;
import com.pos.util.HateoasHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pos.dto.PachetEvenimentCreateDTO;

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


    /**
     * GET /api/event-manager/events/{id}/tickets
     * Returneaza toate biletele pentru un eveniment
     */
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


}