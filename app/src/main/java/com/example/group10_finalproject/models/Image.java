package com.example.group10_finalproject.models;

import android.net.Uri;
import java.util.UUID;

public class Image {

    private String imageId;
    private String userId;
    private String filePath;
    private Uri fileUri;

    public Image() {

    }

    public Image(String userId, String filePath, Uri fileUri) {
        this.imageId = UUID.randomUUID().toString();
        this.userId = userId;
        this.filePath = filePath;
        this.fileUri = fileUri;
    }

    public String getImageId() {
        return imageId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFilePath() {
        return filePath;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }
}
