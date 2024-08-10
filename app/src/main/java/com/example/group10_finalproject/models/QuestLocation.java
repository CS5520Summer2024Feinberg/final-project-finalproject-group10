package com.example.group10_finalproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class QuestLocation implements Parcelable {
    private String locationId;
    private String imageId;
    private double latitude;
    private double longitude;
    private String address;
    private String name;
    private String description;
    private String specialNotes;


    public QuestLocation() {

    }

    protected QuestLocation(Parcel in) {
        locationId = in.readString();
        imageId = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        address = in.readString();
        name = in.readString();
        description = in.readString();
        specialNotes = in.readString();
    }

    public QuestLocation(String address, double longitude, double latitude, String name,
                         String description, String specialNotes, String imageId) {
        this.locationId = UUID.randomUUID().toString();
        this.imageId = imageId;
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
        dest.writeString(imageId);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(specialNotes);
    }

    public String getLocationId() { return this.locationId; }

    public double getLatitude() { return this.latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return this.longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return this.address; }

    public void setAddress(String address) { this.address = address; }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return this.description; }

    public void setDescription(String description) { this.description = description; }

    public String getImageId() { return this.imageId; }

    public String getSpecialNotes() { return this.specialNotes; }

}
