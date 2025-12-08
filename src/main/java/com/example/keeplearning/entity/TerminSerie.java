package com.example.keeplearning.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "terminserie")
public class TerminSerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serieId;

    private Long lehrerId;
    private Long schuelerId;
    private Long fachId;

    private int wochentag;

    private LocalTime startzeit;

    private int dauer; // Minuten

    private boolean istProbestunde;

    //Getter und Setter

    public Long getSerieId() {
        return serieId;
    }

    public void setSerieId(Long serieId) {
        this.serieId = serieId;
    }

    public Long getLehrerId() {
        return lehrerId;
    }

    public void setLehrerId(Long lehrerId) {
        this.lehrerId = lehrerId;
    }

    public Long getSchuelerId() {
        return schuelerId;
    }

    public void setSchuelerId(Long schuelerId) {
        this.schuelerId = schuelerId;
    }

    public Long getFachId() {
        return fachId;
    }

    public void setFachId(Long fachId) {
        this.fachId = fachId;
    }

    public int getWochentag() {
        return wochentag;
    }

    public void setWochentag(int wochentag) {
        this.wochentag = wochentag;
    }

    public LocalTime getStartzeit() {
        return startzeit;
    }

    public void setStartzeit(LocalTime startzeit) {
        this.startzeit = startzeit;
    }

    public int getDauer() {
        return dauer;
    }

    public void setDauer(int dauer) {
        this.dauer = dauer;
    }

    public boolean isIstProbestunde() {
        return istProbestunde;
    }

    public void setIstProbestunde(boolean istProbestunde) {
        this.istProbestunde = istProbestunde;
    }
}

