package com.example.keeplearning.dto;

import java.time.LocalDateTime;

public class ChatListItem {
    
    private Long chatId;
    private String username;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    public ChatListItem(Long chatId, String username, String lastMessage, LocalDateTime lastMessageTime) {
        this.chatId = chatId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
    
    public Long getChatId() {
        return chatId;
    }
    public String getUsername() {
        return username;
    }
    public String getLastMessage() {
        return lastMessage;
    }
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

}
