package com.example.keeplearning.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.keeplearning.repository.SchulartRepository;
import com.example.keeplearning.entity.Schulart;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SchulartRepository schulartRepository;

    @Override
    public void run(String... args) throws Exception {
        if (schulartRepository.count() == 0) {  // nur initialisieren, wenn leer
            String[] names = { "Grundschule", "Realschule", "Gymnasium"};
            for (String name : names) {
                Schulart s = new Schulart();
                s.setName(name);
                schulartRepository.save(s);
            }
        }
    }
}
