package com.example.keeplearning.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class FileShare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User sender;

    @ManyToOne
    private User destination;

    private String filePath;
    private String fancyFilename;
    private String comment;
    private LocalDateTime sentAt;

    public static FileShare create(User sender, User destination, String filePath, String fancyFilename, String comment) {
        var shared = new FileShare();
        shared.sender = sender;
        shared.destination = destination;
        shared.filePath = filePath;
        shared.fancyFilename = fancyFilename;
        shared.comment = comment;
        shared.sentAt = LocalDateTime.now();
        return shared;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getDestination() {
        return destination;
    }

    public void setDestination(User destination) {
        this.destination = destination;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFancyFilename() {
        return fancyFilename;
    }

    public void setFancyFilename(String fancyFilename) {
        this.fancyFilename = fancyFilename;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

}
