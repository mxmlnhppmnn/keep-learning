package com.example.keeplearning.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.keeplearning.entity.Review;
import com.example.keeplearning.entity.User;

import java.util.Optional;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findById(Long id);

    List<Review> findByUser(User user);
    Page<Review> findByUser(User user, Pageable pageable);
    Page<Review> findByUserAndRating(User user, int rating, Pageable pageable);

    @Query("""
                SELECT avg(rating)
                FROM Review r
                WHERE r.user = :user
            """)
    Double findAverageRatingByUser(@Param("user") User user);
    int countByUserAndRating(User user, int rating);

}
