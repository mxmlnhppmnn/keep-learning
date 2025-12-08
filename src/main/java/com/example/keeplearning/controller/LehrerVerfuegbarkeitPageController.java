package com.example.keeplearning.controller;

import com.example.keeplearning.dto.VerfuegbarkeitRequest;
import com.example.keeplearning.dto.VerfuegbarkeitUpdateRequest;
import com.example.keeplearning.service.LehrerVerfuegbarkeitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/lehrer/{userId}/verfuegbarkeit")
public class LehrerVerfuegbarkeitPageController {
    private final LehrerVerfuegbarkeitService service;

    public LehrerVerfuegbarkeitPageController(LehrerVerfuegbarkeitService service){
        this.service = service;
    }

    @GetMapping
    public String showPage(@PathVariable Long userId, @RequestParam(required = false) Long edit, Model model){
        model.addAttribute("userId", userId);
        model.addAttribute("verfuegbarkeitList", service.getVerfuegbarkeiten(userId));

        model.addAttribute("editingId", edit);

        //Leeres Objekt fürs Formular
        model.addAttribute("form", new VerfuegbarkeitRequest(1, null, null));
        return "verfuegbarkeit";
    }

    @PostMapping
    public String createVerfuegbarkeit(@PathVariable Long userId, @ModelAttribute("form") VerfuegbarkeitRequest request){
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
            VerfuegbarkeitUpdateRequest request) {

        service.update(userId, id, request);

        return "redirect:/lehrer/" + userId + "/verfuegbarkeit";
    }

}
