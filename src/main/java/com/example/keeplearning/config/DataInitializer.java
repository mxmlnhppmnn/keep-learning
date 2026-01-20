package com.example.keeplearning.config;

import com.example.keeplearning.entity.SchoolType;
import com.example.keeplearning.entity.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.keeplearning.repository.SchoolTypeRepository;
import com.example.keeplearning.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SchoolTypeRepository schoolTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (schoolTypeRepository.count() == 0) {  // nur initialisieren, wenn leer
            String[] names = { "Grundschule", "Realschule", "Gymnasium"};
            for (String name : names) {
                SchoolType s = new SchoolType();
                s.setName(name);
                schoolTypeRepository.save(s);
            }
        }

        // Admin-Account fuer Demo/Moderation (falls noch nicht vorhanden)
        // Login: admin@keeplearning.local | Passwort: Admin123!
        if (!userRepository.existsByEmail("admin@keeplearning.local")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@keeplearning.local");
            admin.setPasswordEncoded(passwordEncoder.encode("Admin123!"));
            admin.setVerified(true);
            // role-Feld ohne ROLE_-Prefix
            // (User.getAuthorities baut ROLE_ + role.toUpperCase())
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }
}
