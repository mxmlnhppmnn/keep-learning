package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.entity.Lesson;
import com.example.keeplearning.entity.LessonSeries;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.LessonRepository;
import com.example.keeplearning.repository.LessonSeriesRepository;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.GoogleCalendarService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.keeplearning.service.EmailService;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/buchung")
public class BookingController {


    private final EmailService emailService;
    private final AdvertisementRepository advertisementRepository;
    private final LessonRepository lessonRepository;
    private final LessonSeriesRepository lessonSeriesRepository;
    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;

    public BookingController(AdvertisementRepository advertisementRepository,
                             LessonRepository lessonRepository,
                             LessonSeriesRepository lessonSeriesRepository,
                             GoogleCalendarService googleCalendarService,
                             UserRepository userRepository, EmailService emailService) {

        this.advertisementRepository = advertisementRepository;
        this.lessonRepository = lessonRepository;
        this.lessonSeriesRepository = lessonSeriesRepository;
        this.googleCalendarService = googleCalendarService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @GetMapping("/bestaetigen")
    public String confirmBooking(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson,
            Model model) {

        var anzeige = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Anzeige nicht gefunden"));

        LocalTime end = start.plusMinutes(60);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("date", date);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("probe", trialLesson);

        return "booking/confirm";
    }


    /*@PostMapping("/abschliessen")
    public String completeBooking(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson,
            Authentication authentication) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow();

        User student = (User) authentication.getPrincipal();
        Long studentId = student.getId();

        // Terminserie erzeugen
        LessonSeries serie = new LessonSeries();
        //serie.setLehrerId(advertisement.getUserIdDeprecated());
        serie.setTeacherId(advertisement.getUser().getId());
        serie.setStudentId(studentId);
        serie.setSubjectId(advertisement.getSubject().getId());

        serie.setWeekday(date.getDayOfWeek().getValue());
        serie.setStartTime(start);
        serie.setDuration(60);
        serie.setTrialLesson(trialLesson);

        serie = lessonSeriesRepository.save(serie);

        // Einzelnen Termin erzeugen
        Lesson lesson = new Lesson();
        lesson.setSeriesId(serie.getId());
        lesson.setDate(date);
        lesson.setStartTime(start);
        lesson.setStatus("geplant");

        lessonRepository.save(lesson);

        User lehrer = userRepository.findById(advertisement.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Lehrer nicht gefunden"));
        // Google Calendar Event erstellen
        if (lehrer.getGoogleRefreshToken() != null) {
            try {
                googleCalendarService.createCalendarEvent(
                        lehrer.getGoogleRefreshToken(),
                        date,
                        start,
                        start.plusMinutes(60),
                        "Nachhilfestunde",
                        "Terminserie #" + serie.getId()
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Google Calendar konnte nicht aktualisiert werden.");
            }
        }

        User teacher = advertisement.getUser();
        //User student = userRepository.findById(studentId).orElseThrow();

        sendBookingConfirmationMails(teacher, student, advertisement, date, start);

        return "booking/success";
    }*/

    @PostMapping("/abschliessen")
    public String completeBooking(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson,
            Authentication authentication
    ) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow();

        // schüler ist eingeloggt beim Buchen
        User student = (User) authentication.getPrincipal();
        Long studentId = student.getId();

        User teacher = advertisement.getUser();
        Long teacherId = teacher.getId();

        // Serie anlegen (auch bei Probestunde)
        LessonSeries serie = new LessonSeries();
        serie.setTeacherId(teacherId);
        serie.setStudentId(studentId);
        serie.setSubjectId(advertisement.getSubject().getId());
        serie.setWeekday(date.getDayOfWeek().getValue());
        serie.setStartTime(start);
        serie.setDuration(60);
        serie.setTrialLesson(trialLesson);

        serie = lessonSeriesRepository.save(serie);

        // einzelner Termin in der Serie
        Lesson lesson = new Lesson();
        lesson.setSeriesId(serie.getId());
        lesson.setDate(date);
        lesson.setStartTime(start);
        lesson.setStatus("geplant");

        lessonRepository.save(lesson);

        //google calendar des Lehrers
        if (teacher.getGoogleRefreshToken() != null) {
            try {
                googleCalendarService.createCalendarEvent(teacher.getGoogleRefreshToken(), date, start,
                        start.plusMinutes(60), "Nachhilfestunde",
                        "Terminserie #" + serie.getId()
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Google Calendar konnte nicht aktualisiert werden.");
            }
        }

        //bestätigungsmails
        emailService.sendBookingConfirmationToStudent(
                student.getEmail(),
                teacher.getName(),                 // oder getFullName()
                advertisement.getSubject().getName(),
                date,
                start,
                60
        );

        return "booking/success";
    }

}
