package com.ryanmadeit.booksreview;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("booknames")
public class Books {

    @Id
    private String id;

    private String title;
    private String authors;

    @Field("ratings_count")
    private int ratingsCount;

    @Field("average_rating")
    private double averageRating;

    @Field("publisher")
    private String publisher;

    @Field("publication_date")
    private String publishedDate;


    public Books() {}

    public String getId() { 
        return id; 
    }

    public String getTitle() { 
        return title; 
    }

    public String getAuthors() { 
        return authors; 
    }

    public int getRatingsCount() { 
        return ratingsCount; 
    }

    public double getAverageRating() { 
        return averageRating; 
    }

    public String getPublisher() { 
        return publisher; 
    }

    public String getPublishedDate() { 
        return publishedDate; 
    }

    @Override
    public String toString() {
        return String.format(
            "id='%s'Book Title='%s', author(s)='%s', averageRating=%.2f, ratingsCount=%d]",
            id ,title, authors, averageRating, ratingsCount
        );
    }
}
