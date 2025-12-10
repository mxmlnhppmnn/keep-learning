package com.example.keeplearning.service;

import com.example.keeplearning.entity.TeacherAvailability;
import com.example.keeplearning.repository.TeacherAvailabilityRepository;
import com.example.keeplearning.dto.AvailabilityRequest;
import com.example.keeplearning.dto.AvailabilityUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherAvailabilityService {
    private final TeacherAvailabilityRepository repo;

    public TeacherAvailabilityService(TeacherAvailabilityRepository repo) {
        this.repo = repo;
    }

    //Verfügbarkeiten eines Lehrers bekommen
    public List<TeacherAvailability> getAvailabilities(Long userId){
        return repo.findByUserId(userId);
    }

    //Lehrer gibt eine neue Verfügbarkeit an
    public TeacherAvailability create(Long userId, AvailabilityRequest req){
        TeacherAvailability v = new TeacherAvailability();
        v.setUserId(userId);
        v.setWeekday(req.weekday());
        v.setStartTime(req.startTime());
        v.setEndTime(req.endTime());

        return repo.save(v);
    }

    //Löschen der bisherigen Verfügbarkeit
    public void delete(Long userId, Long availabilityId){
        TeacherAvailability v = repo.findById(availabilityId).orElseThrow(() -> new RuntimeException("Verfübarkeit wurde nicht gefunden"));

        if(!v.getUserId().equals(userId)){
            throw new RuntimeException("Nur der Eigentümer darf die Verfügbarkeit löschen");
        }
        repo.delete(v);
    }
    public TeacherAvailability update(Long userId, Long availabilityId, AvailabilityUpdateRequest request) {

        // Der zu bearbeitende Eintrag
        TeacherAvailability v = repo.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Verfügbarkeit nicht gefunden"));

        // nur der Eigentümer soll die Verfügbarkeiten bearbeiten dürfen
        if (!v.getUserId().equals(userId)) {
            throw new RuntimeException("Verfügbarkeit gehört nicht dem Benutzer " + userId);
        }

        // aktualisieren
        v.setWeekday(request.weekday());
        v.setStartTime(request.startTime());
        v.setEndTime(request.endTime());

        // speichern
        return repo.save(v);
    }
}
