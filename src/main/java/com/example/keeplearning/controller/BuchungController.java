package com.example.keeplearning.controller;

import com.example.keeplearning.repository.AnzeigeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/buchung")
public class BuchungController {

    private final AnzeigeRepository anzeigeRepository;

    public BuchungController(AnzeigeRepository anzeigeRepository) {
        this.anzeigeRepository = anzeigeRepository;
    }

    @GetMapping("/bestaetigen")
    public String bestaetigen(
            @RequestParam Long anzeigeId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime start,
            Model model) {

        var anzeige = anzeigeRepository.findById(anzeigeId)
                .orElseThrow(() -> new IllegalArgumentException("Anzeige nicht gefunden"));

        LocalTime end = start.plusMinutes(60);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("date", date);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        return "buchung/bestaetigen";
    }
}