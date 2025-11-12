package com.ryanmadeit.booksreview;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Books, String> {

    // Exact title
    Optional<Books> findFirstByTitle(String title);

    // Case-insensitive partial title search
    List<Books> findAllByTitleContainingIgnoreCase(String title);

    // Exact authors string (entity field is 'authors')
    Optional<Books> findFirstByAuthors(String authors);

    // Case-insensitive partial author search
    List<Books> findAllByAuthorsContainingIgnoreCase(String authors);
}
