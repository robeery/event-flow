package com.pos.repository;

import com.pos.models.PachetEveniment;
import com.pos.models.PachetEvenimentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// Folosim clasa PachetEvenimentId pentru cheia primară compusa
//<template, id>


@Repository
public interface PachetEvenimentRepository extends JpaRepository<PachetEveniment, PachetEvenimentId> {

    List<PachetEveniment> findByEvenimentId(Integer evenimentId);

    List<PachetEveniment> findByPachetId(Integer pachetId);
}
