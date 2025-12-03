package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {

    // für später, wenn login da ist
    Benutzer findByEmail(String email);
}
