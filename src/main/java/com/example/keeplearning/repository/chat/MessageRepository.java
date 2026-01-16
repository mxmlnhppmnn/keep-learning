package com.example.keeplearning.repository.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.keeplearning.entity.chat.Chat;
import com.example.keeplearning.entity.chat.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // TODO: pagination?
    List<Message> findByChatOrderBySentAtAsc(Chat chat);

}
