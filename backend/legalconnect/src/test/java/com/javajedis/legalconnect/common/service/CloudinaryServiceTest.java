package com.javajedis.legalconnect.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import com.javajedis.legalconnect.user.ProfilePictureDTO;

class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private Url url;

    private CloudinaryService cloudinaryService;

    private final String cloudName = "test-cloud-name";
    private final String apiKey = "test-api-key";
    private final String apiSecret = "test-api-secret";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cloudinaryService = new CloudinaryService(cloudName, apiKey, apiSecret);
        ReflectionTestUtils.setField(cloudinaryService, "cloudinary", cloudinary);
    }

    @Test
    void testUploadProfilePictureSuccess() throws IOException {
        // Arrange
        String userId = "test-user-123";
        byte[] fileContent = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_test-user-123.jpg");
        uploadResult.put("public_id", "profile_pictures/user_test-user-123");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileContent), anyMap())).thenReturn(uploadResult);
        when(cloudinary.url()).thenReturn(url);
        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate("profile_pictures/user_test-user-123"))
            .thenReturn("https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_test-user-123.jpg");

        // Act
        ProfilePictureDTO result = cloudinaryService.uploadProfilePicture(file, userId);

        // Assert
        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_test-user-123.jpg", result.getFullPictureUrl());
        assertEquals("https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_test-user-123.jpg", result.getThumbnailPictureUrl());
        assertEquals("profile_pictures/user_test-user-123", result.getPublicId());

        verify(uploader).upload(eq(fileContent), anyMap());
        verify(url).transformation(any(Transformation.class));
        verify(url).secure(true);
        verify(url).generate("profile_pictures/user_test-user-123");
    }

    @Test
    void testUploadProfilePictureIOException() throws IOException {
        // Arrange
        String userId = "test-user-456";
        byte[] fileContent = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileContent), anyMap())).thenThrow(new IOException("Upload failed"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            cloudinaryService.uploadProfilePicture(file, userId);
        });

        assertEquals("Failed to upload image to Cloudinary", exception.getMessage());
        verify(uploader).upload(eq(fileContent), anyMap());
    }

    @Test
    void testDeleteProfilePictureSuccess() throws IOException {
        // Arrange
        String publicId = "profile_pictures/user_test-delete";

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(new HashMap<>());

        // Act
        cloudinaryService.deleteProfilePicture(publicId);

        // Assert
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void testDeleteProfilePictureWithNullPublicId() {
        // Arrange
        String publicId = null;

        // Act
        cloudinaryService.deleteProfilePicture(publicId);

        // Assert - No exception should be thrown, and uploader should not be called
        verify(cloudinary, org.mockito.Mockito.never()).uploader();
    }

    @Test
    void testDeleteProfilePictureWithEmptyPublicId() {
        // Arrange
        String publicId = "";

        // Act
        cloudinaryService.deleteProfilePicture(publicId);

        // Assert - No exception should be thrown, and uploader should not be called
        verify(cloudinary, org.mockito.Mockito.never()).uploader();
    }

    @Test
    void testDeleteProfilePictureIOException() throws IOException {
        // Arrange
        String publicId = "profile_pictures/user_test-error";

        when(cloudinary.uploader()).thenReturn(uploader);
        doThrow(new IOException("Delete failed")).when(uploader).destroy(eq(publicId), anyMap());

        // Act - Should not throw exception, just log error
        cloudinaryService.deleteProfilePicture(publicId);

        // Assert
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void testUploadProfilePictureWithTransformationChaining() throws IOException {
        // Arrange
        String userId = "test-user-transformation";
        byte[] fileContent = "test image content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", fileContent);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_test-user-transformation.jpg");
        uploadResult.put("public_id", "profile_pictures/user_test-user-transformation");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileContent), anyMap())).thenReturn(uploadResult);
        when(cloudinary.url()).thenReturn(url);
        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate("profile_pictures/user_test-user-transformation"))
            .thenReturn("https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_test-user-transformation.jpg");

        // Act
        ProfilePictureDTO result = cloudinaryService.uploadProfilePicture(file, userId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getFullPictureUrl());
        assertNotNull(result.getThumbnailPictureUrl());
        assertNotNull(result.getPublicId());
        
        // Verify transformation was called with proper parameters
        verify(url).transformation(any(Transformation.class));
        verify(url).secure(true);
    }

    @Test
    void testUploadProfilePictureWithDifferentFileTypes() throws IOException {
        // Test PNG file
        String userId = "test-user-png";
        byte[] fileContent = "test png content".getBytes();
        MultipartFile pngFile = new MockMultipartFile("file", "test.png", "image/png", fileContent);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_test-user-png.png");
        uploadResult.put("public_id", "profile_pictures/user_test-user-png");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileContent), anyMap())).thenReturn(uploadResult);
        when(cloudinary.url()).thenReturn(url);
        when(url.transformation(any(Transformation.class))).thenReturn(url);
        when(url.secure(true)).thenReturn(url);
        when(url.generate("profile_pictures/user_test-user-png"))
            .thenReturn("https://res.cloudinary.com/test/image/upload/w_150,h_150,c_fill,g_face/profile_pictures/user_test-user-png.png");

        // Act
        ProfilePictureDTO result = cloudinaryService.uploadProfilePicture(pngFile, userId);

        // Assert
        assertNotNull(result);
        assertEquals("https://res.cloudinary.com/test/image/upload/v1234567890/profile_pictures/user_test-user-png.png", result.getFullPictureUrl());
        assertEquals("profile_pictures/user_test-user-png", result.getPublicId());
    }
}
