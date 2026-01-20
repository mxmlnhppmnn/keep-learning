package com.example.keeplearning.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "income_statement")
public class IncomeStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teacherId;
    private Long seriesId;

    private LocalDateTime createdAt;
    private Double totalAmount;

    // Siehe Invoice#content: explizit als TEXT mappen, um Postgres-LOB/auto-commit Probleme zu vermeiden.
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String content;

    private String filename;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getTeacherId() { return teacherId; }

    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public Long getSeriesId() { return seriesId; }

    public void setSeriesId(Long seriesId) { this.seriesId = seriesId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Double getTotalAmount() { return totalAmount; }

    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getFilename() { return filename; }

    public void setFilename(String filename) { this.filename = filename; }
}
