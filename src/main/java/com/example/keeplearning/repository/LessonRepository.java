package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    //damit man bei den timeslots die verfügbarkeiten eines einzelnen Lehrers rausrechnen kann
    //alternativ wäre, Termin noch eine LehrerId hinzuzufügen, aber das wäre redundant im Datenmodell

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM Lesson t
        JOIN LessonSeries s ON t.seriesId = s.id
        WHERE s.teacherId = :teacherId
          AND t.date = :date
          AND t.startTime = :startTime
    """)
    boolean existsLessonForTeacher(
            @Param("teacherId") Long teacherId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime
    );
}

