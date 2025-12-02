package com.example.keeplearning.repository;
import com.example.keeplearning.entity.Anzeige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnzeigeRepository extends JpaRepository<Anzeige, Long> {

    @Query("""
    SELECT a FROM Anzeige a
    WHERE 
        LOWER(a.titel) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.beschreibung) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.fach.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.schulart.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Anzeige> searchAll(@Param("query") String query);


}
