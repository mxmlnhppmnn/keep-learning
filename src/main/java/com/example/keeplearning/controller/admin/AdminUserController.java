package com.example.keeplearning.controller.admin;

import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.admin.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAdminService adminService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/user-list";
    }

    @PostMapping("/{id}/lock")
    public String lockUser(
            @PathVariable Long id,
            @RequestParam(required = false) String until) {

        LocalDateTime lockUntil = until == null || until.isBlank()
                ? null
                : LocalDateTime.parse(until);

        adminService.lockUser(id, lockUntil, "Admin-Sperre");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
