package com.example.keeplearning.service.favorite;

import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.entity.favorite.Favorite;
import com.example.keeplearning.repository.AdvertisementRepository;
import com.example.keeplearning.repository.FavoriteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional //wegen insert und delete
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final AdvertisementRepository advertisementRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           AdvertisementRepository advertisementRepository) {
        this.favoriteRepository = favoriteRepository;
        this.advertisementRepository = advertisementRepository;
    }

    public void addFavorite(Long userId, Long advertisementId) {

        advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new RuntimeException("Anzeige nicht gefunden"));

        //falls schon favorisiert
        if (favoriteRepository.existsByUserIdAndAdvertisementId(userId, advertisementId)) {
            return;
        }

        Favorite favorite = new Favorite();

        //user und ad setzen
        User user = new User();
        user.setId(userId);
        favorite.setUser(user);

        Advertisement ad = new Advertisement();
        ad.setId(advertisementId);
        favorite.setAdvertisement(ad);

        favoriteRepository.save(favorite);
    }

    public void removeFavorite(Long userId, Long advertisementId) {
        favoriteRepository.deleteByUserIdAndAdvertisementId(userId, advertisementId);
    }

    public boolean isFavorite(Long userId, Long advertisementId) {
        return favoriteRepository.existsByUserIdAndAdvertisementId(userId, advertisementId);
    }

    public List<Advertisement> getFavoriteAdvertisements(Long userId) {
        return favoriteRepository.findByUserId(userId)
                .stream()
                .map(Favorite::getAdvertisement)
                .toList();
    }
}
