package com.example.keeplearning.repository;

import com.example.keeplearning.entity.IncomeStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomeStatementRepository extends JpaRepository<IncomeStatement, Long> {

    List<IncomeStatement> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    boolean existsByTeacherIdAndSeriesId(Long teacherId, Long seriesId);
}
