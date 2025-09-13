package com.javajedis.legalconnect.lawyer.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.lawyer.enums.VerificationStatus;
import com.javajedis.legalconnect.user.ProfilePictureDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerInfoDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
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
    private BigDecimal hourlyCharge;
    private Boolean completeProfile;
    private ProfilePictureDTO profilePicture;
    
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
    
    public String getHourlyChargeDisplayText() {
        return hourlyCharge != null ? hourlyCharge.toString() : null;
    }
    
    public String getCompleteProfileDisplayText() {
        if (completeProfile == null) {
            return "Unknown";
        }
        return completeProfile ? "Complete" : "Incomplete";
    }
} 

