package com.example.keeplearning.repository;

import com.example.keeplearning.entity.verification.VerificationRequest;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.verification.VerificationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationRequestRepository
        extends JpaRepository<VerificationRequest, Long> {

    //schauen, ob der user einen offenen Antrag hat
    boolean existsByUserAndStatus(User user, VerificationRequestStatus status);

    //offenen Antrag holen
    Optional<VerificationRequest> findByUserAndStatus(
            User user,
            VerificationRequestStatus status
    );

    //holt den letzten bewerteten Antrag
    Optional<VerificationRequest>
    findFirstByUserAndStatusOrderByReviewedAtDesc(
            User user,
            VerificationRequestStatus status
    );


    //alle offenen Anträge
    List<VerificationRequest> findAllByStatus(
            VerificationRequestStatus status
    );
}
