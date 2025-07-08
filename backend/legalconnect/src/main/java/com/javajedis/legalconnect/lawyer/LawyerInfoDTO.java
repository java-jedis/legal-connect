package com.javajedis.legalconnect.lawyer;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerInfoDTO {
    private String firm;
    private Integer yearsOfExperience;
    private String barCertificateNumber;
    private PracticingCourt practicingCourt;
    private Division division;
    private District district;
    private String bio;
    private VerificationStatus verificationStatus;
    private OffsetDateTime lawyerCreatedAt;
    private OffsetDateTime lawyerUpdatedAt;
    private List<SpecializationType> specializations;
    
    public String getPracticingCourtDisplayName() {
        return practicingCourt != null ? practicingCourt.getDisplayName() : null;
    }
    
    public String getDivisionDisplayName() {
        return division != null ? division.getDisplayName() : null;
    }
    
    public String getDistrictDisplayName() {
        return district != null ? district.getDisplayName() : null;
    }
    
    public String getVerificationStatusDisplayName() {
        return verificationStatus != null ? verificationStatus.getDisplayName() : null;
    }
    
    public List<String> getSpecializationDisplayNames() {
        if (specializations == null) {
            return List.of();
        }
        return specializations.stream()
                .map(SpecializationType::getDisplayName)
                .toList();
    }
} 