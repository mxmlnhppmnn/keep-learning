package com.example.keeplearning.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fach")
public class Fach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fachId;

    @Column(unique = true, nullable = false)
    private String name;

    // Getter und Setter

    public Long getFachId() {
        return fachId;
    }

    public void setFachId(Long fachId) {
        this.fachId = fachId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

