package org.example;

public class SearchResult {
    private final String fullUrl;
    private final String relevantParagraph;
    private final int relevanceScore;

    public SearchResult(String relevantParagraph, String fullUrl, int relevanceScore) {
        this.relevantParagraph = relevantParagraph;
        this.fullUrl = fullUrl;
        this.relevanceScore = relevanceScore;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public String getRelevantParagraph(){ return relevantParagraph; }

    public int getRelevanceScore() { return relevanceScore; }
}
