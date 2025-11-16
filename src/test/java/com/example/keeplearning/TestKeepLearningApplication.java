package com.example.keeplearning;

import org.springframework.boot.SpringApplication;

public class TestKeepLearningApplication {

    public static void main(String[] args) {
        SpringApplication.from(KeepLearningApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
