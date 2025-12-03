package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Schulart;
import com.example.keeplearning.repository.AnzeigeRepository;
import com.example.keeplearning.entity.Anzeige;
import com.example.keeplearning.repository.BenutzerRepository;
import com.example.keeplearning.entity.Benutzer;
import com.example.keeplearning.repository.FachRepository;
import com.example.keeplearning.entity.Fach;

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
    private BenutzerRepository benutzerRepository;
    @Autowired
    private FachRepository fachRepository;
    @Autowired
    private SchulartRepository schulartRepository;

    @GetMapping
    public String anzeigenListe(Model model) {
        model.addAttribute("anzeigen", anzeigeRepository.findAll());
        return "anzeigen/anzeigen_liste";
    }

    @GetMapping("/neu")
    public String anzeigenNeuForm(Model model) {
        model.addAttribute("anzeige", new Anzeige());
        List<Schulart> alleSchularten = schulartRepository.findAll(); //damit die Schularten im Dropdown schon beim 1. Erstellen verfügbar sind
        model.addAttribute("schularten", alleSchularten);
        return "anzeigen/anzeige_erstellen";
    }

    //wenn man auf eine anzeige clickt sieht man auf einer neuen seite alle details und nur die anzeige
    /*
    @GetMapping("/details")
    public String anzeigenDetails() {
        return "anzeige_details";
    }*/

    @GetMapping("/{id}")
    public String anzeigenDetails(@PathVariable Long id, Model model, Principal principal) {

        Anzeige anzeige = anzeigeRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Anzeige wurde nicht gefunden"));

        //aktuellen Benutzer bestimmmen
        //String email = principal.getName(); //Name ist Einlogname -> Email adresse des nutzers
        //nur zum testen solange es kein login gibt! Nacher unbedingt ändern !!
        String email;
        if (principal != null) {
            email = principal.getName();
        } else {
            // Fake-Login für Entwicklungszwecke
            email = "test@test.de";
        }
        Benutzer user = benutzerRepository.findByEmail(email);

        //ende des to be änderung
        //Benutzer user = benutzerRepository.findByEmail(email);


        boolean istErsteller = (user.getUserId().equals(anzeige.getLehrerId()));

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("istErsteller", istErsteller);

        return "anzeigen/anzeige_details";
    }

    @GetMapping("/bearbeiten/{id}")
    public String bearbeitenForm(@PathVariable Long id, Model model, Principal principal){
        Anzeige anzeige = anzeigeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Anzeige wurde nicht gefunden"));

        //Benutzer user = benutzerRepository.findByEmail(principal.getName());
        //nur zum testen ohne login!! Später unbedingt ändern !!
        Benutzer user;

        if (principal != null) {
            // echter Login (später)
            user = benutzerRepository.findByEmail(principal.getName());
        } else {
            // Fake Login: Mock-User aus DB holen
            user = benutzerRepository.findByEmail("test@test.de");

            if (user == null) {
                throw new RuntimeException("Mock-Benutzer 'test@test.de' existiert nicht in der Datenbank.");
            }
        }
        //ende des to be änderung

        if (!anzeige.getLehrerId().equals(user.getUserId())){
            return "redirect:/anzeigen";
        }
        model.addAttribute("anzeige", anzeige);
        return "anzeigen/anzeige_bearbeiten";
    }

    @GetMapping("/suche")
    public String sucheAnzeigen(@RequestParam("q") String query, Model model){
        //Bei Suche nach nichts alle Anzeigen anzeigen
        if(query == null || query.trim().isEmpty()){
            model.addAttribute("anzeigen", anzeigeRepository.findAll());
            return "anzeigen/anzeigen_liste";
        }
        List<Anzeige> ergebnisse = anzeigeRepository.searchAll(query);

        model.addAttribute("anzeigen", ergebnisse);
        model.addAttribute("suchbegriff", query);

        return "anzeigen/anzeigen_liste";
    }

    //ab hier die PostMappings

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


        anzeige.setLehrerId(1L); // ändern sobald login feature da ist

        // Bild upload optional
        if (bild != null && !bild.isEmpty()) {

            //größenlimit
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
            //wohin uploaden
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

        // ändern sobald das login feature da ist!!
        //Benutzer user = benutzerRepository.findByEmail(principal.getName());
        Benutzer user;
        if (principal != null) {
            user = benutzerRepository.findByEmail(principal.getName());
        } else {
            user = benutzerRepository.findByEmail("test@test.de");
        }

        if (!anzeige.getLehrerId().equals(user.getUserId())) {
            return "redirect:/anzeigen";
        }

        anzeigeRepository.deleteById(id);

        return "redirect:/anzeigen";
    }


}
