package com.javajedis.legalconnect.lawyerdirectory.dto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.javajedis.legalconnect.lawyer.enums.District;
import com.javajedis.legalconnect.lawyer.enums.Division;
import com.javajedis.legalconnect.lawyer.enums.PracticingCourt;
import com.javajedis.legalconnect.lawyer.enums.SpecializationType;
import com.javajedis.legalconnect.user.ProfilePictureDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LawyerSearchResultDTO {
    private UUID lawyerId;
    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String firm;
    private Integer yearsOfExperience;
    private PracticingCourt practicingCourt;
    private Division division;
    private District district;
    private String bio;
    private List<SpecializationType> specializations;
    private Double averageRating;
    private ProfilePictureDTO profilePicture;


    // Utility method to convert specialization CSV to List<SpecializationType>
    public static List<SpecializationType> parseSpecializations(String specializationCsv) {
        if (specializationCsv != null && !specializationCsv.isEmpty()) {
            return Arrays.stream(specializationCsv.split(","))
                    .map(String::trim)
                    .map(SpecializationType::valueOf)
                    .toList();
        }
        return Collections.emptyList();
    }

    public String getPracticingCourtDisplayName() {
        return practicingCourt != null ? practicingCourt.getDisplayName() : null;
    }

    public String getDivisionDisplayName() {
        return division != null ? division.getDisplayName() : null;
    }

    public String getDistrictDisplayName() {
        return district != null ? district.getDisplayName() : null;
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
