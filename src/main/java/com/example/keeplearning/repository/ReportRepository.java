package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
