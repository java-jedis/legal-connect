package com.javajedis.legalconnect.lawyer.enums;

public enum SpecializationType {
    CRIMINAL_LAW("Criminal Law"),
    CIVIL_LAW("Civil Law"),
    FAMILY_LAW("Family Law"),
    LABOUR_LAW("Labour Law"),
    CORPORATE_LAW("Corporate Law"),
    CONSTITUTIONAL_LAW("Constitutional Law"),
    TAX_LAW("Tax Law"),
    ENVIRONMENTAL_LAW("Environmental Law"),
    INTELLECTUAL_PROPERTY_LAW("Intellectual Property Law"),
    BANKING_LAW("Banking and Finance Law"),
    PROPERTY_LAW("Property and Real Estate Law"),
    HUMAN_RIGHTS_LAW("Human Rights Law"),
    INTERNATIONAL_LAW("International Law"),
    CYBER_LAW("Cyber and ICT Law"),
    CONTRACT_LAW("Contract Law"),
    ADMINISTRATIVE_LAW("Administrative Law"),
    IMMIGRATION_LAW("Immigration Law"),
    CONSUMER_LAW("Consumer Protection Law"),
    INSURANCE_LAW("Insurance Law"),
    MARITIME_LAW("Maritime Law"),
    EDUCATION_LAW("Education Law");

    private final String displayName;

    SpecializationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 