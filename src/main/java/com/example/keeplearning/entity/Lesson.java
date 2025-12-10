package com.example.keeplearning.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "lesson")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "series_id")
    private Long seriesId;

    @Column(name = "start_time")
    private LocalTime startTime;

    private LocalDate date;

    private String status; // für abgesagt z.B.

    // Getter und Setter




    public Long getId() {return id;}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate datum) {
        this.date = datum;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
