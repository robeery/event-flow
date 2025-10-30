package com.pos.repository;

import com.pos.models.Bilet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Folosim String pentru cheia primara 'cod'
//<template, id>
@Repository
public interface BiletRepository extends JpaRepository<Bilet, String> {
}
