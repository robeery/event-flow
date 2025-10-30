package com.pos;

import com.pos.models.Bilet;
import com.pos.models.Eveniment;
import com.pos.models.Pachet;
import com.pos.models.PachetEveniment;
import com.pos.repository.BiletRepository;
import com.pos.repository.EvenimentRepository;
import com.pos.repository.PachetEvenimentRepository;
import com.pos.repository.PachetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServiciuBileteApplication {


    @Autowired
    private EvenimentRepository evenimentRepository;
    @Autowired
    private PachetRepository pachetRepository;
    @Autowired
    private BiletRepository biletRepository;
    @Autowired
    private PachetEvenimentRepository pachetEvenimentRepository;

    public static void main(String[] args) {
        SpringApplication.run(ServiciuBileteApplication.class, args);
    }

    @Bean
    CommandLineRunner run() {
        return args -> {
            System.out.println("--- Se rulează Task 1: Crearea schemelor și testarea datelor ---");

            // adaug date doar daca este goala
            if (evenimentRepository.count() == 0 && pachetRepository.count() == 0) {
                System.out.println("Baza de date este goală. Se populează cu date de test...");

                // creez eveniment
                Eveniment eveniment = new Eveniment();
                eveniment.setIdOwner(1); // ID Owner initial
                eveniment.setNume("Concert Rock Clasic");
                eveniment.setLocatie("Stadionul Olimpic");
                eveniment.setDescriere("Un concert cu trupe legendare.");
                eveniment.setNumarLocuri(5000);
                Eveniment evenimentSalvat = evenimentRepository.save(eveniment);
                System.out.println("Eveniment creat: " + evenimentSalvat.getNume());

                // creez pachet
                Pachet pachet = new Pachet();
                pachet.setIdOwner(1);
                pachet.setNume("Pachetul Toamnei Rock");
                pachet.setLocatie("Diverse locații");
                pachet.setDescriere("Include acces la mai multe evenimente rock.");
                Pachet pachetSalvat = pachetRepository.save(pachet);
                System.out.println("Pachet creat: " + pachetSalvat.getNume());

                // Leg Evenimentul de Pachet (prin tabela JOIN_PE / PachetEveniment)
                PachetEveniment legatura = new PachetEveniment(pachetSalvat, evenimentSalvat, 500); // Alocăm 500 locuri
                pachetEvenimentRepository.save(legatura);
                System.out.println("Legătură creată: " + pachetSalvat.getNume() + " <-> " + evenimentSalvat.getNume());

                // creez un Bilet pentru Pachetul respectiv
                Bilet bilet = new Bilet();
                bilet.setCod("A123");
                bilet.setPachet(pachetSalvat); // leg biletul de pachet
                // bilet.setEveniment(null); // Este null, deoarece biletul e pentru pachet
                biletRepository.save(bilet);
                System.out.println("Bilet creat: " + bilet.getCod() + " pentru pachetul " + bilet.getPachet().getNume());

                System.out.println("--- Datele de test au fost salvate! ---");

            } else {
                System.out.println("--- Baza de date este deja populată. Se sare peste crearea datelor de test. ---");
            }


            //citesc si afisez date
            System.out.println("\n--- Verificare date din Baza de Date ---");
            evenimentRepository.findAll().forEach(ev -> {
                System.out.println("Eveniment găsit: " + ev.getNume() + " (ID: " + ev.getId() + ")");
                // Afișez și pachetele asociate
                ev.getPacheteAsociate().forEach(pachEv -> {
                    System.out.println("    -> Face parte din pachetul: " + pachEv.getPachet().getNume());
                    System.out.println("    -> Locuri alocate pachetului: " + pachEv.getNumarLocuri());
                });
            });

            biletRepository.findAll().forEach(b -> {
                System.out.println("Bilet găsit: " + b.getCod());
                if (b.getPachet() != null) {
                    System.out.println("    -> Pentru Pachetul: " + b.getPachet().getNume());
                }
                if (b.getEveniment() != null) {
                    System.out.println("    -> Pentru Evenimentul: " + b.getEveniment().getNume());
                }
            });
            System.out.println("-----------------------------------------");
            System.out.println("Indexul API:         http://localhost:8080");
            System.out.println("Toate Evenimentele:  http://localhost:8080/eveniments");
            System.out.println("Toate Pachetele:     http://localhost:8080/pachets");
            System.out.println("Toate Biletele:      http://localhost:8080/bilets");
        };

    }
}

