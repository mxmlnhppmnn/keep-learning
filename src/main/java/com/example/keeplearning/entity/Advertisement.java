package com.example.keeplearning.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "advertisement")  // anzeige (Tabelle) wird erstellt falls nicht vorhanden
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // OK ohne @Column!

    @ManyToOne
    @JoinColumn(name = "user_id") // nötig
    private User user;

    private String title; // OK ohne @Column
    private String description; // OK ohne @Column
    private Double price; // OK ohne @Column
    private String imagePath; // OK ohne @Column

    @ManyToOne
    @JoinColumn(name = "subject_id")  // nötig
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "school_type_id") // nötig
    private SchoolType schoolType;

    private Boolean trialLesson;

    //Getter und Setter

    public Long getId() {
        return id;
    }

    public void setId(Long anzeigeId) {
        this.id = anzeigeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String titel) {
        this.title = titel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String beschreibung) {
        this.description = beschreibung;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double preis) {
        this.price = preis;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String bildpfad) {
        this.imagePath = bildpfad;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public SchoolType getSchoolType() {
        return schoolType;
    }

    public void setSchoolType(SchoolType schoolType) {
        this.schoolType = schoolType;
    }

    public Boolean getTrialLesson() {
        return trialLesson;
    }

    public void setTrialLesson(Boolean probestunde) {
        this.trialLesson = probestunde;
    }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

}
