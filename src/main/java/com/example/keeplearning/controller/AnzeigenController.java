package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Schulart;
import com.example.keeplearning.repository.AnzeigeRepository;
import com.example.keeplearning.entity.Anzeige;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.repository.FachRepository;
import com.example.keeplearning.entity.Fach;
import com.example.keeplearning.service.TimeslotService;
import com.example.keeplearning.repository.SchulartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/anzeigen")
public class AnzeigenController {

    @Autowired
    private AnzeigeRepository anzeigeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FachRepository fachRepository;

    @Autowired
    private SchulartRepository schulartRepository;

    @Autowired
    private TimeslotService timeslotService;

    @GetMapping
    public String anzeigenListe(Model model) {
        model.addAttribute("anzeigen", anzeigeRepository.findAll());
        return "anzeigen/anzeigen_liste";
    }

    @GetMapping("/neu")
    public String anzeigenNeuForm(Model model) {
        model.addAttribute("anzeige", new Anzeige());
        List<Schulart> alleSchularten = schulartRepository.findAll(); // Schularten fürs Dropdown
        model.addAttribute("schularten", alleSchularten);
        return "anzeigen/anzeige_erstellen";
    }

    @GetMapping("/{id}")
    public String anzeigenDetails(@PathVariable Long id, Model model, Principal principal) {

        Anzeige anzeige = anzeigeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anzeige wurde nicht gefunden"));

        // aktuellen Benutzer bestimmen (Fake-Login solange kein echtes Login da ist)
        String email;
        if (principal != null) {
            email = principal.getName();
        } else {
            email = "test@test.de";
        }

        User user = userRepository.findByEmail(email).orElse(null);

        boolean istErsteller = user != null && user.getId().equals(anzeige.getUserId());

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("istErsteller", istErsteller);

        return "anzeigen/anzeige_details";
    }

    @GetMapping("/bearbeiten/{id}")
    public String bearbeitenForm(@PathVariable Long id, Model model, Principal principal){
        Anzeige anzeige = anzeigeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Anzeige wurde nicht gefunden"));

        // Nutzer ermitteln (mit Fake-Login fallback)
        User user;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
        } else {
            user = userRepository.findByEmail("test@test.de").orElse(null);
            if (user == null) {
                throw new RuntimeException("Mock-Benutzer 'test@test.de' existiert nicht in der Datenbank.");
            }
        }

        if (!anzeige.getUserId().equals(user.getId())){
            return "redirect:/anzeigen";
        }

        model.addAttribute("anzeige", anzeige);
        return "anzeigen/anzeige_bearbeiten";
    }

    @GetMapping("/suche")
    public String sucheAnzeigen(@RequestParam("q") String query, Model model){
        // Bei leerer Suche alle Anzeigen anzeigen
        if (query == null || query.trim().isEmpty()){
            model.addAttribute("anzeigen", anzeigeRepository.findAll());
            return "anzeigen/anzeigen_liste";
        }

        List<Anzeige> ergebnisse = anzeigeRepository.searchAll(query);

        model.addAttribute("anzeigen", ergebnisse);
        model.addAttribute("suchbegriff", query);

        return "anzeigen/anzeigen_liste";
    }

    // ab hier die PostMappings

    @PostMapping("/neu")
    public String erstelleAnzeige(
            @RequestParam("titel") String titel,
            @RequestParam("beschreibung") String beschreibung,
            @RequestParam("fach") String fachName,
            @RequestParam("schulartId") long schulartId,
            @RequestParam("preis") Double preis,
            @RequestParam(value = "bild", required = false) MultipartFile bild
    ) {

        // Fach suchen/neu anlegen
        Fach fach = fachRepository.findByNameIgnoreCase(fachName)
                .orElseGet(() -> {
                    Fach neuesFach = new Fach();
                    neuesFach.setName(fachName);
                    return fachRepository.save(neuesFach);
                });

        Anzeige anzeige = new Anzeige();
        anzeige.setTitel(titel);
        anzeige.setBeschreibung(beschreibung);
        anzeige.setPreis(preis);
        anzeige.setFach(fach);
        Schulart s = schulartRepository.findById(schulartId).orElseThrow();
        anzeige.setSchulart(s);

        anzeige.setUserId(1L); // TODO: ändern, sobald Login-Feature da ist

        // Bild upload optional
        if (bild != null && !bild.isEmpty()) {

            // Größenlimit
            if (bild.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Bild ist zu groß (max. 5 MB erlaubt)");
            }
            try {
                Path uploadDir = Paths.get("src/main/resources/static/images");
                Files.createDirectories(uploadDir); // erstellt Ordner falls er nicht existiert

                Path ziel = uploadDir.resolve(bild.getOriginalFilename());
                Files.copy(bild.getInputStream(), ziel, StandardCopyOption.REPLACE_EXISTING);

                anzeige.setBildpfad(bild.getOriginalFilename());

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Fehler beim Speichern des Bildes");
            }

        } else {
            // Kein Bild
            anzeige.setBildpfad(null);
        }

        anzeigeRepository.save(anzeige);

        return "redirect:/anzeigen";
    }

    @PostMapping("/bearbeiten/{id}")
    public String bearbeitenSpeichern(
            @PathVariable Long id,
            @RequestParam String titel,
            @RequestParam String beschreibung,
            @RequestParam Double preis,
            @RequestParam(value = "bild", required = false) MultipartFile bild

    ){
        Anzeige anzeige = anzeigeRepository.findById(id).orElseThrow();
        anzeige.setTitel(titel);
        anzeige.setBeschreibung(beschreibung);
        anzeige.setPreis(preis);

        if (bild != null && !bild.isEmpty()) {

            String dateiname = bild.getOriginalFilename();
            Path uploadDir = Paths.get("src/main/resources/static/images/");
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Path ziel = uploadDir.resolve(dateiname);
            try {
                Files.copy(bild.getInputStream(), ziel, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            anzeige.setBildpfad(dateiname);
        }

        anzeigeRepository.save(anzeige);
        return "redirect:/anzeigen/" + id;
    }

    @PostMapping("/loeschen/{id}")
    public String loeschen(@PathVariable Long id, Principal principal) {

        Anzeige anzeige = anzeigeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anzeige nicht gefunden"));

        User user;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
        } else {
            user = userRepository.findByEmail("test@test.de").orElse(null);
        }

        if (user == null || !anzeige.getUserId().equals(user.getId())) {
            return "redirect:/anzeigen";
        }

        anzeigeRepository.deleteById(id);

        return "redirect:/anzeigen";
    }

    // Anzeige buchen
    @GetMapping("/{anzeigeId}/buchen")
    public String showBookingPage(@PathVariable Long anzeigeId, Model model) {

        var anzeige = anzeigeRepository.findById(anzeigeId)
                .orElseThrow(() -> new IllegalArgumentException("Anzeige nicht gefunden"));

        Long userId = anzeige.getUserId();

        var slots = timeslotService.generateTimeslotsForUser(userId);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("slots", slots);

        return "buchung/timeslots";
    }

}
