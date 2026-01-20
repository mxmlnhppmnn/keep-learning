package com.example.keeplearning.service;

import com.example.keeplearning.repository.SearchHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Service
public class SearchSuggestionService {

    private final SearchHistoryRepository searchHistoryRepository;

    public SearchSuggestionService(SearchHistoryRepository searchHistoryRepository) {
        this.searchHistoryRepository = searchHistoryRepository;
    }

    public List<String> getSuggestions(Long userId, String query, int limit) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        LinkedHashSet<String> out = new LinkedHashSet<>();

        if (userId != null) {
            List<String> recent = searchHistoryRepository.findRecentQueriesForUser(userId);
            for (String s : recent) {
                if (s == null) continue;
                if (!q.isEmpty() && !s.toLowerCase(Locale.ROOT).contains(q)) continue;
                out.add(s);
                if (out.size() >= limit) return new ArrayList<>(out);
            }
        }

        List<String> popular = searchHistoryRepository.findPopularQueries();
        for (String s : popular) {
            if (s == null) continue;
            if (!q.isEmpty() && !s.toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(s);
            if (out.size() >= limit) break;
        }

        return new ArrayList<>(out);
    }
}
