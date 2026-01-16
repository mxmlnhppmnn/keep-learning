package com.example.keeplearning.entity.chat;

import java.time.LocalDateTime;

import com.example.keeplearning.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "message")
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Chat chat;

    @ManyToOne
    private User sender;

    @Column(nullable = false)
    private String content;

    private LocalDateTime sentAt;

    public static Message create(Chat chat, User sender, String content) {
        var msg = new Message();
        msg.chat = chat;
        msg.sender = sender;
        msg.content = content;
        msg.sentAt = LocalDateTime.now();
        return msg;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

}
