package com.javajedis.legalconnect.caseassets;

public enum AssetPrivacy {
    PRIVATE("Private"),
    SHARED("Shared");

    private final String displayName;

    AssetPrivacy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 