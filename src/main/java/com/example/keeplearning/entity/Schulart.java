package com.example.keeplearning.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Schulart")
public class Schulart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schulartId;

    @Column(name = "Name", nullable = false, unique = true)
    private String name;

    // Getter und Setter

    public Long getSchulartId() {
        return schulartId;
    }

    public void setSchulartId(Long schulartId) {
        this.schulartId = schulartId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
