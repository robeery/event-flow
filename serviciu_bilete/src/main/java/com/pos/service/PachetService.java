package com.pos.service;

import com.pos.dto.PachetDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.models.Pachet;
import com.pos.repository.PachetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PachetService {

    private final PachetRepository pachetRepository;

    @PersistenceContext
    private EntityManager entityManager;


     // iau toate pachetele si  le convertesc în DTO

    public List<PachetDTO> findAll() {
        return pachetRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    public PachetDTO findById(Integer id) {
        Pachet pachet = pachetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pachet", "id", id));

        return convertToDTO(pachet);
    }

   //post
    public PachetDTO create(PachetDTO pachetDTO) {

        //validare 1
        if (pachetDTO.getNume() == null || pachetDTO.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Numele pachetului este obligatoriu");
        }

        // validare 2
        boolean numeExista = pachetRepository.findAll()
                .stream()
                .anyMatch(p -> p.getNume().equalsIgnoreCase(pachetDTO.getNume()));

        if (numeExista) {
            throw new BusinessLogicException("Un pachet cu acest nume exista deja");
        }

        // Validare 3
        if (pachetDTO.getNumarLocuri() != null && pachetDTO.getNumarLocuri() < 0) {
            throw new IllegalArgumentException("Numarul de locuri nu poate fi negativ");
        }

        // DTO -> Entity
        Pachet pachet = new Pachet();
        pachet.setIdOwner(pachetDTO.getIdOwner());
        pachet.setNume(pachetDTO.getNume());
        pachet.setLocatie(pachetDTO.getLocatie());
        pachet.setDescriere(pachetDTO.getDescriere());
        pachet.setNumarLocuri(pachetDTO.getNumarLocuri());

        // salvare in db
        Pachet savedPachet = pachetRepository.save(pachet);

        return convertToDTO(savedPachet);
    }

   //delete
    public PachetDTO delete(Integer id) {
        Pachet pachet = pachetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pachet", "id", id));

        // Validare: Are bilete?
        if (pachet.getBilete() != null && !pachet.getBilete().isEmpty()) {
            throw new BusinessLogicException(
                    "Nu se poate sterge un pachet cu bilete vandute. " +
                            "Numar bilete: " + pachet.getBilete().size()
            );
        }

        PachetDTO deletedDTO = convertToDTO(pachet);
        pachetRepository.delete(pachet);

        return deletedDTO;
    }


    //put


    @SuppressWarnings("JpaQueryApiInspection")
    public PachetDTO update(Integer id, PachetDTO pachetDTO) {
        // Validare 1: Numele este obligatoriu
        if (pachetDTO.getNume() == null || pachetDTO.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Numele pachetului este obligatoriu");
        }

        // Validare 2: nr de locuri trebuie >= 0
        if (pachetDTO.getNumarLocuri() != null && pachetDTO.getNumarLocuri() < 0) {
            throw new IllegalArgumentException("Numarul de locuri nu poate fi negativ");
        }

        Optional<Pachet> existingOptional = pachetRepository.findById(id);

        if (existingOptional.isPresent()) {

            Pachet existing = existingOptional.get();

            // Validare nume unic
            if (!existing.getNume().equalsIgnoreCase(pachetDTO.getNume())) {
                boolean numeExista = pachetRepository.findAll()
                        .stream()
                        .anyMatch(p -> !p.getId().equals(id) &&
                                p.getNume().equalsIgnoreCase(pachetDTO.getNume()));

                if (numeExista) {
                    throw new BusinessLogicException("Un pachet cu acest nume exista deja");
                }
            }

            // Validare: nr locuri > bilete vandute
            if (pachetDTO.getNumarLocuri() != null) {
                int bileteVandute = (existing.getBilete() != null) ? existing.getBilete().size() : 0;

                if (pachetDTO.getNumarLocuri() < bileteVandute) {
                    throw new BusinessLogicException(
                            "Numarul de locuri (" + pachetDTO.getNumarLocuri() +
                                    ") nu poate fi mai mic decat numarul de bilete vandute (" + bileteVandute + ")"
                    );
                }
            }

            // Actualizam campuri
            existing.setIdOwner(pachetDTO.getIdOwner());
            existing.setNume(pachetDTO.getNume());
            existing.setLocatie(pachetDTO.getLocatie());
            existing.setDescriere(pachetDTO.getDescriere());
            existing.setNumarLocuri(pachetDTO.getNumarLocuri());

            Pachet updated = pachetRepository.save(existing);
            return convertToDTO(updated);

        } else {
            // create cu id din url

            // Validare nume unic
            boolean numeExista = pachetRepository.findAll()
                    .stream()
                    .anyMatch(p -> p.getNume().equalsIgnoreCase(pachetDTO.getNume()));

            if (numeExista) {
                throw new BusinessLogicException("Un pachet cu acest nume exista deja");
            }

            // creez cu sql direct
            String insertQuery =
                    "INSERT INTO pachete (id, id_owner, nume, locatie, descriere, numar_locuri) " +
                            "VALUES (:id, :idOwner, :nume, :locatie, :descriere, :numarLocuri)";

            entityManager.createNativeQuery(insertQuery)
                    .setParameter("id", id)
                    .setParameter("idOwner", pachetDTO.getIdOwner())
                    .setParameter("nume", pachetDTO.getNume())
                    .setParameter("locatie", pachetDTO.getLocatie())
                    .setParameter("descriere", pachetDTO.getDescriere())
                    .setParameter("numarLocuri", pachetDTO.getNumarLocuri())
                    .executeUpdate();

            entityManager.flush();

            Pachet created = pachetRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Failed to create packet"));

            return convertToDTO(created);
        }
    }


    private PachetDTO convertToDTO(Pachet pachet) {
        PachetDTO dto = new PachetDTO();
        dto.setId(pachet.getId());
        dto.setIdOwner(pachet.getIdOwner());
        dto.setNume(pachet.getNume());
        dto.setLocatie(pachet.getLocatie());
        dto.setDescriere(pachet.getDescriere());
        dto.setNumarLocuri(pachet.getNumarLocuri());

        // Calculeaza bilete disponibile
        if (pachet.getBilete() != null && pachet.getNumarLocuri() != null) {
            int bileteVandute = pachet.getBilete().size();
            dto.setBileteDisponibile(pachet.getNumarLocuri() - bileteVandute);
        }

        return dto;
    }


    public boolean existsById(Integer id) {
        return pachetRepository.existsById(id);
    }
}