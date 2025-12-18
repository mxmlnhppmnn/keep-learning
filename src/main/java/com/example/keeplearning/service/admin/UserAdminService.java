package com.example.keeplearning.service.admin;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.UserStatus;
import com.example.keeplearning.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserAdminService {

    @Autowired
    private UserRepository userRepository;

    //User sperren
    public void lockUser(Long actorId, Long targetUserId,
                         LocalDateTime until, String reason) {
        preventSelfAction(actorId, targetUserId);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.LOCKED);
        user.setLockedUntil(until);
        //um leere Strings wie "" oder " " in der DB zu vermeiden
        user.setLockReason((reason == null || reason.isBlank()) ? null : reason);

        userRepository.save(user);
    }

    //User entsperren
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.ACTIVE);
        user.setLockedUntil(null);
        user.setLockReason(null);

        userRepository.save(user);
    }

    //User soft löschen
    public void deleteUser(Long actorId, Long targetUserId) {
        preventSelfAction(actorId, targetUserId);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User nicht gefunden"));

        user.setStatus(UserStatus.DELETED); //nicht wirklich aus DB entfernen, nur Statusänderung
        user.setLockedUntil(null);

        userRepository.save(user);
    }

    //Admins sollen sich nicht selbst sperren können
    private void preventSelfAction(Long actorId, Long targetUserId) {
        if (actorId.equals(targetUserId)) {
            throw new IllegalStateException(
                    "Admins dürfen sich nicht selbst sperren oder löschen."
            );
        }
    }



}
