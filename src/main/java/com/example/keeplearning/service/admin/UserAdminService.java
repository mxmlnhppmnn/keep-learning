package com.example.keeplearning.service.admin;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.UserStatus;
import com.example.keeplearning.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserAdminService {

    @Autowired
    private UserRepository userRepository;

    public void lockUser(Long userId, LocalDateTime until, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(until);
        user.setLockReason(reason);

        userRepository.save(user);
    }

    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setLockReason(null);

        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.DELETED);
        user.setLockedUntil(null);

        userRepository.save(user);
    }
}
