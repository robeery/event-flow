package com.pos.repository;

import com.pos.models.PachetEveniment;
import com.pos.models.PachetEvenimentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Folosim clasa PachetEvenimentId pentru cheia primară compusa
//<template, id>
@Repository
public interface PachetEvenimentRepository extends JpaRepository<PachetEveniment, PachetEvenimentId> {
}
