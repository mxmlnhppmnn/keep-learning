package com.example.keeplearning.entity.verification;

import jakarta.persistence.*;
import com.example.keeplearning.entity.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Wer den Antrag gestellt hat
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationRequestStatus status;

    // Pfad zur hochgeladenen Datei
    @Column(nullable = false)
    private String documentPath;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // gesetzt bei APPROVED / REJECTED
    private LocalDateTime reviewedAt;

    // Admin, der entschieden hat
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    // nur bei REJECTED
    @Column(length = 1000)
    private String rejectionReason;

    // ---- Konstruktoren ----

    protected VerificationRequest() {
        // JPA
    }

    public VerificationRequest(User user, String documentPath) {
        this.user = user;
        this.documentPath = documentPath;
        this.status = VerificationRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    //Getter und Setter


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VerificationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(VerificationRequestStatus status) {
        this.status = status;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
