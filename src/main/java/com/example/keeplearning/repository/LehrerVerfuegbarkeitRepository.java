package com.example.keeplearning.repository;

import com.example.keeplearning.entity.LehrerVerfuegbarkeit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LehrerVerfuegbarkeitRepository
        extends JpaRepository<LehrerVerfuegbarkeit, Long> {

    List<LehrerVerfuegbarkeit> findByUserId(Long userId);
}
