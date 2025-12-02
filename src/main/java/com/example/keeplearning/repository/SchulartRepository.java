package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Schulart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchulartRepository extends JpaRepository<Schulart, Long> {
}
