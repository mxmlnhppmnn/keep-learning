package com.example.keeplearning.service.admin;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.verification.VerificationRequest;
import com.example.keeplearning.entity.verification.VerificationRequestStatus;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.repository.VerificationRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional //sorgt dafür, dass Status und User-Update gemeinsam passieren
public class VerificationService {

    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.verification-path}")
    private String verificationUploadPath;

    public VerificationService(VerificationRequestRepository verificationRequestRepository,
                               UserRepository userRepository) {
        this.verificationRequestRepository = verificationRequestRepository;
        this.userRepository = userRepository;
    }

    //Antrag darf nur gestellt werden, wenn der Status nicht pending ist
    public void submitRequest(User user, MultipartFile document) {

        if (user.isVerified()) {
            throw new IllegalStateException("User already verified");
        }

        boolean hasPending =
                verificationRequestRepository.existsByUserAndStatus(
                        user, VerificationRequestStatus.PENDING);

        if (hasPending) {
            throw new IllegalStateException("Pending request already exists");
        }

        if (document == null || document.isEmpty()) {
            throw new IllegalArgumentException("Document is required");
        }

        String filePath = storeVerificationFile(user.getId(), document);

        VerificationRequest request =
                new VerificationRequest(user, filePath);

        verificationRequestRepository.save(request);
    }

    //genehmigen
    public void approveRequest(Long requestId, User admin) {

        assertAdmin(admin);

        VerificationRequest request = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Verification request not found"));

        if (request.getStatus() != VerificationRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }

        request.setStatus(VerificationRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(admin);

        User user = request.getUser();
        user.setVerified(true);

        userRepository.save(user);
        verificationRequestRepository.save(request);
    }

    //ablehnen mit Grund
    public void rejectRequest(Long requestId, User admin, String reason) {

        assertAdmin(admin);

        VerificationRequest request = verificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Verification request not found"));

        if (request.getStatus() != VerificationRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }

        request.setStatus(VerificationRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(admin);
        request.setRejectionReason(reason);

        verificationRequestRepository.save(request);
    }



    private void assertAdmin(User user) {
        System.out.println("ROLE FROM USER OBJECT = [" + user.getRole() + "]");
        if (!"admin".equals(user.getRole())) {
            throw new SecurityException("Only admins can perform this action");
        }
    }

    //damit das in dem /uploads/verification landet
    private String storeVerificationFile(Long userId, MultipartFile file) {
        try {
            Path userDir = Paths.get(verificationUploadPath, userId.toString());
            Files.createDirectories(userDir);

            String filename = UUID.randomUUID() + "_" +
                    StringUtils.cleanPath(file.getOriginalFilename());

            Path targetPath = userDir.resolve(filename);

            Files.copy(file.getInputStream(), targetPath);

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store verification file", e);
        }
    }


}
