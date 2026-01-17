package com.example.keeplearning.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.keeplearning.dto.ChatMessage;
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
	private SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService, MessageRepository messageRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
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

    @GetMapping("/list")
    public String showChats(@AuthenticationPrincipal User me, Model model) {

        var chats = chatService.getChatsFor(me);
        model.addAttribute("chats", chats);
        return "chat/list";
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessage message, Principal principal) {

        User sender = userRepository.findByEmail(principal.getName()).orElseThrow();
        Chat chat = chatService.get(message.getChatId()).orElseThrow();

        if (chat.getUser1().getId() != sender.getId() && chat.getUser2().getId() != sender.getId()) {
            return;
        }

        var msg = Message.create(chat, sender, message.getContent());
        messageRepository.save(msg);

        messagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), msg);
    }

}
