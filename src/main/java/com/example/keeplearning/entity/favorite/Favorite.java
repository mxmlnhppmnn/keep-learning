package com.example.keeplearning.entity.favorite;

import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorite", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "advertisement_id"})
        })
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "advertisement_id")
    private Advertisement advertisement;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Advertisement getAdvertisement() { return advertisement; }
    public void setAdvertisement(Advertisement advertisement) {
        this.advertisement = advertisement;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
