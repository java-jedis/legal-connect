package com.javajedis.legalconnect.notifications;

public enum NotificationType {
    CASE_CREATE("Case Creation"),
    EVENT_ADD("Event Addition"),
    SCHEDULE_REMINDER("Schedule Reminder"),
    DOC_UPLOAD("Document Upload"),
    NOTE_CREATE("Note Creation");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 