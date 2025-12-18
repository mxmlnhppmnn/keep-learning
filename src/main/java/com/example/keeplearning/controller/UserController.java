package com.example.keeplearning.controller;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.verification.VerificationRequest;
import com.example.keeplearning.entity.verification.VerificationRequestStatus;
import com.example.keeplearning.repository.VerificationRequestRepository;
import com.example.keeplearning.service.UserService;
import com.example.keeplearning.service.admin.VerificationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Optional;

@Controller
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;
    private final VerificationRequestRepository verificationRequestRepository;


    public UserController(UserService userService,  VerificationService verificationService, VerificationRequestRepository verificationRequestRepository) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.verificationRequestRepository = verificationRequestRepository;
    }

    //Das Profil
    @GetMapping("/user")
    public String showUserInfo(@RequestParam(required = false) String google, Model model, Principal principal) {
        User user = userService.getUser(principal).orElseThrow();

        boolean hasPending =
                verificationRequestRepository.existsByUserAndStatus(
                        user,
                        VerificationRequestStatus.PENDING
                );

        Optional<VerificationRequest> rejectedRequest = Optional.empty();

        if (!hasPending) {
            rejectedRequest = verificationRequestRepository
                            .findFirstByUserAndStatusOrderByReviewedAtDesc(
                                    user, VerificationRequestStatus.REJECTED
                            );
        }


        boolean isGoogleConnected = user.getGoogleRefreshToken() != null;
        model.addAttribute("googleConnected", isGoogleConnected);

        // einmalig nach dem Hinzufügen eines Google Calendars
        if ("ok".equals(google)) {
            model.addAttribute("googleMessage", "Google Calendar erfolgreich verbunden!");
        } else if ("error".equals(google)) {
            model.addAttribute("googleMessage", "Fehler beim Verbinden mit Google Calendar.");
        }

        model.addAttribute("user", user);
        model.addAttribute("hasPending", hasPending);
        model.addAttribute("rejectedRequest", rejectedRequest.orElse(null));
        model.addAttribute("lines", new String[] {
                "Name: " + user.getName(),
                "Email: " + user.getEmail(),
                "Passwort: " + user.getPassword()
        });
        return "utils/show-text";
    }

    @PostMapping("/user/verification")
    public String submitVerification(
            @RequestParam("document") MultipartFile document) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = authentication.getName();

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        verificationService.submitRequest(user, document);

        return "redirect:/user";
    }



}
