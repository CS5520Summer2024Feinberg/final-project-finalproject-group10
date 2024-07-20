package com.example.group10_finalproject.models;

import java.util.UUID;

public class QuestLocation {
    private String locationId;
    private int latitude;
    private int longitude;
    private String address;
    private String name;
    private String descriptionShort;
    private String descriptionFull;
    private String imageId;
    private String specialNotes;

    public QuestLocation(String address, int longitude, int latitude, String name,
                         String descriptionShort, String descriptionFull, String specialNotes) {
        this.locationId = UUID.randomUUID().toString();
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.descriptionShort = descriptionShort;
        this.descriptionFull = descriptionFull;
        this.specialNotes = specialNotes;
    }

    public String getLocationId() { return this.locationId; }

    public int getLatitude() { return this.latitude; }

    public int getLongitude() { return this.longitude; }

    public String getAddress() { return this.address; }

    public String getName() { return this.name; }

    public String getDescriptionShort() { return this.descriptionShort; }

    public String getDescriptionFull() { return this.descriptionFull; }

    public String getImageId() { return this.imageId; }

    public String getSpecialNotes() { return this.specialNotes; }

}
