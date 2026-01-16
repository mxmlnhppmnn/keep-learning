package com.example.keeplearning.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.keeplearning.dto.ChatListItem;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.chat.Chat;
import com.example.keeplearning.entity.chat.Message;
import com.example.keeplearning.repository.chat.ChatRepository;
import com.example.keeplearning.repository.chat.MessageRepository;

@Service
public class ChatService {

    private ChatRepository chatRepository;
    private MessageRepository messageRepository;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
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

    public List<ChatListItem> getChatsFor(User user) {

        List<Chat> chats = chatRepository.findAllByUser(user);

        return chats.stream().map((Chat chat) -> {

            User other = chat.getUser1().equals(user) ? chat.getUser2() : chat.getUser1();
            Optional<Message> lastMsg = messageRepository.findFirstByChatOrderBySentAtDesc(chat);

            ChatListItem cli = new ChatListItem(
                chat.getId(),
                other.getName(),
                lastMsg.map(Message::getContent).orElse(""),
                lastMsg.map(Message::getSentAt).orElse(null)
            );

            return cli;
        }).toList();
    }

}
