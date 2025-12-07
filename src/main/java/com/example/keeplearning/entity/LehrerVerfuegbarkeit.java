package com.example.keeplearning.entity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "lehrer_verfuegbarkeit")
public class LehrerVerfuegbarkeit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verfuegbarkeit_id")
    private Long verfuegbarkeitId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wochentag", nullable = false)
    private int wochentag;

    @Column(name = "start_zeit", nullable = false)
    private LocalTime startZeit;

    @Column(name = "end_zeit", nullable = false)
    private LocalTime endZeit;

    @Column(name = "gueltig_ab")
    private LocalDate gueltigAb;

    @Column(name = "gueltig_bis")
    private LocalDate gueltigBis;

    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    //Getter und Setter


    public Long getVerfuegbarkeitId() {
        return verfuegbarkeitId;
    }

    public void setVerfuegbarkeitId(Long verfuegbarkeitId) {
        this.verfuegbarkeitId = verfuegbarkeitId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getWochentag() {
        return wochentag;
    }

    public void setWochentag(int wochentag) {
        this.wochentag = wochentag;
    }

    public LocalTime getStartZeit() {
        return startZeit;
    }

    public void setStartZeit(LocalTime startZeit) {
        this.startZeit = startZeit;
    }

    public LocalTime getEndZeit() {
        return endZeit;
    }

    public void setEndZeit(LocalTime endZeit) {
        this.endZeit = endZeit;
    }

    public LocalDate getGueltigAb() {
        return gueltigAb;
    }

    public void setGueltigAb(LocalDate gueltigAb) {
        this.gueltigAb = gueltigAb;
    }

    public LocalDate getGueltigBis() {
        return gueltigBis;
    }

    public void setGueltigBis(LocalDate gueltigBis) {
        this.gueltigBis = gueltigBis;
    }

    public boolean isAktiv() {
        return aktiv;
    }

    public void setAktiv(boolean aktiv) {
        this.aktiv = aktiv;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


