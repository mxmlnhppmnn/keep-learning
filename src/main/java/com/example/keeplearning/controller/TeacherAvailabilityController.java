package com.example.keeplearning.controller;


import com.example.keeplearning.dto.AvailabilityRequest;
import com.example.keeplearning.entity.TeacherAvailability;
import com.example.keeplearning.service.TeacherAvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{userId}/verfuegbarkeit")
public class TeacherAvailabilityController {
    private final TeacherAvailabilityService service;

    public TeacherAvailabilityController(TeacherAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeacherAvailability> getAvailabilities(@PathVariable Long userId){
        return service.getAvailabilities(userId);
    }

    @PostMapping
    public TeacherAvailability createAvailability(@PathVariable Long userId, @RequestBody AvailabilityRequest req){
        return service.create(userId, req);
    }

    @DeleteMapping
    public void deleteAvailability(@PathVariable Long userId, @PathVariable Long verfId){
        service.delete(userId, verfId);

    }
}
