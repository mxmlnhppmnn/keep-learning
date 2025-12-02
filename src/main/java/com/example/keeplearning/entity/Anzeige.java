package com.example.keeplearning.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "anzeige")  // anzeige (Tabelle) wird erstellt falls nicht vorhanden
public class Anzeige {
    @Id //primärschlüssel
    @GeneratedValue(strategy = GenerationType.IDENTITY) //automatische ID-Vergabe

    private Long anzeigeId;
    private Long lehrerId;
    private String titel;
    private String beschreibung;
    private Double preis;
    private String bildpfad;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fachId", referencedColumnName = "fachId")
    private Fach fach;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schulartId", referencedColumnName = "schulartId")
    private Schulart schulart;

    private Boolean probestunde;

   //Getter und Setter

    public Long getAnzeigeId() {
        return anzeigeId;
    }

    public void setAnzeigeId(Long anzeigeId) {
        this.anzeigeId = anzeigeId;
    }

    public Long getLehrerId() {
        return lehrerId;
    }

    public void setLehrerId(Long lehrerId) {
        this.lehrerId = lehrerId;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public Double getPreis() {
        return preis;
    }

    public void setPreis(Double preis) {
        this.preis = preis;
    }

    public String getBildpfad() {
        return bildpfad;
    }

    public void setBildpfad(String bildpfad) {
        this.bildpfad = bildpfad;
    }

    public Fach getFach() {
        return fach;
    }

    public void setFach(Fach fach) {
        this.fach = fach;
    }

    public Schulart getSchulart() {
        return schulart;
    }

    public void setSchulart(Schulart schulart) {
        this.schulart = schulart;
    }

    public Boolean getProbestunde() {
        return probestunde;
    }

    public void setProbestunde(Boolean probestunde) {
        this.probestunde = probestunde;
    }

}
