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

        // Default Zahlungsmethode (nur wenn keine Probestunde)
        model.addAttribute("paymentMethod", "PAYPAL");

        return "booking/confirm";
    }

    /**
     * Startet eine Buchung direkt aus der Timeslot-Liste.
     * Fuer normale Stunden wird anschliessend in den Payment-Schritt weitergeleitet.
     */
    @PostMapping("/start")
    public String startBookingFromTimeslots(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson,
            Authentication authentication
    ) {
        // Delegiert an die bestehende Logik, ohne vorherige Confirm-Seite.
        return completeBooking(advertisementId, date, start, trialLesson, null, authentication);
    }

    @PostMapping("/abschliessen")
    public String completeBooking(
            @RequestParam Long advertisementId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean trialLesson,
            @RequestParam(required = false) String paymentMethod,
            Authentication authentication
    ) {

        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow();

        // Anzeige kann nur einmal gebucht werden.
        if (advertisement.isBooked()) {
            return "redirect:/anzeigen/" + advertisementId + "?alreadyBooked=1";
        }

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
        serie.setAdvertisementId(advertisement.getId());
        serie.setWeekday(date.getDayOfWeek().getValue());
        serie.setStartTime(start);
        serie.setDuration(60);
        serie.setTrialLesson(trialLesson);
        // Preis fuer spaetere Rechnungen fixieren
        serie.setPricePerHour(advertisement.getPrice());

        // Zahlungsabwicklung: Serie wird angelegt, aber erst im naechsten Schritt bezahlt.
        if (!trialLesson) {
            String pm = (paymentMethod == null || paymentMethod.isBlank()) ? "PAYPAL" : paymentMethod.toUpperCase();
            if (!pm.equals("PAYPAL") && !pm.equals("SEPA")) {
                pm = "PAYPAL";
            }
            serie.setPaymentMethod(pm);
            serie.setPaid(false);
        }

        serie = lessonSeriesRepository.save(serie);

        // einzelner Termin in der Serie
        Lesson lesson = new Lesson();
        lesson.setSeriesId(serie.getId());
        lesson.setDate(date);
        lesson.setStartTime(start);
        lesson.setStatus("geplant");

        lessonRepository.save(lesson);

        // Anzeige als gebucht markieren, damit sie fuer andere Schueler nicht mehr sichtbar/buchbar ist.
        advertisement.setBooked(true);
        advertisement.setBookedStudentId(studentId);
        advertisement.setBookedSeriesId(serie.getId());
        advertisementRepository.save(advertisement);

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

        //bestätigungsmail an den schüler
        emailService.sendBookingConfirmationToStudent(
                student.getEmail(),
                teacher.getName(),
                advertisement.getSubject().getName(),
                date,
                start,
                60
        );

        // Bei Probestunde direkt fertig, sonst zur Zahlung weiterleiten.
        if (trialLesson) {
            return "redirect:/buchung/erfolg?seriesId=" + serie.getId();
        }

        return "redirect:/buchung/bezahlen?seriesId=" + serie.getId();
    }

    @GetMapping("/bezahlen")
    public String paymentForm(@RequestParam Long seriesId, Model model, Authentication authentication) {
        LessonSeries serie = lessonSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie nicht gefunden"));

        User student = (User) authentication.getPrincipal();
        if (!serie.getStudentId().equals(student.getId())) {
            return "redirect:/home";
        }

        if (serie.isTrialLesson() || serie.isPaid()) {
            return "redirect:/buchung/erfolg?seriesId=" + seriesId;
        }

        Advertisement ad = null;
        if (serie.getAdvertisementId() != null) {
            ad = advertisementRepository.findById(serie.getAdvertisementId()).orElse(null);
        }

        model.addAttribute("serie", serie);
        model.addAttribute("anzeige", ad);
        model.addAttribute("paymentMethod", (serie.getPaymentMethod() == null) ? "PAYPAL" : serie.getPaymentMethod());
        return "booking/pay";
    }

    @PostMapping("/bezahlen")
    public String pay(
            @RequestParam Long seriesId,
            @RequestParam String paymentMethod,
            Authentication authentication
    ) {
        LessonSeries serie = lessonSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie nicht gefunden"));

        User student = (User) authentication.getPrincipal();
        if (!serie.getStudentId().equals(student.getId())) {
            return "redirect:/home";
        }

        if (serie.isTrialLesson()) {
            return "redirect:/buchung/erfolg?seriesId=" + seriesId;
        }

        String pm = (paymentMethod == null || paymentMethod.isBlank()) ? "PAYPAL" : paymentMethod.toUpperCase();
        if (!pm.equals("PAYPAL") && !pm.equals("SEPA")) {
            pm = "PAYPAL";
        }

        serie.setPaymentMethod(pm);
        serie.setPaid(true);
        lessonSeriesRepository.save(serie);

        // Zahlungsbestaetigungsmail (Feature-Nachweis)
        Double amount = (serie.getPricePerHour() == null) ? 0.0 : serie.getPricePerHour();
        emailService.sendPaymentConfirmationToStudent(student.getEmail(), pm, amount);

        return "redirect:/buchung/erfolg?seriesId=" + seriesId;
    }

    @GetMapping("/erfolg")
    public String bookingSuccess(@RequestParam Long seriesId, Model model, Authentication authentication) {
        LessonSeries serie = lessonSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new IllegalArgumentException("Serie nicht gefunden"));

        User student = (User) authentication.getPrincipal();
        if (!serie.getStudentId().equals(student.getId())) {
            return "redirect:/home";
        }

        model.addAttribute("serie", serie);
        return "booking/success";
    }

}
