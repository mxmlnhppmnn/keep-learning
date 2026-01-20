package com.example.keeplearning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.keeplearning.entity.FileShare;
import com.example.keeplearning.entity.User;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    
    List<FileShare> findAllByDestinationOrderBySenderDescSentAtDesc(User destination);
    List<FileShare> findAllBySenderOrderByDestinationDescSentAtDesc(User destination);

    List<FileShare> findAllByDestinationAndSenderOrderBySentAtDesc(User destination, User sender);

}
