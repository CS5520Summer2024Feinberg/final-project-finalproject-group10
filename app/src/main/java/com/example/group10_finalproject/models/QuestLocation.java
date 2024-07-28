package com.example.group10_finalproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class QuestLocation implements Parcelable {
    private String locationId;
    private double latitude;
    private double longitude;
    private String address;
    private String name;
    private String description;
    private String imageId;
    private String specialNotes;


    public QuestLocation() {
        this.locationId = "";
        this.address = "";
        this.longitude = 0;
        this.latitude = 0;
        this.name = "";
        this.description = "";
        this.specialNotes = "";
    }

    protected QuestLocation(Parcel in) {
        locationId = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        name = in.readString();
        description = in.readString();
        imageId = in.readString();
        specialNotes = in.readString();
    }

    public QuestLocation(String address, double longitude, double latitude, String name,
                         String description, String specialNotes) {
        this.locationId = UUID.randomUUID().toString();
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
        this.description = description;
        this.specialNotes = specialNotes;
    }

    public static final Creator<QuestLocation> CREATOR = new Creator<QuestLocation>() {
        @Override
        public QuestLocation createFromParcel(Parcel source) {
            return new QuestLocation(source);
        }

        @Override
        public QuestLocation[] newArray(int size) {
            return new QuestLocation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(locationId);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageId);
        dest.writeString(specialNotes);
    }

    public String getLocationId() { return this.locationId; }

    public double getLatitude() { return this.latitude; }

    public double getLongitude() { return this.longitude; }

    public String getAddress() { return this.address; }

    public String getName() { return this.name; }

    public String getDescription() { return this.description; }

    public String getImageId() { return this.imageId; }

    public String getSpecialNotes() { return this.specialNotes; }

}
