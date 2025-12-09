package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Anzeige;
import com.example.keeplearning.entity.Benutzer;
import com.example.keeplearning.entity.Termin;
import com.example.keeplearning.entity.TerminSerie;
import com.example.keeplearning.repository.AnzeigeRepository;
import com.example.keeplearning.repository.BenutzerRepository;
import com.example.keeplearning.repository.TerminRepository;
import com.example.keeplearning.repository.TerminSerieRepository;
import com.example.keeplearning.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/buchung")
public class BuchungController {

    private final AnzeigeRepository anzeigeRepository;
    private final TerminRepository terminRepository;
    private final TerminSerieRepository terminSerieRepository;
    private final GoogleCalendarService googleCalendarService;
    private final BenutzerRepository benutzerRepository;

    public BuchungController(AnzeigeRepository anzeigeRepository,
                             TerminRepository terminRepository,
                             TerminSerieRepository terminSerieRepository,
                             GoogleCalendarService googleCalendarService,
                             BenutzerRepository benutzerRepository) {

        this.anzeigeRepository = anzeigeRepository;
        this.terminRepository = terminRepository;
        this.terminSerieRepository = terminSerieRepository;
        this.googleCalendarService = googleCalendarService;
        this.benutzerRepository = benutzerRepository;
    }

    @GetMapping("/bestaetigen")
    public String bestaetigen(
            @RequestParam Long anzeigeId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean probe,
            Model model) {

        var anzeige = anzeigeRepository.findById(anzeigeId)
                .orElseThrow(() -> new IllegalArgumentException("Anzeige nicht gefunden"));

        LocalTime end = start.plusMinutes(60);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("date", date);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("probe", probe);

        return "buchung/bestaetigen";
    }
    @PostMapping("/abschliessen")
    public String abschliessen(
            @RequestParam Long anzeigeId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            @RequestParam boolean probe) {

        Anzeige anzeige = anzeigeRepository.findById(anzeigeId)
                .orElseThrow();

        //ändern sobald das Login da ist
        Long schuelerId = 1L;

        // Terminserie erzeugen
        TerminSerie serie = new TerminSerie();
        serie.setLehrerId(anzeige.getUserId());
        serie.setSchuelerId(schuelerId);
        serie.setFachId(anzeige.getFach().getFachId());

        serie.setWochentag(date.getDayOfWeek().getValue());
        serie.setStartzeit(start);
        serie.setDauer(60);

        serie.setIstProbestunde(probe);
        serie = terminSerieRepository.save(serie);

        // Einzelnen Termin erzeugen
        Termin termin = new Termin();
        termin.setSerieId(serie.getSerieId());
        termin.setDatum(date);
        termin.setStartzeit(start);
        termin.setStatus("geplant");

        terminRepository.save(termin);

        // Google Calendar Event erstellen
        Benutzer lehrer = benutzerRepository.findById(anzeige.getUserId())
                .orElseThrow();

        if (lehrer.getGoogleRefreshToken() != null) {
            try {
                googleCalendarService.createCalendarEvent(
                        lehrer.getGoogleRefreshToken(),
                        date,
                        start,
                        start.plusMinutes(60),
                        "Nachhilfestunde",
                        "Terminserie #" + serie.getSerieId()
                );
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Google Calendar konnte nicht aktualisiert werden.");
            }
        }
        return "buchung/erfolg";
    }

}