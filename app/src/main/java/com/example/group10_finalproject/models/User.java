package com.example.group10_finalproject.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private String userId;
    private String email;
    private String username;
    private String password;
    private LocalDateTime dateCreated;
    private String currentQuest;
    private List<String> media;
    private List<String> completedQuests;
    private List<String> createdQuests;

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.dateCreated = LocalDateTime.now();
        this.userId = UUID.randomUUID().toString();
        this.completedQuests = new ArrayList<>();
        this.createdQuests = new ArrayList<>();
        this.media = new ArrayList<>();
    }

    public String getUserId() { return this.userId; }

    public String getEmail() { return this.email; }

    public String getUsername() { return this.username; }

    public String getPassword() { return this.password; }

    public LocalDateTime getDateCreated() { return this.dateCreated; }

    public String getCurrentQuest() {
        return this.currentQuest;
    }

    public void setCurrentQuest(String quest) {
        this.currentQuest = quest;
    }

    public List<String> getCompletedQuests() { return this.completedQuests; }

    public void addCompletedQuest(String quest) {
        this.completedQuests.add(quest);
    }

    public List<String> getCreatedQuests() { return this.createdQuests; }

    public void addCreatedQuest(String quest) {
        this.createdQuests.add(quest);
    }

    public List<String> getMedia() { return this.media; }

    public void addMedia(String newMedia) {
        this.media.add(newMedia);
    }
}
