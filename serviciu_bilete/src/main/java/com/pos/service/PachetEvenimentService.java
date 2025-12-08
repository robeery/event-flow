package com.pos.service;

import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.models.Eveniment;
import com.pos.models.Pachet;
import com.pos.models.PachetEveniment;
import com.pos.models.PachetEvenimentId;
import com.pos.repository.PachetEvenimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PachetEvenimentService {

    private final PachetEvenimentRepository pachetEvenimentRepository;
    private final EvenimentService evenimentService;
    private final PachetService pachetService;


     // gaseste toate pachetele care contin un eveniment

    public List<PachetDTO> findPacheteForEveniment(Integer evenimentId) {

        evenimentService.findById(evenimentId);

        return pachetEvenimentRepository.findByEvenimentId(evenimentId)
                .stream()
                .map(pe -> pachetService.convertToDTO(pe.getPachet()))
                .collect(Collectors.toList());
    }


     //gaseste toate evenimentele dintr-un pachet

    public List<EvenimentDTO> findEvenimenteForPachet(Integer pachetId) {

        pachetService.findById(pachetId);

        return pachetEvenimentRepository.findByPachetId(pachetId)
                .stream()
                .map(pe -> evenimentService.convertToDTO(pe.getEveniment()))
                .collect(Collectors.toList());
    }

    /**
     * Adaugă un eveniment într-un pachet
     */
    public void addEvenimentToPachet(Integer pachetId, Integer evenimentId) {
        // Verifică că pachetul și evenimentul există
        Pachet pachet = pachetService.findEntityById(pachetId);
        Eveniment eveniment = evenimentService.findEntityById(evenimentId);

        // Verifică dacă asocierea deja există
        PachetEvenimentId id = new PachetEvenimentId(pachetId, evenimentId);
        if (pachetEvenimentRepository.existsById(id)) {
            throw new BusinessLogicException("Evenimentul este deja inclus in acest pachet");
        }

        // Creează asocierea
        PachetEveniment pe = new PachetEveniment();
        pe.setId(id);
        pe.setPachet(pachet);
        pe.setEveniment(eveniment);


        pachetEvenimentRepository.save(pe);
    }

    /**
     * Adauga un pachet pentru un eveniment (aceeasi operație, alt context)
     */
    public void addPachetToEveniment(Integer evenimentId, Integer pachetId) {
        addEvenimentToPachet(pachetId, evenimentId);
    }
}