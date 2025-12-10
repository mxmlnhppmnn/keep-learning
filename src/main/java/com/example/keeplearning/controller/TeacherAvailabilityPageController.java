package com.example.keeplearning.controller;

import com.example.keeplearning.dto.AvailabilityRequest;
import com.example.keeplearning.dto.AvailabilityUpdateRequest;
import com.example.keeplearning.service.TeacherAvailabilityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lehrer/{userId}/verfuegbarkeit")
public class TeacherAvailabilityPageController {
    private final TeacherAvailabilityService service;

    public TeacherAvailabilityPageController(TeacherAvailabilityService service){
        this.service = service;
    }

    @GetMapping
    public String showPage(@PathVariable Long userId, @RequestParam(required = false) Long edit, Model model){
        model.addAttribute("userId", userId);
        model.addAttribute("verfuegbarkeitList", service.getAvailabilities(userId));

        model.addAttribute("editingId", edit);

        //Leeres Objekt fürs Formular
        model.addAttribute("form", new AvailabilityRequest(1, null, null));
        return "verfuegbarkeit";
    }

    @PostMapping
    public String createVerfuegbarkeit(@PathVariable Long userId, @ModelAttribute("form") AvailabilityRequest request){
        service.create(userId, request);

        return "redirect:/lehrer/" + userId + "/verfuegbarkeit";
    }

    @PostMapping("/delete/{id}")
    public String deleteVerfuegbarkeit(
            @PathVariable Long userId,
            @PathVariable Long id) {

        service.delete(userId, id);

        return "redirect:/lehrer/" + userId + "/verfuegbarkeit";
    }

    @PostMapping("/update/{id}")
    public String updateVerfuegbarkeit(
            @PathVariable Long userId,
            @PathVariable Long id,
            AvailabilityUpdateRequest request) {

        service.update(userId, id, request);

        return "redirect:/lehrer/" + userId + "/verfuegbarkeit";
    }

}
