package com.pos.service;

import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
import com.pos.dto.PachetEvenimentCreateDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.models.Eveniment;
import com.pos.models.Pachet;
import com.pos.models.PachetEveniment;
import com.pos.models.PachetEvenimentId;
import com.pos.repository.PachetEvenimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pos.exception.ResourceNotFoundException;
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
     * Adauga un eveniment intr-un pachet
     */
    public PachetEvenimentCreateDTO addEvenimentToPachet(Integer pachetId, Integer evenimentId) {
        // Verifica ca pachetul si evenimentul exista
        Pachet pachet = pachetService.findEntityById(pachetId);
        Eveniment eveniment = evenimentService.findEntityById(evenimentId);

        // Verifica daca asocierea deja exista
        PachetEvenimentId id = new PachetEvenimentId(pachetId, evenimentId);
        if (pachetEvenimentRepository.existsById(id)) {
            throw new BusinessLogicException("Evenimentul este deja inclus in acest pachet");
        }

        // Creeaza asocierea
        PachetEveniment pe = new PachetEveniment();
        pe.setId(id);
        pe.setPachet(pachet);
        pe.setEveniment(eveniment);

        PachetEvenimentCreateDTO dto = new PachetEvenimentCreateDTO();
        dto.setPachetId(pachetId);
        dto.setEvenimentId(evenimentId);


        pachetEvenimentRepository.save(pe);

        return dto;
    }

    public PachetEvenimentCreateDTO addPachetToEveniment(Integer evenimentId, Integer pachetId) {
        return addEvenimentToPachet(pachetId, evenimentId);
    }




    /**
     * sterge asocierea dintre un eveniment si un pachet
     */

    /*
    public void removeEvenimentFromPachet(Integer pachetId, Integer evenimentId) {
        PachetEvenimentId id = new PachetEvenimentId(pachetId, evenimentId);

        if (!pachetEvenimentRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Asociere",
                    "pachetId=" + pachetId + ", evenimentId=" + evenimentId,
                    ""
            );
        }

        pachetEvenimentRepository.deleteById(id);
    }

     */

    /**
     * sterge asocierea dintre un pachet si un eveniment
     * (aceeasi operație, alt context)
     */

    /*
    public void removePachetFromEveniment(Integer evenimentId, Integer pachetId) {
        removeEvenimentFromPachet(pachetId, evenimentId);
    }

     */

}