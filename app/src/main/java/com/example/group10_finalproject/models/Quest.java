package com.example.group10_finalproject.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Quest {
    private String questId;
    private LocalDateTime dateCreated;
    private LocalDateTime datePublished;
    private String description;
    private String roughLocation;
    private String title;
    private String creatorId;
    private Status status;
    private List<QuestLocation> locations;

    public Quest(String description, String roughLocation, String title,
                 String creatorId, Status status, List<QuestLocation> locations) {
        this.questId = UUID.randomUUID().toString();
        this.dateCreated = LocalDateTime.now();
        this.description = description;
        this.roughLocation = roughLocation;
        this.title = title;
        this.creatorId = creatorId;
        this.status = status;
        this.locations = locations;
    }

    public String getQuestId() { return this.questId; }

    public LocalDateTime getDateCreated() { return this.dateCreated; }

    public LocalDateTime getDatePublished() { return this.datePublished; }

    public String getDescription() { return this.description; }

    public String getRoughLocation() { return this.roughLocation; }

    public String getTitle() { return this.title; }

    public String getCreatorId() { return this.creatorId; }

    public Status getStatus() { return this.status; }

    public List<QuestLocation> getLocations() { return this.locations; }

}
