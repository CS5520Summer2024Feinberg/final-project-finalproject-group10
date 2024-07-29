package com.example.group10_finalproject.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Review {
    private String reviewId;
    private String dateCreated;
    private String content;
    private int rating;
    private String questId;
    private String userId;

    public Review(String content, int rating, String questId, String userId) {
        this.reviewId = UUID.randomUUID().toString();
        this.dateCreated = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.content = content;
        this.rating = rating;
        this.questId = questId;
        this.userId = userId;
    }

    public String getReviewId() {
        return this.reviewId;
    }

    public String getDateCreated() {
        return this.dateCreated;
    }

    public String getContent() {
        return this.content;
    }

    public int getRating() {
        return this.rating;
    }

    public String getQuestId() {
        return this.questId;
    }

    public String getUserId() {
        return this.userId;
    }
}
