package com.javajedis.legalconnect.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProfilePictureDTOTest {

    @Test
    void testNoArgsConstructor() {
        ProfilePictureDTO dto = new ProfilePictureDTO();
        assertNull(dto.getFullPictureUrl());
        assertNull(dto.getThumbnailPictureUrl());
        assertNull(dto.getPublicId());
    }

    @Test
    void testAllArgsConstructor() {
        String fullUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_123.jpg";
        String thumbnailUrl = "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_123.jpg";
        String publicId = "profile_pictures/user_123";

        ProfilePictureDTO dto = new ProfilePictureDTO(fullUrl, thumbnailUrl, publicId);

        assertEquals(fullUrl, dto.getFullPictureUrl());
        assertEquals(thumbnailUrl, dto.getThumbnailPictureUrl());
        assertEquals(publicId, dto.getPublicId());
    }

    @Test
    void testGettersSetters() {
        ProfilePictureDTO dto = new ProfilePictureDTO();
        
        String fullUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_456.jpg";
        String thumbnailUrl = "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_456.jpg";
        String publicId = "profile_pictures/user_456";

        dto.setFullPictureUrl(fullUrl);
        dto.setThumbnailPictureUrl(thumbnailUrl);
        dto.setPublicId(publicId);

        assertEquals(fullUrl, dto.getFullPictureUrl());
        assertEquals(thumbnailUrl, dto.getThumbnailPictureUrl());
        assertEquals(publicId, dto.getPublicId());
    }

    @Test
    void testEqualsAndHashCode() {
        String fullUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_789.jpg";
        String thumbnailUrl = "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_789.jpg";
        String publicId = "profile_pictures/user_789";

        ProfilePictureDTO dto1 = new ProfilePictureDTO(fullUrl, thumbnailUrl, publicId);
        ProfilePictureDTO dto2 = new ProfilePictureDTO(fullUrl, thumbnailUrl, publicId);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        String fullUrl = "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_999.jpg";
        String thumbnailUrl = "https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_999.jpg";
        String publicId = "profile_pictures/user_999";

        ProfilePictureDTO dto = new ProfilePictureDTO(fullUrl, thumbnailUrl, publicId);
        String toStringResult = dto.toString();

        assertNotNull(toStringResult);
        assertEquals(true, toStringResult.contains("ProfilePictureDTO"));
    }
}
