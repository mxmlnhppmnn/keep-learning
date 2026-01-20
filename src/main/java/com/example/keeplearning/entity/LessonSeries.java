package com.example.keeplearning.entity;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_series")
public class LessonSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teacherId;
    private Long studentId;
    private Long subjectId;

    // Referenz auf die Anzeige ("Kurs"), aus der die Buchung entstanden ist.
    private Long advertisementId;

    private int weekday;

    private LocalTime startTime;

    private int duration;

    private boolean trialLesson;

    // Preis pro Stunde (fuer Rechnungsverwaltung). Wird beim Buchen aus der Anzeige kopiert,
    // damit sich Rechnungen nicht aendern, falls der Lehrer spaeter den Anzeigenpreis aendert.
    private Double pricePerHour;

    // Zahlungsabwicklung (Mock, keine echte PayPal-API)
    private String paymentMethod; // PAYPAL | SEPA | null
    private boolean paid;
    private LocalDateTime paidAt;

    //Getter und Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long lehrerId) {
        this.teacherId = lehrerId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getAdvertisementId() {
        return advertisementId;
    }

    public void setAdvertisementId(Long advertisementId) {
        this.advertisementId = advertisementId;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isTrialLesson() {
        return trialLesson;
    }

    public void setTrialLesson(boolean trialLesson) {
        this.trialLesson = trialLesson;
    }

    public Double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
        this.paidAt = paid ? LocalDateTime.now() : null;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}

