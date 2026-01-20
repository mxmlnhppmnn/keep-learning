package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    boolean existsByStudentIdAndSeriesId(Long studentId, Long seriesId);

    List<Invoice> findByStudentIdAndSeriesIdOrderByCreatedAtDesc(Long studentId, Long seriesId);
}
