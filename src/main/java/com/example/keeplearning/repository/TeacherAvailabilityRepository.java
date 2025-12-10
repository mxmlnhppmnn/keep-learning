package com.example.keeplearning.repository;

import com.example.keeplearning.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherAvailabilityRepository
        extends JpaRepository<TeacherAvailability, Long> {

    List<TeacherAvailability> findByUserId(Long userId);
}
