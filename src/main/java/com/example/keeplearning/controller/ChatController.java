package com.example.keeplearning.controller;

import java.security.Principal;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.chat.Chat;
import com.example.keeplearning.entity.chat.Message;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.repository.chat.MessageRepository;
import com.example.keeplearning.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatController {
    
    private ChatService chatService;
    private MessageRepository messageRepository;
    private UserRepository userRepository;

    public ChatController(ChatService chatService, MessageRepository messageRepository, UserRepository userRepository) {
        this.chatService = chatService;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{chatId}")
    public String chat(@AuthenticationPrincipal User me, @PathVariable Long chatId, Model model) {

        // TODO: redirect to chats
        var chat = chatService.get(chatId).orElseThrow();
        var messages = messageRepository.findByChatOrderBySentAtAsc(chat);
        
        model.addAttribute("chat", chat);
        model.addAttribute("messages", messages);
        return "chat/messages";
    }

    @GetMapping("/with/{userId}")
    public String chatWith(@AuthenticationPrincipal User me, @PathVariable Long userId, Model model) {
        
        // TODO: redirect to chats
        var other = userRepository.findById(userId).orElseThrow();
        Chat chat = chatService.getOrCreate(me, other);
        return "redirect:/chat/" + chat.getId();

    }

    @PostMapping("/{chatId}")
    public String send(
        @AuthenticationPrincipal User me,
        @PathVariable Long chatId,
        @RequestParam String content
    ) {
        var chat = chatService.get(chatId).orElseThrow();
        var message = Message.create(chat, me, content);

        messageRepository.save(message);
        return "redirect:/chat/" + chatId;
    }

}
