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
    @GetMapping
    public ResponseEntity<CollectionModel<PachetDTO>> getAllPachete(
            @RequestParam(required = false) Integer page,
            @RequestParam(name = "items_per_page", required = false) Integer itemsPerPage,
            @RequestParam(name = "available_tickets", required = false) Integer availableTickets)

    {
        //valoare default
        if(page != null && itemsPerPage == null)
            itemsPerPage = 2;

        List<PachetDTO> pachete = pachetService.findAll(page, itemsPerPage, availableTickets);

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

    /**
     * GET /api/event-manager/event-packets/{id}/tickets
     * Returneaza toate biletele pentru un pachet
     */
    @GetMapping("/{id}/tickets")
    public ResponseEntity<CollectionModel<BiletDTO>> getBileteForPachet(@PathVariable Integer id) {

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
    @GetMapping("/{id}/tickets/{ticketCod}")
    public ResponseEntity<BiletDTO> getBiletForPachet(
            @PathVariable Integer id,
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
    @PostMapping("/{id}/tickets")
    public ResponseEntity<BiletDTO> createBiletForPachet(
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
    @GetMapping("/{id}/events")
    public ResponseEntity<CollectionModel<EvenimentDTO>> getEvenimenteForPachet(@PathVariable Integer id) {
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
    @PostMapping("/{id}/events")
    public ResponseEntity<EvenimentDTO> createEvenimentInPachet(
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
    @DeleteMapping("/{id}/events/{evenimentId}")
    public ResponseEntity<Void> removeEvenimentFromPachet(
            @PathVariable Integer id,
            @PathVariable Integer evenimentId) {

        pachetEvenimentService.removeEvenimentFromPachet(id, evenimentId);
        return ResponseEntity.noContent().build();
    }
}