package com.example.keeplearning.repository;
import com.example.keeplearning.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query("""
        SELECT a FROM Advertisement a
        WHERE 
            LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.subject.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.schoolType.name) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Advertisement> searchAll(@Param("query") String query);


}
