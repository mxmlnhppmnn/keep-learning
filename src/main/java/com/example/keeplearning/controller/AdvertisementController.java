package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.entity.SchoolType;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.repository.SubjectRepository;
import com.example.keeplearning.entity.Subject;
import com.example.keeplearning.service.TimeslotService;
import com.example.keeplearning.repository.SchoolTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class AdvertisementController {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SchoolTypeRepository schoolTypeRepository;

    @Autowired
    private TimeslotService timeslotService;

    @GetMapping
    public String listAdvertisements(Model model) {
        model.addAttribute("anzeigen", advertisementRepository.findAll());
        return "advertisements/list";
    }

    @GetMapping("/neu")
    public String showCreateForm(Model model) {
        model.addAttribute("anzeige", new Advertisement());
        List<SchoolType> alleSchularten = schoolTypeRepository.findAll(); // Schularten fürs Dropdown
        model.addAttribute("schularten", schoolTypeRepository.findAll());
        return "advertisements/create";
    }

    @GetMapping("/{id}")
    public String showAdvertisementDetails(@PathVariable Long id, Model model, Principal principal) {

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anzeige wurde nicht gefunden"));

        // aktuellen Benutzer bestimmen (Fake-Login solange kein echtes Login da ist)
        String email;
        if (principal != null) {
            email = principal.getName();
        } else {
            email = "test@test.de";
        }

        User user = userRepository.findByEmail(email).orElse(null);


        //boolean isOwner = user != null && user.getId().equals(advertisement.getUserIdDeprecated());
        boolean isOwner = user != null && user.getId().equals(ad.getUser().getId());

        model.addAttribute("advertisement", ad);
        model.addAttribute("istErsteller", isOwner);

        return "advertisements/details";
    }

    @GetMapping("/bearbeiten/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal){
        Advertisement ad = advertisementRepository.findById(id)
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

        /*if (!advertisement.getUserIdDeprecated().equals(user.getId())){
            return "redirect:/anzeigen";
        }*/
        if(!ad.getUser().getId().equals(user.getId())){
            return "redirect:/anzeigen";
        }



        model.addAttribute("advertisement", ad);
        return "advertisements/edit";
    }

    @GetMapping("/suche")
    public String searchAdvertisements(@RequestParam("q") String query, Model model){
        // Bei leerer Suche alle Anzeigen anzeigen
        if (query == null || query.trim().isEmpty()){
            model.addAttribute("anzeigen", advertisementRepository.findAll());
            return "list";
        }

        List<Advertisement> results = advertisementRepository.searchAll(query);

        model.addAttribute("anzeigen", results);
        model.addAttribute("suchbegriff", query);

        return "advertisements/list";
    }

    // ab hier die PostMappings

    @PostMapping("/neu")
    public String createAdvertisement(
            @RequestParam("titel") String title,
            @RequestParam("beschreibung") String description,
            @RequestParam("fach") String subjectName,
            @RequestParam("schulartId") long schoolTypeId,
            @RequestParam("preis") Double price,
            @RequestParam(value = "bild", required = false) MultipartFile image,
            @AuthenticationPrincipal User user,
            Principal principal
    ) {

        // Fach suchen/neu anlegen
        Subject subject = subjectRepository.findByNameIgnoreCase(subjectName)
                .orElseGet(() -> {
                    Subject newSubject = new Subject();
                    newSubject.setName(subjectName);
                    return subjectRepository.save(newSubject);
                });

        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(title);
        advertisement.setDescription(description);
        advertisement.setPrice(price);
        advertisement.setSubject(subject);
        SchoolType s = schoolTypeRepository.findById(schoolTypeId).orElseThrow();
        advertisement.setSchoolType(s);

        //advertisement.setUserIdDeprecated(1L); // TODO: ändern, sobald Login-Feature da ist
        /*User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow();*/

        advertisement.setUser(user);


        // Bild upload optional
        if (image != null && !image.isEmpty()) {

            // Größenlimit
            if (image.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Bild ist zu groß (max. 5 MB erlaubt)");
            }
            try {
                Path uploadDir = Paths.get("src/main/resources/static/images");
                Files.createDirectories(uploadDir); // erstellt Ordner falls er nicht existiert

                Path ziel = uploadDir.resolve(image.getOriginalFilename());
                Files.copy(image.getInputStream(), ziel, StandardCopyOption.REPLACE_EXISTING);

                advertisement.setImagePath(image.getOriginalFilename());

            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Fehler beim Speichern des Bildes");
            }

        } else {
            // Kein Bild
            advertisement.setImagePath(null);
        }

        advertisementRepository.save(advertisement);

        return "redirect:/anzeigen";
    }

    @PostMapping("/bearbeiten/{id}")
    public String updateAdvertisement(
            @PathVariable Long id,
            @RequestParam("titel") String title,
            @RequestParam("beschreibung") String description,
            @RequestParam("preis") Double price,
            @RequestParam(value = "bild", required = false) MultipartFile image

    ){
        Advertisement advertisement = advertisementRepository.findById(id).orElseThrow();
        advertisement.setTitle(title);
        advertisement.setDescription(description);
        advertisement.setPrice(price);

        if (image != null && !image.isEmpty()) {

            String filename = image.getOriginalFilename();
            Path uploadDir = Paths.get("src/main/resources/static/images/");
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Path ziel = uploadDir.resolve(filename);
            try {
                Files.copy(image.getInputStream(), ziel, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            advertisement.setImagePath(filename);
        }

        advertisementRepository.save(advertisement);
        return "redirect:/anzeigen/" + id;
    }

    @PostMapping("/loeschen/{id}")
    public String deleteAdvertisement(@PathVariable Long id, Principal principal) {

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anzeige nicht gefunden"));

        User user;
        if (principal != null) {
            user = userRepository.findByEmail(principal.getName()).orElse(null);
        } else {
            user = userRepository.findByEmail("test@test.de").orElse(null);
        }

        /*if (user == null || !advertisement.getUserIdDeprecated().equals(user.getId())) {
            return "redirect:/anzeigen";
        }*/
        if (user == null || !ad.getUser().getId().equals(user.getId())) {
            return "redirect:/anzeigen";
        }

        advertisementRepository.deleteById(id);

        return "redirect:/anzeigen";
    }

    // Anzeige buchen
    @GetMapping("/{advertisementId}/buchen")
    public String showBookingPage(@PathVariable Long advertisementId, Model model) {

        var anzeige = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Anzeige nicht gefunden"));

        //Long userId = anzeige.getUserIdDeprecated();
        Long userId = anzeige.getUser().getId();

        var slots = timeslotService.generateTimeslotsForUser(userId);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("slots", slots);

        return "buchung/timeslots";
    }

}
