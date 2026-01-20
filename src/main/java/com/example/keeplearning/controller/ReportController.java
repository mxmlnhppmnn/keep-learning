package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Report;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.ReportRepository;
import com.example.keeplearning.repository.chat.MessageRepository;
import com.example.keeplearning.repository.StudyGroupMessageRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/meldungen")
public class ReportController {

    private final ReportRepository reportRepository;
    private final MessageRepository messageRepository;
    private final StudyGroupMessageRepository studyGroupMessageRepository;

    public ReportController(ReportRepository reportRepository,
                            MessageRepository messageRepository,
                            StudyGroupMessageRepository studyGroupMessageRepository) {
        this.reportRepository = reportRepository;
        this.messageRepository = messageRepository;
        this.studyGroupMessageRepository = studyGroupMessageRepository;
    }

    @PostMapping("/anzeige/{adId}")
    public String reportAd(
            @PathVariable Long adId,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false, defaultValue = "/anzeigen/{id}") String redirect,
            @AuthenticationPrincipal User user
    ) {
        Report r = new Report();
        r.setType(Report.ReportType.ADVERTISEMENT);
        r.setAdvertisementId(adId);
        r.setReporter(user);
        r.setReason(reason);
        reportRepository.save(r);

        // Redirect zur Anzeige (Standard)
        return "redirect:/anzeigen/" + adId + "?reported=1";
    }

    @PostMapping("/nachricht/{messageId}")
    public String reportMessage(
            @PathVariable Long messageId,
            @RequestParam Long chatId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user
    ) {
        // existenz check
        messageRepository.findById(messageId).orElseThrow();

        Report r = new Report();
        r.setType(Report.ReportType.MESSAGE);
        r.setMessageId(messageId);
        r.setReporter(user);
        r.setReason(reason);
        reportRepository.save(r);

        return "redirect:/chat/" + chatId + "?reported=1";
    }

    @PostMapping("/gruppennachricht/{messageId}")
    public String reportStudyGroupMessage(
            @PathVariable Long messageId,
            @RequestParam Long groupId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User user
    ) {
        // Existenz check
        studyGroupMessageRepository.findById(messageId).orElseThrow();

        Report r = new Report();
        r.setType(Report.ReportType.STUDY_GROUP_MESSAGE);
        r.setStudyGroupMessageId(messageId);
        r.setReporter(user);
        r.setReason(reason);
        reportRepository.save(r);

        return "redirect:/gruppen/" + groupId + "?reported=1#chat";
    }
}
