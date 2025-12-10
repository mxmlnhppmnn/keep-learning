package com.example.keeplearning.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.keeplearning.entity.Subject;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByNameIgnoreCase(String name);
}

