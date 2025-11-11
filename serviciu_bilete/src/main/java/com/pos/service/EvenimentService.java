package com.pos.service;

import com.pos.dto.EvenimentDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.models.Eveniment;
import com.pos.repository.EvenimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenimentService {

    private final EvenimentRepository evenimentRepository;

     //iau toate evenimentele si le convertesc in DTO
     // Găsește toate evenimentele și le convertește în DTO

    public List<EvenimentDTO> findAll() {
        return evenimentRepository.findAll()
                .stream()
                .map(this::convertToDTO)  // convertim Entity → DTO
                .collect(Collectors.toList());
    }



    public EvenimentDTO findById(Integer id) {
        Eveniment eveniment = evenimentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eveniment", "id", id));

        return convertToDTO(eveniment);
    }


    // creaza un eveniment nou

    public EvenimentDTO create(EvenimentDTO evenimentDTO) {

        // Validare 1: Numele este obligatoriu
        if (evenimentDTO.getNume() == null || evenimentDTO.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Numele evenimentului este obligatoriu");
        }

        // Validare 2: Verificam dacă numele este unic
        boolean numeExista = evenimentRepository.findAll()
                .stream()
                .anyMatch(e -> e.getNume().equalsIgnoreCase(evenimentDTO.getNume()));

        //409
        if (numeExista) {
            throw new BusinessLogicException("Un eveniment cu acest nume exista deja");
        }

        // Validare 3: Numărul de locuri trebuie >= 0
        if (evenimentDTO.getNumarLocuri() != null && evenimentDTO.getNumarLocuri() < 0) {
            throw new IllegalArgumentException("Numarul de locuri nu poate fi negativ");
        }
        //dto -> entity
        Eveniment eveniment = new Eveniment();
        eveniment.setIdOwner(evenimentDTO.getIdOwner());
        eveniment.setNume(evenimentDTO.getNume());
        eveniment.setLocatie(evenimentDTO.getLocatie());
        eveniment.setDescriere(evenimentDTO.getDescriere());
        eveniment.setNumarLocuri(evenimentDTO.getNumarLocuri());

        // salvare in DB
        Eveniment savedEveniment = evenimentRepository.save(eveniment);

        // entity -> dto -> return
        return convertToDTO(savedEveniment);
    }


    private EvenimentDTO convertToDTO(Eveniment eveniment) {
        EvenimentDTO dto = new EvenimentDTO();
        dto.setId(eveniment.getId());
        dto.setIdOwner(eveniment.getIdOwner());
        dto.setNume(eveniment.getNume());
        dto.setLocatie(eveniment.getLocatie());
        dto.setDescriere(eveniment.getDescriere());
        dto.setNumarLocuri(eveniment.getNumarLocuri());

        //se calculeaza biletele disponibile
        if (eveniment.getBilete() != null && eveniment.getNumarLocuri() != null) {
            int bileteVandute = eveniment.getBilete().size();
            dto.setBileteDisponibile(eveniment.getNumarLocuri() - bileteVandute);
        }

        return dto;
    }
}