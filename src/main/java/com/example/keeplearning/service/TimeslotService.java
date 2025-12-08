package com.example.keeplearning.service;

import com.example.keeplearning.dto.Timeslot;
import com.example.keeplearning.entity.LehrerVerfuegbarkeit;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeslotService {

    private final LehrerVerfuegbarkeitService verfService;

    public TimeslotService(LehrerVerfuegbarkeitService verfService) {
        this.verfService = verfService;
    }

    public List<Timeslot> generateTimeslotsForUser(Long userId) {

        // Alle Verfügbarkeiten des Lehrers laden
        List<LehrerVerfuegbarkeit> verfList = verfService.getVerfuegbarkeiten(userId);
        List<Timeslot> result = new ArrayList<>();

        // Debug
        System.out.println("DEBUG: Verfügbarkeiten für userId=" + userId);
        System.out.println("DEBUG: Anzahl=" + verfList.size());
        for (LehrerVerfuegbarkeit v : verfList) {
            System.out.println(
                    "   ID=" + v.getVerfuegbarkeitId() +
                            " Wochentag=" + v.getWochentag() +
                            " Start=" + v.getStartZeit() +
                            " Ende=" + v.getEndZeit() +
                            " gültigAb=" + v.getGueltigAb() +
                            " gültigBis=" + v.getGueltigBis()
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(7);
        int slotMinutes = 60;

        // Für die nächsten 7 Tage Slots generieren
        for (LocalDate date = today; !date.isAfter(until); date = date.plusDays(1)) {

            int wochentag = date.getDayOfWeek().getValue(); // 1 = Montag, 7 = Sonntag

            for (LehrerVerfuegbarkeit v : verfList) {

                // Wochentag muss passen
                if (v.getWochentag() != wochentag)
                    continue;

                // Gültigkeitszeitraum prüfen
                if (v.getGueltigAb() != null && date.isBefore(v.getGueltigAb()))
                    continue;

                if (v.getGueltigBis() != null && date.isAfter(v.getGueltigBis()))
                    continue;

                LocalTime current = v.getStartZeit();

                // Slots erzeugen
                while (!current.plusMinutes(slotMinutes).isAfter(v.getEndZeit())) {

                    result.add(new Timeslot(
                            date,
                            current,
                            current.plusMinutes(slotMinutes)
                    ));

                    current = current.plusMinutes(slotMinutes);
                }
            }
        }

        return result;
    }
}

