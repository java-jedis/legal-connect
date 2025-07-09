package com.javajedis.legalconnect.lawyer.enums;

public enum PracticingCourt {
    SUPREME_COURT("Supreme Court"),
    HIGH_COURT_DIVISION("High Court Division"),
    APPELLATE_DIVISION("Appellate Division"),
    DISTRICT_COURT("District Court"),
    SESSIONS_COURT("Sessions Court"),
    ADMINISTRATIVE_TRIBUNAL("Administrative Tribunal"),
    LABOUR_COURT("Labour Court"),
    FAMILY_COURT("Family Court"),
    MAGISTRATE_COURT("Magistrate Court"),
    SPECIAL_TRIBUNAL("Special Tribunal");

    private final String displayName;

    PracticingCourt(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 