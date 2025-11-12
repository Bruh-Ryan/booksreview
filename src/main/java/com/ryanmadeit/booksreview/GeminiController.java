package com.ryanmadeit.booksreview;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "http://localhost:8501")
public class GeminiController {

    private final GeminiService gemini;
    private final BookRepository repo;

    public GeminiController(GeminiService gemini, BookRepository repo) {
        this.gemini = gemini;
        this.repo = repo;
    }

    // Book description (tolerant: exact -> fallback)
    @GetMapping("/book/describe")
    public ResponseEntity<Map<String, String>> describeBook(@RequestParam("title") String title) {
        var exact = repo.findFirstByTitle(title);
        Books book = exact.orElseGet(() -> bestTitleFallback(title));
        if (book == null) {
            return ResponseEntity.ok(Map.of("description", "No matching book found for that title."));
        }
        String out = gemini.describeBook(book);
        return ResponseEntity.ok(Map.of("description", out));
    }

    // Author description
    @GetMapping("/author/describe")
    public ResponseEntity<Map<String, String>> describeAuthor(@RequestParam("name") String name) {
        String out = gemini.describeAuthor(name);
        return ResponseEntity.ok(Map.of("description", out));
    }

    private Books bestTitleFallback(String title) {
        List<Books> candidates = repo.findAllByTitleContainingIgnoreCase(title);
        if (candidates == null || candidates.isEmpty()) return null;
        final String tNorm = title.toLowerCase(Locale.ROOT).trim();
        return candidates.stream()
                .max(Comparator.comparingInt(b -> scoreTitleMatch(b.getTitle(), tNorm)))
                .orElse(candidates.get(0));
    }

    private int scoreTitleMatch(String dbTitle, String queryNorm) {
        if (dbTitle == null) return 0;
        String d = dbTitle.toLowerCase(Locale.ROOT).trim();
        if (d.equals(queryNorm)) return Integer.MAX_VALUE;
        if (d.contains(queryNorm)) return queryNorm.length();
        int score = 0;
        for (String token : queryNorm.split("\\s+")) {
            if (token.length() > 2 && d.contains(token)) score += token.length();
        }
        return score;
    }
}
