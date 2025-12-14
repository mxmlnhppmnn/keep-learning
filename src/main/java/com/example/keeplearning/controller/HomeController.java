package com.example.keeplearning.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.keeplearning.dto.UserInfo;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    UserService userService;

    @GetMapping({ "/", "/home" })
    public String home(Model model, Principal principal) {
        return "home";
    }


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("userInfo", new UserInfo());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserInfo info, Model model) {

        Optional<User> user = userService.createUser(info);
        if (user.isPresent()) {
            return "redirect:/login";
        }

        // failed to create user
        model.addAttribute("error", "");
        return "register";
    }

    @GetMapping("/login")
    public String showUserLogin() {
        return "login";
    }

    @GetMapping("/user")
    public String showUserInfo(@RequestParam(required = false) String google, Model model, Principal principal) {
        User user = userService.getUser(principal).orElseThrow();

        boolean isGoogleConnected = user.getGoogleRefreshToken() != null;
        model.addAttribute("googleConnected", isGoogleConnected);

        // einmalige Meldungen (OK / Fehler)
        if ("ok".equals(google)) {
            model.addAttribute("googleMessage", "Google Calendar erfolgreich verbunden!");
        } else if ("error".equals(google)) {
            model.addAttribute("googleMessage", "Fehler beim Verbinden mit Google Calendar.");
        }

        model.addAttribute("user", user);
        model.addAttribute("lines", new String[] {
                "Name: " + user.getName(),
                "Email: " + user.getEmail(),
                "Passwort: " + user.getPassword()
        });
        return "utils/show-text";
    }

}
