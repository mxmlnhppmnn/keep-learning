package com.example.keeplearning.service;

import com.example.keeplearning.dto.Timeslot;
import com.example.keeplearning.entity.TeacherAvailability;
import com.example.keeplearning.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TimeslotService {

    private final TeacherAvailabilityService verfService;
    private final LessonRepository lessonRepository;

    public TimeslotService(TeacherAvailabilityService verfService, LessonRepository lessonRepository) {
        this.verfService = verfService;
        this.lessonRepository = lessonRepository;
    }

    public List<Timeslot> generateTimeslotsForUser(Long userId) {

        // Alle Verfügbarkeiten des Lehrers laden
        List<TeacherAvailability> verfList = verfService.getAvailabilities(userId);
        //hier werden die freien slots gesammelt
        List<Timeslot> result = new ArrayList<>();


        LocalDate today = LocalDate.now();
        LocalDate until = today.plusDays(7); //vllt später ändern auf 14?
        int slotMinutes = 60;

        // Für die nächsten 7 Tage Slots generieren
        //iteriert über jeden Tag im Zeitraum
        for (LocalDate date = today; !date.isAfter(until); date = date.plusDays(1)) {

            int wochentag = date.getDayOfWeek().getValue(); // 1= mo, 2= di, ...

            //jede verfügbarkeit des lehrers prüfen
            for (TeacherAvailability v : verfList) {

                if (v.getWeekday() != wochentag)
                    continue;

                // Gültigkeitszeitraum wird vielleicht später noch ins html getan für z.B. Urlaub
                if (v.getValidFrom() != null && date.isBefore(v.getValidFrom()))
                    continue;

                if (v.getValidUntil() != null && date.isAfter(v.getValidUntil()))
                    continue;

                LocalTime current = v.getStartTime();

                // Slots erzeugen, nur freie (nicht-gebuchte) Zeiträume sollen angezeigt werden
                while (!current.plusMinutes(slotMinutes).isAfter(v.getEndTime())) {
                    boolean gebucht = lessonRepository.existsLessonForTeacher(userId, date, current);

                    if (!gebucht) {
                        result.add(new Timeslot(date, current, current.plusMinutes(slotMinutes)));
                    }
                    current = current.plusMinutes(slotMinutes);
                }
            }
        }

        return result;
    }
}

