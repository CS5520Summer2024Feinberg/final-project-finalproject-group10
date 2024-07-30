package com.example.group10_finalproject.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class Quest {
    private String questId;
    private String dateCreated;
    private LocalDateTime datePublished;
    private String description;
    private String roughLocation;
    private String title;
    private String creatorId;
    private Status status;
    private String imageId;
    private List<QuestLocation> locations;

    public Quest() {
        // Firebase requires a no-argument constructor
    }

    public Quest(String description, String roughLocation, String title,
                 String creatorId, Status status, List<QuestLocation> locations) {
        this.questId = UUID.randomUUID().toString();
        this.dateCreated = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.description = description;
        this.roughLocation = roughLocation;
        this.title = title;
        this.creatorId = creatorId;
        this.status = status;
        this.locations = locations;
        this.imageId = "";
    }

    public String getQuestId() { return this.questId; }

    public String getDateCreated() { return this.dateCreated; }

    public LocalDateTime getDatePublished() { return this.datePublished; }

    public String getDescription() { return this.description; }

    public String getRoughLocation() { return this.roughLocation; }

    public String getTitle() { return this.title; }

    public String getCreatorId() { return this.creatorId; }

    public Status getStatus() { return this.status; }

    public List<QuestLocation> getLocations() { return this.locations; }

    public void setLocations(List<QuestLocation> locations) {
        this.locations = locations;
    }

    public void setImageId(String imageId) { this.imageId = imageId; }

    public String getImageId() { return this.imageId; }

}
