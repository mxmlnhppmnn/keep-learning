package com.example.keeplearning.controller.admin;

import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.admin.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.service.UserService;


import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserAdminService userAdminService;
    private final UserRepository userRepository;

    AdminUserController(UserService userService, UserAdminService userAdminService, UserRepository userRepository) {
        this.userService = userService;
        this.userAdminService = userAdminService;
        this.userRepository = userRepository;
    }

    //Liste aller user
    //eigene admin id wird mitgegeben, um das anti-selbst-sperre zu ermöglichen
    @GetMapping
    public String listUsers(Model model, Principal principal) {

        User currentAdmin = userService
                .getUserByEmail(principal.getName())
                .orElseThrow();

        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("currentAdminId", currentAdmin.getId());
        return "admin/user-list";
    }

    //sperren eines users
    @PostMapping("/{id}/lock")
    public String lockUser(
            @PathVariable Long id,
            @RequestParam(required = false) String until, @RequestParam(required = false) String reason,
            Principal principal) {

        User admin = userService.getUserByEmail(principal.getName())
                .orElseThrow();

        LocalDateTime lockUntil = null;

        if (until != null && !until.isBlank()) {
            lockUntil = LocalDateTime.parse(until);
        }

        userAdminService.lockUser(admin.getId(), id, lockUntil, reason);

        return "redirect:/admin/users";
    }

    //entsperren eines users
    @PostMapping("/{id}/unlock")
    public String unlockUser(@PathVariable Long id) {
        userAdminService.unlockUser(id);
        return "redirect:/admin/users";
    }

    //soft deleting eines users
    //wird nicht wirklich aus der DB gelöscht
    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            Principal principal) {

        User admin = userService.getUserByEmail(principal.getName())
                .orElseThrow();

        userAdminService.deleteUser(admin.getId(), id);

        return "redirect:/admin/users";
    }
}
