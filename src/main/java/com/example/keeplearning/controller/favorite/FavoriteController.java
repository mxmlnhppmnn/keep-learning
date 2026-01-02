package com.example.keeplearning.controller.favorite;
import com.example.keeplearning.entity.User;
import com.example.keeplearning.service.favorite.FavoriteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/favoriten")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    //alle favoriten des nutzers anzeigen
    @GetMapping
    public String favoritesPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute(
                "favorites",
                favoriteService.getFavoriteAdvertisements(user.getId())
        );
        return "favorites";
    }

    //neuen Favorit hinzufügen
    @PostMapping("/{advertisementId}")
    public String addFavorite(@AuthenticationPrincipal User user, @PathVariable Long advertisementId) {
        favoriteService.addFavorite(user.getId(), advertisementId);
        return "redirect:/anzeigen/" + advertisementId;
    }

    @PostMapping("/{advertisementId}/delete")
    public String removeFavorite(
            @AuthenticationPrincipal User user,
            @PathVariable Long advertisementId,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        favoriteService.removeFavorite(user.getId(), advertisementId);

        // Fallback, falls man keinen referer hat
        if (referer == null || referer.isBlank()) {
            return "redirect:/favoriten";
        }

        //man wird dahin redirected, wo man herkam
        //damit das favoriten entfernen auf der favoriten seite und auf der detailseite nicht auf der gleichen Seite landet
        return "redirect:" + referer;
    }
}
