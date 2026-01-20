package com.example.keeplearning.controller.admin;

import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.entity.Report;
import com.example.keeplearning.entity.StudyGroupMessage;
import com.example.keeplearning.entity.chat.Message;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.ReportRepository;
import com.example.keeplearning.repository.StudyGroupMessageRepository;
import com.example.keeplearning.repository.chat.MessageRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportRepository reportRepository;
    private final AdvertisementRepository advertisementRepository;
    private final MessageRepository messageRepository;
    private final StudyGroupMessageRepository studyGroupMessageRepository;

    public AdminReportController(ReportRepository reportRepository,
                                 AdvertisementRepository advertisementRepository,
                                 MessageRepository messageRepository,
                                 StudyGroupMessageRepository studyGroupMessageRepository) {
        this.reportRepository = reportRepository;
        this.advertisementRepository = advertisementRepository;
        this.messageRepository = messageRepository;
        this.studyGroupMessageRepository = studyGroupMessageRepository;
    }

    @GetMapping
    public String listReports(Model model) {
        List<Report> reports = reportRepository.findAll();
        reports.sort(Comparator.comparing(Report::getCreatedAt).reversed());

        // Zusatzinfos fuer die UI (Titel/Preview)
        Map<Long, String> adTitles = new HashMap<>();
        Map<Long, String> msgPreviews = new HashMap<>();
        Map<Long, String> groupMsgPreviews = new HashMap<>();

        for (Report r : reports) {
            if (r.getType() == Report.ReportType.ADVERTISEMENT && r.getAdvertisementId() != null) {
                Advertisement ad = advertisementRepository.findById(r.getAdvertisementId()).orElse(null);
                if (ad != null) {
                    adTitles.put(r.getId(), ad.getTitle());
                }
            }
            if (r.getType() == Report.ReportType.MESSAGE && r.getMessageId() != null) {
                Message msg = messageRepository.findById(r.getMessageId()).orElse(null);
                if (msg != null) {
                    String content = msg.getContent();
                    if (content != null && content.length() > 60) {
                        content = content.substring(0, 60) + "…";
                    }
                    msgPreviews.put(r.getId(), content);
                }
            }

            if (r.getType() == Report.ReportType.STUDY_GROUP_MESSAGE && r.getStudyGroupMessageId() != null) {
                StudyGroupMessage msg = studyGroupMessageRepository.findById(r.getStudyGroupMessageId()).orElse(null);
                if (msg != null) {
                    String content = msg.getContent();
                    if (content != null && content.length() > 60) {
                        content = content.substring(0, 60) + "…";
                    }
                    groupMsgPreviews.put(r.getId(), content);
                }
            }
        }

        model.addAttribute("reports", reports);
        model.addAttribute("adTitles", adTitles);
        model.addAttribute("msgPreviews", msgPreviews);
        model.addAttribute("groupMsgPreviews", groupMsgPreviews);
        return "admin/reports";
    }

    @PostMapping("/{id}/delete")
    public String deleteReport(@PathVariable Long id) {
        reportRepository.deleteById(id);
        return "redirect:/admin/reports?deleted=1";
    }

    @PostMapping("/{id}/remove-message")
    public String removeReportedMessage(@PathVariable Long id) {
        Report r = reportRepository.findById(id).orElse(null);
        if (r == null) {
            return "redirect:/admin/reports";
        }

        if (r.getType() == Report.ReportType.MESSAGE && r.getMessageId() != null) {
            messageRepository.findById(r.getMessageId()).ifPresent(messageRepository::delete);
        }

        if (r.getType() == Report.ReportType.STUDY_GROUP_MESSAGE && r.getStudyGroupMessageId() != null) {
            studyGroupMessageRepository.findById(r.getStudyGroupMessageId()).ifPresent(studyGroupMessageRepository::delete);
        }

        // Report gleich mit entfernen (moderiert)
        reportRepository.deleteById(id);
        return "redirect:/admin/reports?removed=1";
    }
}
