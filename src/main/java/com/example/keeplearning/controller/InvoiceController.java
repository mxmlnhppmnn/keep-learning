package com.example.keeplearning.controller;

import com.example.keeplearning.entity.Invoice;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.service.InvoiceService;
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
@RequestMapping("/rechnungen")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal User user) {
        List<Invoice> invoices = invoiceService.listInvoicesForStudent(user.getId());
        model.addAttribute("invoices", invoices);
        model.addAttribute("series", invoiceService.listSeriesForStudent(user.getId()));
        return "invoice/list";
    }

    @PostMapping("/create")
    public String create(@RequestParam Long seriesId, @AuthenticationPrincipal User user) {
        invoiceService.createInvoiceForStudent(user.getId(), seriesId);
        return "redirect:/rechnungen?created=1";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @AuthenticationPrincipal User user) {
        Invoice invoice = invoiceService.getInvoiceForStudent(id, user.getId());

        byte[] bytes = invoice.getContent().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoice.getFilename() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
