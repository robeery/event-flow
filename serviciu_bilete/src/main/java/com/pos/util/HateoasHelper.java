package com.pos.util;

import com.pos.controller.BiletController;
import com.pos.controller.EvenimentController;
import com.pos.controller.PachetController;
import com.pos.dto.BiletDTO;
import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class HateoasHelper {


     // Adauga link-uri HATEOAS pentru un singur eveniment folosing webmvclinkbuilder din spring-hateoaas pentru generare automata

    //EVENIMENTE

    public void addLinksToEveniment(EvenimentDTO eveniment) {
        // Link self - catre resursa curenta
        eveniment.add(linkTo(methodOn(EvenimentController.class)
                .getEvenimentById(eveniment.getId()))
                .withSelfRel()
                .withType("GET"));

        // Link all-events - catre toate evenimentele
        eveniment.add(linkTo(methodOn(EvenimentController.class)
                .getAllEvenimente())
                .withRel("all-events")
                .withType("GET"));

        // Link update - actualizeaza evenimentul
        eveniment.add(linkTo(methodOn(EvenimentController.class)
                .updateEveniment(eveniment.getId(), null))
                .withRel("update")
                .withType("PUT"));

        // Link delete - sterge evenimentul
        eveniment.add(linkTo(methodOn(EvenimentController.class)
                .deleteEveniment(eveniment.getId()))
                .withRel("delete")
                .withType("DELETE"));
    }


     // Adauga link-uri HATEOAS pentru  lista de evenimente

    public void addLinksToEvenimente(List<EvenimentDTO> evenimente) {
        evenimente.forEach(this::addLinksToEveniment);
    }

    //PACHETE
    public void addLinksToPachet(PachetDTO pachet) {
        pachet.add(linkTo(methodOn(PachetController.class)
                .getPachetById(pachet.getId()))
                .withSelfRel()
                .withType("GET"));

        pachet.add(linkTo(methodOn(PachetController.class)
                .getAllPachete())
                .withRel("all-packets")
                .withType("GET"));

        pachet.add(linkTo(methodOn(PachetController.class)
                .updatePachet(pachet.getId(), null))
                .withRel("update")
                .withType("PUT"));

        pachet.add(linkTo(methodOn(PachetController.class)
                .deletePachet(pachet.getId()))
                .withRel("delete")
                .withType("DELETE"));
    }

    public void addLinksToPachete(List<PachetDTO> pachete) {
        pachete.forEach(this::addLinksToPachet);
    }

    ///BILETE
    public void addLinksToBilet(BiletDTO bilet) {
        bilet.add(linkTo(methodOn(BiletController.class)
                .getBiletByCod(bilet.getCod()))
                .withSelfRel()
                .withType("GET"));

        bilet.add(linkTo(methodOn(BiletController.class)
                .getAllBilete())
                .withRel("all-tickets")
                .withType("GET"));

        bilet.add(linkTo(methodOn(BiletController.class)
                .deleteBilet(bilet.getCod()))
                .withRel("delete")
                .withType("DELETE"));

        if (bilet.getEvenimentId() != null) {
            bilet.add(linkTo(methodOn(EvenimentController.class)
                    .getEvenimentById(bilet.getEvenimentId()))
                    .withRel("event")
                    .withType("GET"));
        }

        if (bilet.getPachetId() != null) {
            bilet.add(linkTo(methodOn(PachetController.class)
                    .getPachetById(bilet.getPachetId()))
                    .withRel("packet")
                    .withType("GET"));
        }
    }

    public void addLinksToBilete(List<BiletDTO> bilete) {
        bilete.forEach(this::addLinksToBilet);
    }
}