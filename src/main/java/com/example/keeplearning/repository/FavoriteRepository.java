package com.example.keeplearning.repository;

import com.example.keeplearning.entity.favorite.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndAdvertisementId(Long userId, Long advertisementId);

    List<Favorite> findByUserId(Long userId);

    void deleteByUserIdAndAdvertisementId(Long userId, Long advertisementId);
}
