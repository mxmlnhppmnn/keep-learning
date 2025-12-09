package com.example.keeplearning.repository;

import com.example.keeplearning.entity.Termin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface TerminRepository extends JpaRepository<Termin, Long> {

    //damit man bei den timeslots die verfügbarkeiten eines einzelnen Lehrers rausrechnen kann
    //alternativ wäre, Termin noch eine LehrerId hinzuzufügen, aber das wäre redundant im Datenmodell

    @Query("""
        SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
        FROM Termin t
        JOIN TerminSerie s ON t.serieId = s.serieId
        WHERE s.lehrerId = :lehrerId
          AND t.datum = :datum
          AND t.startzeit = :startzeit
    """)
    boolean existsTerminForLehrer(
            @Param("lehrerId") Long lehrerId,
            @Param("datum") LocalDate datum,
            @Param("startzeit") LocalTime startzeit
    );
}

