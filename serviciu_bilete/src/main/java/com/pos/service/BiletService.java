package com.pos.service;

import com.pos.dto.BiletDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.models.Bilet;
import com.pos.models.Eveniment;
import com.pos.models.Pachet;
import com.pos.repository.BiletRepository;
import com.pos.repository.EvenimentRepository;
import com.pos.repository.PachetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BiletService {

    private final BiletRepository biletRepository;
    private final EvenimentRepository evenimentRepository;
    private final PachetRepository pachetRepository;

    /**
     * gaseste toate biletele
     */
    public List<BiletDTO> findAll() {
        return biletRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * gaseste un bilet dupa cod
     */
    public BiletDTO findByCod(String cod) {
        Bilet bilet = biletRepository.findById(cod)
                .orElseThrow(() -> new ResourceNotFoundException("Bilet", "cod", cod));

        return convertToDTO(bilet);
    }

    /**
     * Creeaza un bilet nou (POST)
     * Body: { "evenimentId": 1 } SAU { "pachetId": 1 }
     */
    public BiletDTO create(BiletDTO biletDTO) {
        // Validare 1: Trebuie sa aiba fie eveniment, fie pachet
        if (biletDTO.getEvenimentId() == null && biletDTO.getPachetId() == null) {
            throw new IllegalArgumentException("Biletul trebuie asociat unui eveniment sau unui pachet");
        }

        // Validare 2: NU poate avea AMBELE
        if (biletDTO.getEvenimentId() != null && biletDTO.getPachetId() != null) {
            throw new BusinessLogicException("Biletul nu poate fi asociat simultan unui eveniment si unui pachet");
        }

        Bilet bilet = new Bilet();

        // ✨ Genereaza cod automat
        bilet.setCod(generateUniqueCode());

        // Asociaza cu eveniment sau pachet
        if (biletDTO.getEvenimentId() != null) {
            Eveniment eveniment = evenimentRepository.findById(biletDTO.getEvenimentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Eveniment", "id", biletDTO.getEvenimentId()));
            bilet.setEveniment(eveniment);
        }

        if (biletDTO.getPachetId() != null) {
            Pachet pachet = pachetRepository.findById(biletDTO.getPachetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pachet", "id", biletDTO.getPachetId()));
            bilet.setPachet(pachet);
        }

        Bilet savedBilet = biletRepository.save(bilet);
        return convertToDTO(savedBilet);
    }

    /**
     * DELETE
     */
    public BiletDTO delete(String cod) {
        Bilet bilet = biletRepository.findById(cod)
                .orElseThrow(() -> new ResourceNotFoundException("Bilet", "cod", cod));

        BiletDTO deletedDTO = convertToDTO(bilet);
        biletRepository.delete(bilet);

        return deletedDTO;
    }

    /**
     * Genereaza un cod unic pentru bilet
     * Format: BILET-{UUID-8-caractere}
     */
    private String generateUniqueCode() {
        String cod;
        do {
            String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            cod = "BILET-" + uuid;
        } while (biletRepository.existsById(cod));

        return cod;
    }

    /**
     * Converteste Bilet -> BiletDTO
     */
    private BiletDTO convertToDTO(Bilet bilet) {
        BiletDTO dto = new BiletDTO();
        dto.setCod(bilet.getCod());

        if (bilet.getEveniment() != null) {
            dto.setEvenimentId(bilet.getEveniment().getId());
            dto.setEvenimentNume(bilet.getEveniment().getNume());
        }

        if (bilet.getPachet() != null) {
            dto.setPachetId(bilet.getPachet().getId());
            dto.setPachetNume(bilet.getPachet().getNume());
        }

        return dto;
    }

    /**
     * gaseste bilete pentru un eveniment
     */
    public List<BiletDTO> findByEvenimentId(Integer evenimentId) {
        return biletRepository.findByEvenimentId(evenimentId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * gaseste bilete pentru un pachet
     */
    public List<BiletDTO> findByPachetId(Integer pachetId) {
        return biletRepository.findByPachetId(pachetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}