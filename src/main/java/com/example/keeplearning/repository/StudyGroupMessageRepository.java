package com.example.keeplearning.repository;

import com.example.keeplearning.entity.StudyGroup;
import com.example.keeplearning.entity.StudyGroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyGroupMessageRepository extends JpaRepository<StudyGroupMessage, Long> {
    List<StudyGroupMessage> findByGroupOrderBySentAtAsc(StudyGroup group);
}
