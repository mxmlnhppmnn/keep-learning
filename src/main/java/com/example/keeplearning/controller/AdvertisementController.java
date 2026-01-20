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
import com.example.keeplearning.service.favorite.FavoriteService;
import com.example.keeplearning.service.similarAd.SimilarAdvertisementService;
import com.example.keeplearning.entity.SearchHistory;
import com.example.keeplearning.repository.SearchHistoryRepository;
import com.example.keeplearning.service.SearchSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.time.LocalDateTime;
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

    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private SimilarAdvertisementService similarAdvertisementService;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private SearchSuggestionService searchSuggestionService;

    @GetMapping
    public String listAdvertisements(Model model, @AuthenticationPrincipal User user) {
        List<Advertisement> all = advertisementRepository.findAll();
        model.addAttribute("anzeigen", filterVisibleAdvertisements(all, user));
        return "advertisements/list";
    }

    private List<Advertisement> filterVisibleAdvertisements(List<Advertisement> ads, User user) {
        if (ads == null || ads.isEmpty()) return ads;

        return ads.stream().filter(ad -> {
            if (!ad.isBooked()) {
                return true;
            }

            // Gebuchte Anzeige bleibt sichtbar fuer:
            // 1) Owner (Lehrer)
            // 2) Schueler, der gebucht hat
            if (user == null) {
                return false;
            }
            if (ad.getUser() != null && ad.getUser().getId() != null && ad.getUser().getId().equals(user.getId())) {
                return true;
            }
            return ad.getBookedStudentId() != null && ad.getBookedStudentId().equals(user.getId());
        }).toList();
    }

    //Anzeige erstellen
    @GetMapping("/neu")
    public String showCreateForm(Model model) {
        model.addAttribute("anzeige", new Advertisement());
        //List<SchoolType> alleSchularten = schoolTypeRepository.findAll(); // Schularten fürs Dropdown
        model.addAttribute("schularten", schoolTypeRepository.findAll());
        return "advertisements/create";
    }

    @GetMapping("/{id}")
    public String showAdvertisementDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal User user) {

        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Anzeige wurde nicht gefunden"));

        boolean isOwner = user != null && user.getId().equals(ad.getUser().getId());
        boolean isFavorite = false;
        if (user != null) {
            isFavorite = favoriteService.isFavorite(user.getId(), id);
        }
        model.addAttribute("similarAdvertisements",
                similarAdvertisementService.findSimilarAdvertisements(ad, 4)
        ); //soll nur 4 ähnliche Anzeigen anzeigen
        model.addAttribute("advertisement", ad);
        model.addAttribute("istErsteller", isOwner);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("owner", ad.getUser());

        return "advertisements/details";
    }

    @GetMapping("/bearbeiten/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @AuthenticationPrincipal User user){
        Advertisement ad = advertisementRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Anzeige wurde nicht gefunden"));

        if(!ad.getUser().getId().equals(user.getId())){
            return "redirect:/anzeigen";
        }

        model.addAttribute("advertisement", ad);
        return "advertisements/edit";
    }

    @GetMapping("/suche")
    public String searchAdvertisements(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "title,asc") String sort,
            Model model,
            @AuthenticationPrincipal User user
    ) {
        String[] sortParams = sort.split(",");
        Sort sorting = Sort.by(
                Sort.Direction.fromString(sortParams[1]),
                sortParams[0]
        );

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Advertisement> results;

        if (q == null || q.trim().isEmpty()) {
            results = advertisementRepository.findAll(pageable);
        } else {
            results = advertisementRepository
                    .findByTitleContainingIgnoreCase(q, pageable);

            // Suchverlauf speichern (nur wenn eingeloggt)
            if (user != null) {
                SearchHistory h = new SearchHistory();
                h.setUserId(user.getId());
                h.setQuery(q.trim());
                h.setCreatedAt(LocalDateTime.now());
                searchHistoryRepository.save(h);
            }
        }

        model.addAttribute("anzeigen", filterVisibleAdvertisements(results.getContent(), user));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", results.getTotalPages());
        model.addAttribute("suchbegriff", q);
        model.addAttribute("sort", sort);

        return "advertisements/list";
    }

    @GetMapping("/suggestions")
    @ResponseBody
    public List<String> suggestions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "8") int limit,
            @AuthenticationPrincipal User user
    ) {
        Long userId = user != null ? user.getId() : null;
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return searchSuggestionService.getSuggestions(userId, q, safeLimit);
    }


    @PostMapping("/neu")
    public String createAdvertisement(
            @RequestParam("titel") String title,
            @RequestParam("beschreibung") String description,
            @RequestParam("fach") String subjectName,
            @RequestParam("schulartId") long schoolTypeId,
            @RequestParam("preis") Double price,
            @RequestParam(value = "bild", required = false) MultipartFile image,
            @AuthenticationPrincipal User user
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

        // Wenn die Anzeige bereits gebucht wurde, soll kein weiterer Schueler buchen koennen.
        if (anzeige.isBooked()) {
            return "redirect:/anzeigen/" + advertisementId + "?alreadyBooked=1";
        }

        Long userId = anzeige.getUser().getId();

        var slots = timeslotService.generateTimeslotsForUser(userId);

        model.addAttribute("anzeige", anzeige);
        model.addAttribute("slots", slots);

        return "booking/timeslots";
    }

}
