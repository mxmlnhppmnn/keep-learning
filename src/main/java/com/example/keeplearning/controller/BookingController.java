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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/buchung")
public class BookingController {

    private final AdvertisementRepository advertisementRepository;
    private final LessonRepository lessonRepository;
    private final LessonSeriesRepository lessonSeriesRepository;
    private final GoogleCalendarService googleCalendarService;
    private final UserRepository userRepository;

    public BookingController(AdvertisementRepository advertisementRepository,
                             LessonRepository lessonRepository,
                             LessonSeriesRepository lessonSeriesRepository,
                             GoogleCalendarService googleCalendarService,
                             UserRepository userRepository) {

        this.advertisementRepository = advertisementRepository;
        this.lessonRepository = lessonRepository;
        this.lessonSeriesRepository = lessonSeriesRepository;
        this.googleCalendarService = googleCalendarService;
        this.userRepository = userRepository;
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


    @PostMapping("/abschliessen")
    public String completeBooking(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow();

        // TODO: ersetzen, sobald Login da ist
        Long studentId = 1L;

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

        // Lehrer (User) laden
        /*User lehrer = userRepository.findById(advertisement.getUserIdDeprecated())
                .orElseThrow(() -> new RuntimeException("Lehrer nicht gefunden"));*/
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

        return "booking/success";
    }
}
