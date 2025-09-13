package com.javajedis.legalconnect.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePictureDTO {
    private String fullPictureUrl;
    private String thumbnailPictureUrl;
    private String publicId;
}
