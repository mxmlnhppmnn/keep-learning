package com.example.keeplearning.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    // Jede Rechnung bezieht sich auf genau einen Kurs (Terminserie)
    private Long seriesId;

    // Der zugehoerige Lehrer (zur Anzeige/Abrechnung)
    private Long teacherId;

    private LocalDateTime createdAt;

    // Gesamtsumme in EUR
    private Double totalAmount;

    /**
     * Rechnungstext.
     *
     * WICHTIG: In PostgreSQL kann @Lob String als "Large Object" (OID) gemappt werden.
     * Das fuehrt beim Lesen ausserhalb einer Transaktion zu
     * "LargeObjects (LOB) duerfen im Modus 'auto-commit' nicht verwendet werden".
     *
     * Daher explizit als TEXT/LONGVARCHAR mappen (kein LOB).
     */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "text")
    private String content;

    private String filename;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
