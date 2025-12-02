package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {

    // Darauf greifen wir später zu, wenn sich jemand mit Email einloggt
    Benutzer findByEmail(String email);
}
