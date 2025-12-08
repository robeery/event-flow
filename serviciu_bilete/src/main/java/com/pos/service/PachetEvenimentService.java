package com.pos.service;

import com.pos.dto.EvenimentDTO;
import com.pos.dto.PachetDTO;
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
}