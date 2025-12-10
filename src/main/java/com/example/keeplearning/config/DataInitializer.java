package com.example.keeplearning.config;

import com.example.keeplearning.entity.SchoolType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.keeplearning.repository.SchoolTypeRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SchoolTypeRepository schoolTypeRepository;

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
    }
}
