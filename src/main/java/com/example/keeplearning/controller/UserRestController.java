package com.example.keeplearning.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.keeplearning.dto.UserInfo;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.UserService;


@RestController
@RequestMapping("/api/users")
public class UserRestController {
    UserService userService;
    UserRepository userRepository;

    public UserRestController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping()
    public User createUser(@RequestBody UserInfo userInfo) {
        return userService.createUser(userInfo).orElse(null);
    }

    @GetMapping()
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
    
    @GetMapping("/{email}")
    public ResponseEntity<User> getUser(@PathVariable("email") String email) {
        return ResponseEntity.of(userService.getUserByEmail(email));
    }

    @DeleteMapping("{userId}")
    public void deleteUser(@PathVariable("id") Long userId) {
        userRepository.deleteById(userId);
    }
    
}
