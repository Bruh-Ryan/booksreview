package com.ryanmadeit.booksreview;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8501")
@RestController
public class BookController {

    private final BookRepository repo;
    private final BookEventProducer eventProducer;

    @Autowired
    public BookController(BookRepository repo, BookEventProducer eventProducer) {
        this.repo = repo;
        this.eventProducer = eventProducer;
    }

    @GetMapping("/")
    public ResponseEntity rootRedirect() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("/swagger-ui/index.html"))
            .build();
    }

    // Tolerant details lookup: exact -> case-insensitive fallback
    @GetMapping("/book/get-by-title")
    public ResponseEntity<Books> getBookByTitle(@RequestParam("title") String title) {
        // 1) Exact match
        var exact = repo.findFirstByTitle(title);
        if (exact.isPresent()) {
            Books book = exact.get();
            
            // Publish view event to Kafka
            BookViewEvent viewEvent = new BookViewEvent(
                book.getId(),
                book.getTitle(),
                book.getAuthors(),
                book.getAverageRating(),
                System.currentTimeMillis()
            );
            eventProducer.publishViewEvent(viewEvent);
            
            return ResponseEntity.ok(book);
        }

        // 2) Case-insensitive fallback: find best candidate
        List<Books> candidates = repo.findAllByTitleContainingIgnoreCase(title);
        if (candidates == null || candidates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Prefer exact case-insensitive equality if present, else longest overlap
        final String tNorm = title.toLowerCase(Locale.ROOT).trim();
        Books best = candidates.stream()
            .max(Comparator.comparingInt(b -> scoreTitleMatch(b.getTitle(), tNorm)))
            .orElse(candidates.get(0));
        
        // Publish view event for the matched book
        BookViewEvent viewEvent = new BookViewEvent(
            best.getId(),
            best.getTitle(),
            best.getAuthors(),
            best.getAverageRating(),
            System.currentTimeMillis()
        );
        eventProducer.publishViewEvent(viewEvent);
        
        return ResponseEntity.ok(best);
    }

    // Simple heuristic: prioritize full equality (case-insensitive), else substring length
    private int scoreTitleMatch(String dbTitle, String queryNorm) {
        if (dbTitle == null) return 0;
        String d = dbTitle.toLowerCase(Locale.ROOT).trim();
        if (d.equals(queryNorm)) return Integer.MAX_VALUE; // strongest match
        if (d.contains(queryNorm)) return queryNorm.length();
        
        // partial overlap score
        int score = 0;
        for (String token : queryNorm.split("\\s+")) {
            if (token.length() > 2 && d.contains(token)) score += token.length();
        }
        return score;
    }

    // List by partial title (search page)
    @GetMapping("/books/get-title")
    public ResponseEntity<List<Books>> findAllByTitle(@RequestParam("title") String title) {
        List<Books> list = repo.findAllByTitleContainingIgnoreCase(title);
        
        // Publish search event to Kafka
        BookSearchEvent searchEvent = new BookSearchEvent(
            title,
            "title",
            list != null ? list.size() : 0,
            System.currentTimeMillis()
        );
        eventProducer.publishSearchEvent(searchEvent);
        
        return ResponseEntity.ok(list);
    }

    // Exact authors string
    @GetMapping("/book/get-by-author")
    public ResponseEntity<Books> findFirstByAuthors(@RequestParam("authors") String authors) {
        return repo.findFirstByAuthors(authors)
            .map(book -> {
                // Publish view event
                BookViewEvent viewEvent = new BookViewEvent(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthors(),
                    book.getAverageRating(),
                    System.currentTimeMillis()
                );
                eventProducer.publishViewEvent(viewEvent);
                return ResponseEntity.ok(book);
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Partial author (search + "More by")
    @GetMapping("/books/get-author")
    public ResponseEntity<List<Books>> findAllByAuthorNames(@RequestParam("author") String author) {
        List<Books> list = repo.findAllByAuthorsContainingIgnoreCase(author);
        
        // Publish search event to Kafka
        BookSearchEvent searchEvent = new BookSearchEvent(
            author,
            "author",
            list != null ? list.size() : 0,
            System.currentTimeMillis()
        );
        eventProducer.publishSearchEvent(searchEvent);
        
        return ResponseEntity.ok(list);
    }
}
