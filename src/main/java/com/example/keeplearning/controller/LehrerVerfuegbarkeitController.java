package com.example.keeplearning.controller;

import com.example.keeplearning.dto.VerfuegbarkeitRequest;
import com.example.keeplearning.entity.LehrerVerfuegbarkeit;
import com.example.keeplearning.service.LehrerVerfuegbarkeitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/{userId}/verfuegbarkeit")
public class LehrerVerfuegbarkeitController {
    private final LehrerVerfuegbarkeitService service;

    public LehrerVerfuegbarkeitController(LehrerVerfuegbarkeitService service) {
        this.service = service;
    }

    @GetMapping
    public List<LehrerVerfuegbarkeit> getVerfuegbarkeiten(@PathVariable Long userId){
        return service.getVerfuegbarkeiten(userId);
    }

    @PostMapping
    public LehrerVerfuegbarkeit createVerfuegbarkeit(@PathVariable Long userId, @RequestBody VerfuegbarkeitRequest req){
        return service.create(userId, req);
    }

    @DeleteMapping
    public void deleteVerfuegbarkeit(@PathVariable Long userId, @PathVariable Long verfId){
        service.delete(userId, verfId);

    }
}
