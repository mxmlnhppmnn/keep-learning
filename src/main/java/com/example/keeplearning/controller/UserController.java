package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Review;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.verification.VerificationRequest;
import com.example.keeplearning.entity.verification.VerificationRequestStatus;
import com.example.keeplearning.repository.ReviewRepository;
import com.example.keeplearning.repository.LessonSeriesRepository;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.SubjectRepository;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.repository.VerificationRequestRepository;
import com.example.keeplearning.service.UserService;
import com.example.keeplearning.service.admin.VerificationService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;
    private final VerificationRequestRepository verificationRequestRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final LessonSeriesRepository lessonSeriesRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SubjectRepository subjectRepository;

    public UserController(UserService userService,  VerificationService verificationService, VerificationRequestRepository verificationRequestRepository, UserRepository userRepository, ReviewRepository reviewRepository,
                          LessonSeriesRepository lessonSeriesRepository,
                          AdvertisementRepository advertisementRepository,
                          SubjectRepository subjectRepository) {
        this.userService = userService;
        this.verificationService = verificationService;
        this.verificationRequestRepository = verificationRequestRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.lessonSeriesRepository = lessonSeriesRepository;
        this.advertisementRepository = advertisementRepository;
        this.subjectRepository = subjectRepository;
    }

    //Das Profil
    @GetMapping("")
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

        // Buchungen im Lehrerprofil anzeigen (damit der Payment-Demo-Flow sichtbar ist)
        if ("TEACHER".equalsIgnoreCase(user.getRole())) {
            var series = lessonSeriesRepository.findByTeacherIdOrderByIdDesc(user.getId());
            Map<Long, String> courseTitles = new HashMap<>();
            Map<Long, String> subjectNames = new HashMap<>();
            Map<Long, String> studentNames = new HashMap<>();

            for (var s : series) {
                String title = (s.getAdvertisementId() == null) ? ("Kurs #" + s.getId())
                        : advertisementRepository.findById(s.getAdvertisementId()).map(a -> a.getTitle()).orElse("Kurs #" + s.getId());
                courseTitles.put(s.getId(), title);

                String subject = (s.getSubjectId() == null) ? "" : subjectRepository.findById(s.getSubjectId()).map(sub -> sub.getName()).orElse("");
                subjectNames.put(s.getId(), subject);

                String studentName = userRepository.findById(s.getStudentId()).map(u -> u.getName()).orElse("Schueler #" + s.getStudentId());
                studentNames.put(s.getId(), studentName);
            }

            model.addAttribute("teacherSeries", series);
            model.addAttribute("teacherCourseTitles", courseTitles);
            model.addAttribute("teacherSubjectNames", subjectNames);
            model.addAttribute("teacherStudentNames", studentNames);
        }
        return "user/detail";
    }

    @GetMapping("/view/{id}")
    public String showUserView(
        @PathVariable Long id,
        @RequestParam(required = false) Integer stars,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        Model model
    ) {
        User user = userRepository.findById(id).orElseThrow();
        var avgRating = reviewRepository.findAverageRatingByUser(user);

        if (avgRating == null) {
            avgRating = 0.0;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> results;
        if (stars == null) {
            results = reviewRepository.findByUser(user, pageable);
        } else {
            results = reviewRepository.findByUserAndRating(user, stars, pageable);
            model.addAttribute("filterStars", stars);
        }
        
        model.addAttribute("user", user);
        model.addAttribute("avgRating", Math.round(avgRating));
        model.addAttribute("reviews", results);

        model.addAttribute("ratings", new int[]{
            reviewRepository.countByUserAndRating(user, 1),
            reviewRepository.countByUserAndRating(user, 2),
            reviewRepository.countByUserAndRating(user, 3),
            reviewRepository.countByUserAndRating(user, 4),
            reviewRepository.countByUserAndRating(user, 5)
        });
        return "user/view";
    }

    @PostMapping("/review/{id}")
    public String rateUser(
        @PathVariable Long id,
        @RequestParam Integer rating,
        @RequestParam String titel,
        @RequestParam String comment,
        @AuthenticationPrincipal User author,
        Model model
    ) {
        User user = userRepository.findById(id).orElseThrow();

        // TODO: debug remove
        model.addAttribute("lines", new String[] {
            "user: " + user.getName(),
            "author: " + author.getName(),
            "Rating: " + rating,
            "Titel: " + titel,
            "Comment: " + comment
        });

        var review = Review.create(user, author, rating, titel, comment);
        reviewRepository.save(review);

        return "utils/show-text";
    }

    @PostMapping("/bearbeiten")
    public String updateUser(@ModelAttribute User changedUser, @AuthenticationPrincipal User user, Model model) {
        if (changedUser.getName() != null)
            user.setName(changedUser.getName());

        String newEmail = changedUser.getEmail();
        if (newEmail != null && newEmail != user.getEmail()) {
            user.setEmail(newEmail);
        }

        userService.saveUser(user);
        return "redirect:/user";
    }

    @GetMapping("/edit-password")
    public String showEditPassword() {
        return "user/edit-password";
    }

    @PostMapping("/change-password")
    public String postMethodName(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal User user,
            Model model) {
        if (!userService.isUserPassword(user.getId(), currentPassword)) {
            model.addAttribute("wrongPassword", "Falsches passwort");
            return "user/edit-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("missmatch", "");
            return "user/edit-password";
        }

        userService.updatePassword(user.getId(), newPassword);
        return "redirect:/user";
    }

    @PostMapping("/verification")
    public String submitVerification(
            @RequestParam("document") MultipartFile document) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

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
