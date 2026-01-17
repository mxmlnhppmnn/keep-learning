package com.example.keeplearning.dto;

public class ChatMessage {
    private Long chatId;
    private String content;
    
    public Long getChatId() {
        return chatId;
    }
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
