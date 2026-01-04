package com.example.keeplearning.service.similarAd;
import com.example.keeplearning.entity.Advertisement;
import com.example.keeplearning.repository.AdvertisementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //braucht nur Lese-Zugriff
public class SimilarAdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    public SimilarAdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public List<Advertisement> findSimilarAdvertisements(Advertisement ad, int limit) {

        // sicherstellen dass es ein fach und eine id gibt
        if (ad.getSubject() == null || ad.getSubject().getId() == null) {
            return List.of();
        }

        Long schoolTypeId = ad.getSchoolType() != null ? ad.getSchoolType().getId() : null;

        //ruft findSimilar aus dem Repository auf
        return advertisementRepository.findSimilar(
                ad.getId(),
                ad.getSubject().getId(),
                schoolTypeId,
                PageRequest.of(0, limit) //nur begrenzt (limit viele) ähnliche Anzeigen anzeigen
        );
    }
}
