package com.javajedis.legalconnect.lawyer;

public enum Division {
    DHAKA("Dhaka"),
    CHATTOGRAM("Chattogram"),
    RAJSHAHI("Rajshahi"),
    KHULNA("Khulna"),
    BARISHAL("Barishal"),
    SYLHET("Sylhet"),
    RANGPUR("Rangpur"),
    MYMENSINGH("Mymensingh");

    private final String displayName;

    Division(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 