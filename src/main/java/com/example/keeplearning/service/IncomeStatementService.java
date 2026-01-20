package com.example.keeplearning.service;

import com.example.keeplearning.entity.IncomeStatement;
import com.example.keeplearning.entity.Lesson;
import com.example.keeplearning.entity.LessonSeries;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.IncomeStatementRepository;
import com.example.keeplearning.repository.LessonRepository;
import com.example.keeplearning.repository.LessonSeriesRepository;
import com.example.keeplearning.repository.SubjectRepository;
import com.example.keeplearning.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class IncomeStatementService {

    private final IncomeStatementRepository incomeStatementRepository;
    private final LessonSeriesRepository lessonSeriesRepository;
    private final LessonRepository lessonRepository;
    private final AdvertisementRepository advertisementRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public IncomeStatementService(IncomeStatementRepository incomeStatementRepository,
                                 LessonSeriesRepository lessonSeriesRepository,
                                 LessonRepository lessonRepository,
                                 AdvertisementRepository advertisementRepository,
                                 SubjectRepository subjectRepository,
                                 UserRepository userRepository) {
        this.incomeStatementRepository = incomeStatementRepository;
        this.lessonSeriesRepository = lessonSeriesRepository;
        this.lessonRepository = lessonRepository;
        this.advertisementRepository = advertisementRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonSeries> listSeriesForTeacher(Long teacherId) {
        return lessonSeriesRepository.findByTeacherIdOrderByIdDesc(teacherId);
    }

    @Transactional(readOnly = true)
    public List<IncomeStatement> listForTeacher(Long teacherId) {
        return incomeStatementRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

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

    @Transactional
    public IncomeStatement createForTeacher(Long teacherId, Long seriesId) {
        if (incomeStatementRepository.existsByTeacherIdAndSeriesId(teacherId, seriesId)) {
            throw new IllegalStateException("Fuer diesen Kurs existiert bereits eine Einnahmebescheinigung");
        }

        LessonSeries series = lessonSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Kurs nicht gefunden"));

        if (!series.getTeacherId().equals(teacherId)) {
            throw new SecurityException("Keine Berechtigung fuer diesen Kurs");
        }

        // Einnahmen nur fuer bezahlte Kurse
        if (!series.isPaid() && !series.isTrialLesson()) {
            throw new IllegalStateException("Kurs ist noch nicht bezahlt");
        }

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Lehrer nicht gefunden"));
        User student = userRepository.findById(series.getStudentId()).orElse(null);

        String courseTitle = courseTitle(series);
        String subjectName = subjectName(series);

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
        sb.append("Einnahmebescheinigung - keep learning\n");
        sb.append("Erstellt am: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .append("\n");
        sb.append("Lehrer: ").append(teacher.getName()).append(" (").append(teacher.getEmail()).append(")\n");
        if (student != null) {
            sb.append("Schueler: ").append(student.getName()).append(" (").append(student.getEmail()).append(")\n");
        }
        sb.append("Kurs: ").append(courseTitle).append("\n");
        sb.append("Fach: ").append(subjectName).append("\n");
        sb.append("Zahlungsstatus: ").append(series.isTrialLesson() ? "Probestunde" : (series.isPaid() ? "bezahlt" : "unbezahlt"))
                .append("\n\n");

        sb.append("Abgerechnete Leistungen (bis heute):\n");
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
        sb.append("Summe: ").append(String.format(java.util.Locale.GERMANY, "%.2f EUR", total)).append("\n");

        IncomeStatement st = new IncomeStatement();
        st.setTeacherId(teacherId);
        st.setSeriesId(seriesId);
        st.setCreatedAt(LocalDateTime.now());
        st.setTotalAmount(total);
        st.setContent(sb.toString());

        String filename = "einnahme_" + teacherId + "_kurs" + seriesId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt";
        st.setFilename(filename);

        return incomeStatementRepository.save(st);
    }

    @Transactional(readOnly = true)
    public IncomeStatement getForTeacher(Long statementId, Long teacherId) {
        IncomeStatement st = incomeStatementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Einnahmebescheinigung nicht gefunden"));
        if (!st.getTeacherId().equals(teacherId)) {
            throw new SecurityException("Keine Berechtigung fuer diese Einnahmebescheinigung");
        }
        return st;
    }
}
