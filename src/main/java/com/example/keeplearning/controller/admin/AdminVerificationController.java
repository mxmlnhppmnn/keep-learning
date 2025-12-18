package com.example.keeplearning.controller.admin;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.verification.VerificationRequest;
import com.example.keeplearning.entity.verification.VerificationRequestStatus;
import com.example.keeplearning.repository.VerificationRequestRepository;
import com.example.keeplearning.service.admin.VerificationService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Controller
@RequestMapping("/admin/verifications")
public class AdminVerificationController {

    private final VerificationService verificationService;
    private final VerificationRequestRepository verificationRequestRepository;

    public AdminVerificationController(
            VerificationService verificationService,
            VerificationRequestRepository verificationRequestRepository) {
        this.verificationService = verificationService;
        this.verificationRequestRepository = verificationRequestRepository;
    }


    //Alle Pending (noch nicht bestätigt oder abgelehnten) Anträge
    @GetMapping
    public String listPendingVerifications(Model model) {

        List<VerificationRequest> pendingRequests =
                verificationRequestRepository.findAllByStatus(
                        VerificationRequestStatus.PENDING
                );

        model.addAttribute("requests", pendingRequests);
        return "admin/verifications";
    }


    //genehmigen
    @PostMapping("/{id}/approve")
    public String approveVerification(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {

        verificationService.approveRequest(id, admin);
        return "redirect:/admin/verifications";
    }

   //ablehnen
    @PostMapping("/{id}/reject")
    public String rejectVerification(
            @PathVariable Long id,
            @RequestParam("reason") String reason,
            @AuthenticationPrincipal User admin) {

        verificationService.rejectRequest(id, admin, reason);
        return "redirect:/admin/verifications";
    }

    //das beigefügte Dokument herunterladen
    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> downloadVerificationDocument(
            @PathVariable Long id) {

        // Antrag laden
        VerificationRequest request = verificationRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // darüber Pfad aus DB holen (dort nur Pfad! Keine Dokumente selbst!)
        Path path = Paths.get(request.getDocumentPath());

        if (!Files.exists(path)) {
            throw new IllegalStateException("File does not exist");
        }

        // neue Ressource (für das dokument)
        Resource resource;
        try {
            resource = new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file path", e);
        }

        // als download returnen
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + path.getFileName().toString() + "\"")
                .body(resource);
    }

}
