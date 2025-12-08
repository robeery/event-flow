package com.pos.repository;

import com.pos.models.Eveniment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Folosim Integer pentru cheia primara 'id'
//<template, id>
@Repository
public interface EvenimentRepository extends JpaRepository<Eveniment, Integer> {


    List<Eveniment> findByLocatie(String locatie);

    List<Eveniment> findByNumeContainingIgnoreCase(String nume);
}
