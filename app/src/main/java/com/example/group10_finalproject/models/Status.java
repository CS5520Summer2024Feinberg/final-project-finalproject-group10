package com.example.group10_finalproject.models;

public enum Status {
    DRAFT("draft"),
    PUBLISHED("published");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
