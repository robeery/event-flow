package com.pos.repository;

import com.pos.models.Bilet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Folosim String pentru cheia primara 'cod'
//<template, id>
@Repository
public interface BiletRepository extends JpaRepository<Bilet, String> {


    //gaseste bilete pentru un eveniment
    List<Bilet> findByEvenimentId(Integer evenimentId);

    //gaseste un bilet pentru un pachet
    List<Bilet> findByPachetId(Integer pachetId);


}
