package com.javajedis.legalconnect.casemanagement;

public enum CaseStatus {
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved");

    private final String displayName;

    CaseStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 