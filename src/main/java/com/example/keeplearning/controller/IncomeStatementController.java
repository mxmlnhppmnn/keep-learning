package com.example.keeplearning.controller;

import com.example.keeplearning.entity.IncomeStatement;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.service.IncomeStatementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/einnahmen")
public class IncomeStatementController {

    private final IncomeStatementService incomeStatementService;

    public IncomeStatementController(IncomeStatementService incomeStatementService) {
        this.incomeStatementService = incomeStatementService;
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal User user) {
        List<IncomeStatement> statements = incomeStatementService.listForTeacher(user.getId());
        model.addAttribute("statements", statements);
        model.addAttribute("series", incomeStatementService.listSeriesForTeacher(user.getId()));
        return "income/list";
    }

    @PostMapping("/create")
    public String create(@RequestParam Long seriesId, @AuthenticationPrincipal User user) {
        incomeStatementService.createForTeacher(user.getId(), seriesId);
        return "redirect:/einnahmen?created=1";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @AuthenticationPrincipal User user) {
        IncomeStatement st = incomeStatementService.getForTeacher(id, user.getId());
        byte[] bytes = st.getContent().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + st.getFilename() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
