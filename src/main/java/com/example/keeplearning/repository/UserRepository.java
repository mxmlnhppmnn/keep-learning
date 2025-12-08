package com.example.keeplearning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.keeplearning.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    boolean existsByEmail(String email);
}
