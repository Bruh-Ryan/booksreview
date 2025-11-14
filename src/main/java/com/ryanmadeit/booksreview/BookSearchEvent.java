package com.ryanmadeit.booksreview;

public class BookSearchEvent {
    private String searchQuery;
    private String searchType; // "title" or "author"
    private int resultsCount;
    private long timestamp;

    public BookSearchEvent() {}

    public BookSearchEvent(String searchQuery, String searchType, int resultsCount, long timestamp) {
        this.searchQuery = searchQuery;
        this.searchType = searchType;
        this.resultsCount = resultsCount;
        this.timestamp = timestamp;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public int getResultsCount() {
        return resultsCount;
    }

    public void setResultsCount(int resultsCount) {
        this.resultsCount = resultsCount;
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
            "BookSearchEvent[query='%s', type='%s', results=%d, time=%d]",
            searchQuery, searchType, resultsCount, timestamp
        );
    }
}
