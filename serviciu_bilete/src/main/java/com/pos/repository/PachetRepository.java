package com.pos.repository;

import com.pos.models.Pachet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Folosim Integer pentru cheia primara 'id'
//<template, id>
@Repository
public interface PachetRepository extends JpaRepository<Pachet, Integer> {

    //paginare
    Page<Pachet> findAll(Pageable pageable);

    //cauta dupa tip in descriere
    List<Pachet> findByDescriereContainingIgnoreCase(String type);
}
