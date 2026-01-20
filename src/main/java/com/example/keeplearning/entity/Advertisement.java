package com.example.keeplearning.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "advertisement")  // anzeige (Tabelle) wird erstellt falls nicht vorhanden
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String description;
    private Double price;
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "school_type_id")
    private SchoolType schoolType;

    private Boolean trialLesson;

    // Wird auf true gesetzt, sobald ein Schueler die Anzeige gebucht hat.
    // Dadurch wird die Anzeige fuer andere Schueler nicht mehr angezeigt/gebucht.
    @Column(nullable = false)
    private boolean booked = false;

    // Schueler-ID, der diese Anzeige gebucht hat (nur relevant, wenn booked == true)
    private Long bookedStudentId;

    // Serie, die durch die Buchung entstanden ist (nur relevant, wenn booked == true)
    private Long bookedSeriesId;

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

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public Long getBookedStudentId() {
        return bookedStudentId;
    }

    public void setBookedStudentId(Long bookedStudentId) {
        this.bookedStudentId = bookedStudentId;
    }

    public Long getBookedSeriesId() {
        return bookedSeriesId;
    }

    public void setBookedSeriesId(Long bookedSeriesId) {
        this.bookedSeriesId = bookedSeriesId;
    }

}
