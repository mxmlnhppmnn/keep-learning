package com.example.keeplearning.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.chat.Chat;
import com.example.keeplearning.repository.chat.ChatRepository;

@Service
public class ChatService {

    private ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat getOrCreate(User user1, User user2) {
        assert (user1.getId() != user2.getId());

        return chatRepository.findBetweenUsers(user1, user2)
            .orElseGet(() -> {
                var chat = new Chat();
                if (user1.getId() < user2.getId()) {
                    chat.setUser1(user1);
                    chat.setUser2(user2);
                } else {
                    chat.setUser1(user2);
                    chat.setUser2(user1);
                }
                return chatRepository.save(chat);
            }
        );
    }

    public Optional<Chat> get(Long chatId) {
        return chatRepository.findById(chatId);
    }

}
