package com.pos.controller;

import com.pos.dto.EvenimentDTO;
import com.pos.service.EvenimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-manager/events")
@RequiredArgsConstructor
public class EvenimentController {

    private final EvenimentService evenimentService;


     // GET /api/event-manager/events
     //  returneaza toate evenimentele

    @GetMapping
    public ResponseEntity<List<EvenimentDTO>> getAllEvenimente() {
        List<EvenimentDTO> evenimente = evenimentService.findAll();
        return ResponseEntity.ok(evenimente);
    }


     // GET /api/event-manager/events/{id}
     // returneaza un eveniment specific

    @GetMapping("/{id}")
    public ResponseEntity<EvenimentDTO> getEvenimentById(@PathVariable Integer id) {
        EvenimentDTO eveniment = evenimentService.findById(id);
        return ResponseEntity.ok(eveniment);
    }


     //POST /api/event-manager/events
     //creaza un eveniment nou

    @PostMapping
    public ResponseEntity<EvenimentDTO> createEveniment(@RequestBody EvenimentDTO evenimentDTO) {
        EvenimentDTO createdEveniment = evenimentService.create(evenimentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEveniment);
    }

    //DELETE /api/event-manager/events/{id}
    //sterge un eveniment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEveniment(@PathVariable Integer id) {
        evenimentService.delete(id);

        // 204 No Content fara body in raspuns
        return ResponseEntity.noContent().build();
    }
}