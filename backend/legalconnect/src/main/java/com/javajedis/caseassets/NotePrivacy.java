package com.javajedis.caseassets;

public enum NotePrivacy {
    PRIVATE("Private"),
    SHARED("Shared");

    private final String displayName;

    NotePrivacy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 