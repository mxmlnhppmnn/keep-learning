package com.example.keeplearning.repository.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.chat.Chat;

// 
// ChatRepository nie direct benutzen sonder immer durch ChatService
//
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByUser1AndUser2(User user1, User user2);

    @Query("""
                SELECT c FROM Chat c
                WHERE (c.user1 = :user1 AND c.user2 = :user2)
                   OR (c.user1 = :user2 AND c.user2 = :user1)
            """)
    Optional<Chat> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("""
                SELECT c FROM Chat c
                WHERE c.user1 = :user or c.user2 = :user
            """)
    List<Chat> findAllByUser(@Param("user") User user);

}
