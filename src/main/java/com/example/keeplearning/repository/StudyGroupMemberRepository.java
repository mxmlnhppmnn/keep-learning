package com.example.keeplearning.repository;

import com.example.keeplearning.entity.StudyGroup;
import com.example.keeplearning.entity.StudyGroupMember;
import com.example.keeplearning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

    List<StudyGroupMember> findByUser(User user);

    List<StudyGroupMember> findByGroup(StudyGroup group);

    Optional<StudyGroupMember> findByGroupAndUser(StudyGroup group, User user);
}
