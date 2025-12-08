package com.pos.service;

import com.pos.dto.EvenimentDTO;
import com.pos.exception.BusinessLogicException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.models.Eveniment;
import com.pos.repository.EvenimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvenimentService {

    private final EvenimentRepository evenimentRepository;

    @PersistenceContext
    private EntityManager entityManager;

     //iau toate evenimentele si le convertesc in DTO
     // gaseste toate evenimentele si le converteste in DTO

    public List<EvenimentDTO> findAll() {
        return evenimentRepository.findAll()
                .stream()
                .map(this::convertToDTO)  // convertim Entity → DTO
                .collect(Collectors.toList());

        //sau
        // return findAll(null, null);
    }

    //filtrare optionala
    public List<EvenimentDTO> findAll(String location, String name) {
        List<Eveniment> evenimente;

        if (location != null && !location.trim().isEmpty()) {
            evenimente = evenimentRepository.findByLocatie(location);
        } else if (name != null && !name.trim().isEmpty()) {
            evenimente = evenimentRepository.findByNumeContainingIgnoreCase(name);
        } else {
            evenimente = evenimentRepository.findAll();
        }

        return evenimente.stream()
                .map(this::convertToDTO)
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


    public EvenimentDTO delete(Integer id) {
        Eveniment eveniment = evenimentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eveniment", "id", id));

        // Validare 1: Are bilete?
        if (eveniment.getBilete() != null && !eveniment.getBilete().isEmpty()) {
            throw new BusinessLogicException(
                    "Nu se poate sterge un eveniment cu bilete. Numar bilete: " +
                            eveniment.getBilete().size()
            );
        }

        // Validare 2: Face parte din pachete?
        if (eveniment.getPacheteAsociate() != null && !eveniment.getPacheteAsociate().isEmpty()) {
            throw new BusinessLogicException(
                    "Nu se poate sterge un eveniment care face parte din pachete active. " +
                            "Numar pachete: " + eveniment.getPacheteAsociate().size()
            );
        }

        EvenimentDTO deletedDTO = convertToDTO(eveniment);
        evenimentRepository.delete(eveniment);

        return deletedDTO;
    }



     //PUT - actualizeaza sau creaza evenimentul


    public EvenimentDTO update(Integer id, EvenimentDTO evenimentDTO) {
        // Validare 1: numele e obligatoriu
        if (evenimentDTO.getNume() == null || evenimentDTO.getNume().trim().isEmpty()) {
            throw new IllegalArgumentException("Numele evenimentului este obligatoriu");
        }

        // Validare 2: Numarul de locuri trebuie pozitiv
        if (evenimentDTO.getNumarLocuri() != null && evenimentDTO.getNumarLocuri() < 0) {
            throw new IllegalArgumentException("Numarul de locuri nu poate fi negativ");
        }


        Optional<Eveniment> existingOptional = evenimentRepository.findById(id);

        if (existingOptional.isPresent()) {

            Eveniment existing = existingOptional.get();

            // Validare 3: se verifica daca se schimba numele daca e UK
            if (!existing.getNume().equalsIgnoreCase(evenimentDTO.getNume())) {
                boolean numeExista = evenimentRepository.findAll()
                        .stream()
                        .anyMatch(e -> !e.getId().equals(id) &&
                                e.getNume().equalsIgnoreCase(evenimentDTO.getNume()));

                if (numeExista) {
                    throw new BusinessLogicException("Un eveniment cu acest nume exista deja");
                }
            }

            // Validare 4: Are bilete?
            if (evenimentDTO.getNumarLocuri() != null) {
                int bileteVandute = (existing.getBilete() != null) ? existing.getBilete().size() : 0;

                if (evenimentDTO.getNumarLocuri() < bileteVandute) {
                    throw new BusinessLogicException(
                            "Numarul de locuri (" + evenimentDTO.getNumarLocuri() +
                                    ") nu poate fi mai mic decât numărul de bilete ve (" + bileteVandute + ")"
                    );
                }
            }


            existing.setIdOwner(evenimentDTO.getIdOwner());
            existing.setNume(evenimentDTO.getNume());
            existing.setLocatie(evenimentDTO.getLocatie());
            existing.setDescriere(evenimentDTO.getDescriere());
            existing.setNumarLocuri(evenimentDTO.getNumarLocuri());


            Eveniment updated = evenimentRepository.save(existing);

            return convertToDTO(updated);

        } else {
            // CREARE - evenimentul NU exista

            // Validare: verific nume UK
            boolean numeExista = evenimentRepository.findAll()
                    .stream()
                    .anyMatch(e -> e.getNume().equalsIgnoreCase(evenimentDTO.getNume()));

            if (numeExista) {
                throw new BusinessLogicException("Un eveniment cu acest nume există deja");
            }

            // creez eveniment nou cu ID-ul specificat



            //DEOARECE HIBERNATE/JPA FACE FIGURI SI CUM NU VREAU SA MANAGERIEZ MANUAL PK-URILE
            //INSEREZ DIRECT IN BD
            entityManager.createNativeQuery(
                            "INSERT INTO evenimente (id, id_owner, nume, locatie, descriere, numar_locuri) " +
                                    "VALUES (:id, :idOwner, :nume, :locatie, :descriere, :numarLocuri)"
                    )
                    .setParameter("id", id)
                    .setParameter("idOwner", evenimentDTO.getIdOwner())
                    .setParameter("nume", evenimentDTO.getNume())
                    .setParameter("locatie", evenimentDTO.getLocatie())
                    .setParameter("descriere", evenimentDTO.getDescriere())
                    .setParameter("numarLocuri", evenimentDTO.getNumarLocuri())
                    .executeUpdate();


            //elegant ar fi pe viitor sa imi adaug eu in globalexceptionhandler ceva frumos, nu banal 500 de la spring
            Eveniment created = evenimentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Failed to create event"));

            entityManager.flush();

            return convertToDTO(created);
        }
    }




    public EvenimentDTO convertToDTO(Eveniment eveniment) {
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



    public boolean existsById(Integer id) {
        return evenimentRepository.existsById(id);
    }

    public Eveniment findEntityById(Integer id) {
        return evenimentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Eveniment", "id", id));
    }
}