package com.example.keeplearning.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.keeplearning.entity.Fach;

import java.util.Optional;

public interface FachRepository extends JpaRepository<Fach, Long> {
    Optional<Fach> findByNameIgnoreCase(String bezeichnung);
}

