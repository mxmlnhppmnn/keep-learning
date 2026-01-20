package com.example.keeplearning.service;

import com.example.keeplearning.entity.Invoice;
import com.example.keeplearning.entity.Lesson;
import com.example.keeplearning.entity.LessonSeries;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.InvoiceRepository;
import com.example.keeplearning.repository.LessonRepository;
import com.example.keeplearning.repository.LessonSeriesRepository;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.SubjectRepository;
import com.example.keeplearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final LessonRepository lessonRepository;
    private final LessonSeriesRepository lessonSeriesRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          LessonRepository lessonRepository,
                          LessonSeriesRepository lessonSeriesRepository,
                          SubjectRepository subjectRepository,
                          UserRepository userRepository,
                          AdvertisementRepository advertisementRepository) {
        this.invoiceRepository = invoiceRepository;
        this.lessonRepository = lessonRepository;
        this.lessonSeriesRepository = lessonSeriesRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonSeries> listSeriesForStudent(Long studentId) {
        return lessonSeriesRepository.findByStudentIdOrderByIdDesc(studentId);
    }

    // Helper fuer UI
    @Transactional(readOnly = true)
    public String courseTitle(LessonSeries series) {
        if (series == null) return "";
        Long adId = series.getAdvertisementId();
        if (adId == null) {
            return "Kurs #" + series.getId();
        }
        return advertisementRepository.findById(adId)
                .map(a -> a.getTitle())
                .orElse("Kurs #" + series.getId());
    }

    @Transactional(readOnly = true)
    public String subjectName(LessonSeries series) {
        if (series == null) return "";
        Long subjectId = series.getSubjectId();
        if (subjectId == null) return "";
        return subjectRepository.findById(subjectId)
                .map(s -> s.getName())
                .orElse("(Fach unbekannt)");
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoicesForStudent(Long studentId) {
        return invoiceRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public Invoice createInvoiceForStudent(Long studentId, Long seriesId) {
        if (invoiceRepository.existsByStudentIdAndSeriesId(studentId, seriesId)) {
            throw new IllegalStateException("Fuer diesen Kurs existiert bereits eine Rechnung");
        }

        LessonSeries series = lessonSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs nicht gefunden"));

        if (!series.getStudentId().equals(studentId)) {
            throw new SecurityException("Keine Berechtigung fuer diesen Kurs");
        }

        if (!series.isPaid() && !series.isTrialLesson()) {
            throw new IllegalStateException("Kurs ist noch nicht bezahlt");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student nicht gefunden"));
        User teacher = userRepository.findById(series.getTeacherId()).orElse(null);

        String courseTitle = advertisementRepository.findById(series.getAdvertisementId())
                .map(a -> a.getTitle())
                .orElse("Kurs #" + seriesId);

        String subjectName = subjectRepository.findById(series.getSubjectId())
                .map(s -> s.getName())
                .orElse("(Fach unbekannt)");

        LocalDate today = LocalDate.now();
        List<Lesson> lessons = lessonRepository.findBySeriesIdOrderByDateAscStartTimeAsc(seriesId)
                .stream()
                .filter(l -> !l.getDate().isAfter(today))
                .filter(l -> l.getStatus() == null || !"abgesagt".equalsIgnoreCase(l.getStatus()))
                .toList();

        double total = 0.0;
        double pricePerHour = series.getPricePerHour() != null ? series.getPricePerHour() : 0.0;
        double hoursPerLesson = series.getDuration() / 60.0;

        StringBuilder sb = new StringBuilder();
        sb.append("Rechnung - keep learning\n");
        sb.append("Erstellt am: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .append("\n");
        sb.append("Schueler: ").append(student.getName()).append(" (").append(student.getEmail()).append(")\n");
        if (teacher != null) {
            sb.append("Lehrer: ").append(teacher.getName()).append(" (").append(teacher.getEmail()).append(")\n");
        }
        sb.append("Kurs: ").append(courseTitle).append("\n");
        sb.append("Fach: ").append(subjectName).append("\n");
        sb.append("Zahlungsstatus: ").append(series.isTrialLesson() ? "Probestunde" : (series.isPaid() ? "bezahlt" : "unbezahlt"))
                .append("\n\n");

        sb.append("Leistungen (bis heute):\n");
        sb.append("----------------------------------------\n");

        if (lessons.isEmpty()) {
            sb.append("Keine abrechenbaren Stunden gefunden (bis heute).\n");
        } else {
            for (Lesson lesson : lessons) {
                double line = pricePerHour * hoursPerLesson;
                total += line;

                sb.append(lesson.getDate()).append(" ")
                        .append(lesson.getStartTime()).append(" - ")
                        .append(subjectName)
                        .append(" (").append(series.getDuration()).append("min)")
                        .append(" | ")
                        .append(String.format(java.util.Locale.GERMANY, "%.2f EUR", line))
                        .append("\n");
            }
        }

        sb.append("----------------------------------------\n");
        sb.append("Gesamt: ").append(String.format(java.util.Locale.GERMANY, "%.2f EUR", total)).append("\n");

        Invoice invoice = new Invoice();
        invoice.setStudentId(studentId);
        invoice.setTeacherId(series.getTeacherId());
        invoice.setSeriesId(seriesId);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setTotalAmount(total);
        invoice.setContent(sb.toString());

        String filename = "rechnung_" + studentId + "_kurs" + seriesId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt";
        invoice.setFilename(filename);

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceForStudent(Long invoiceId, Long studentId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Rechnung nicht gefunden"));

        if (!invoice.getStudentId().equals(studentId)) {
            throw new SecurityException("Keine Berechtigung fuer diese Rechnung");
        }

        return invoice;
    }
}
