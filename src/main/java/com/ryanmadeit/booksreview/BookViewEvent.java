package com.ryanmadeit.booksreview;

public class BookViewEvent {
    private String bookId;
    private String title;
    private String authors;
    private double averageRating;
    private long timestamp;

    public BookViewEvent() {}

    public BookViewEvent(String bookId, String title, String authors, double averageRating, long timestamp) {
        this.bookId = bookId;
        this.title = title;
        this.authors = authors;
        this.averageRating = averageRating;
        this.timestamp = timestamp;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format(
            "BookViewEvent[id='%s', title='%s', authors='%s', rating=%.2f, time=%d]",
            bookId, title, authors, averageRating, timestamp
        );
    }
}
