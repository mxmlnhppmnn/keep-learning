package com.example.keeplearning.service;

import com.example.keeplearning.entity.LehrerVerfuegbarkeit;
import com.example.keeplearning.repository.LehrerVerfuegbarkeitRepository;
import com.example.keeplearning.dto.VerfuegbarkeitRequest;
import com.example.keeplearning.dto.VerfuegbarkeitUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LehrerVerfuegbarkeitService {
    private final LehrerVerfuegbarkeitRepository repo;

    public LehrerVerfuegbarkeitService(LehrerVerfuegbarkeitRepository repo) {
        this.repo = repo;
    }

    //Verfügbarkeiten eines Lehrers bekommen
    public List<LehrerVerfuegbarkeit> getVerfuegbarkeiten(Long userId){
        return repo.findByUserId(userId);
    }

    //Lehrer gibt eine neue Verfügbarkeit an
    public LehrerVerfuegbarkeit create(Long userId, VerfuegbarkeitRequest req){
        LehrerVerfuegbarkeit v = new LehrerVerfuegbarkeit();
        v.setUserId(userId);
        v.setWochentag(req.wochentag());
        v.setStartZeit(req.startZeit());
        v.setEndZeit(req.endZeit());

        return repo.save(v);
    }

    //Löschen der bisherigen Verfügbarkeit
    public void delete(Long userId, Long verfuegbarkeitId){
        LehrerVerfuegbarkeit v = repo.findById(verfuegbarkeitId).orElseThrow(() -> new RuntimeException("Verfübarkeit wurde nicht gefunden"));

        if(!v.getUserId().equals(userId)){
            throw new RuntimeException("Nur der Eigentümer darf die Verfügbarkeit löschen");
        }
        repo.delete(v);
    }
    public LehrerVerfuegbarkeit update(Long userId, Long verfuegbarkeitId, VerfuegbarkeitUpdateRequest request) {

        // Der zu bearbeitende Eintrag
        LehrerVerfuegbarkeit v = repo.findById(verfuegbarkeitId)
                .orElseThrow(() -> new RuntimeException("Verfügbarkeit nicht gefunden"));

        // nur der Eigentümer soll die Verfügbarkeiten bearbeiten dürfen
        if (!v.getUserId().equals(userId)) {
            throw new RuntimeException("Verfügbarkeit gehört nicht dem Benutzer " + userId);
        }

        // aktualisieren
        v.setWochentag(request.wochentag());
        v.setStartZeit(request.startZeit());
        v.setEndZeit(request.endZeit());

        // speichern
        return repo.save(v);
    }
}
