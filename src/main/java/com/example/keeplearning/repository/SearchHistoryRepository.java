package com.example.keeplearning.repository;

import com.example.keeplearning.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("""
        SELECT h.query
        FROM SearchHistory h
        WHERE h.userId = :userId
        ORDER BY h.createdAt DESC
    """)
    List<String> findRecentQueriesForUser(@Param("userId") Long userId);

    @Query("""
        SELECT h.query
        FROM SearchHistory h
        GROUP BY h.query
        ORDER BY COUNT(h.id) DESC
    """)
    List<String> findPopularQueries();
}
