package com.example.keeplearning.repository;
import com.example.keeplearning.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    Page<Advertisement> findByTitleContainingIgnoreCase(
            String title,
            Pageable pageable
    );

//Ähnliche Anzeigen, die den gleichen Schultyp und das gleiche Fach haben finden
    //aktuelle Anzeige soll nicht vorgeschlagen werden
    @Query("""
        SELECT a
        FROM Advertisement a
        WHERE a.id <> :adId
          AND a.subject.id = :subjectId
          AND (:schoolTypeId IS NULL OR a.schoolType.id = :schoolTypeId)
    """)
    List<Advertisement> findSimilar(
            @Param("adId") Long adId,
            @Param("subjectId") Long subjectId,
            @Param("schoolTypeId") Long schoolTypeId,
            Pageable pageable
    );


}
