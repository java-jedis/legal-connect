package com.javajedis.legalconnect.common.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.javajedis.legalconnect.user.ProfilePictureDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.cloud-name}") String cloudName,
                           @Value("${cloudinary.api-key}") String apiKey,
                           @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public ProfilePictureDTO uploadProfilePicture(MultipartFile file, String userId) throws IOException {
        try {
            // Upload original image
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "profile_pictures",
                    "public_id", "user_" + userId,
                    "overwrite", true,
                    "quality", "auto:good",
                    "fetch_format", "auto"
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String fullPictureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // Generate thumbnail URL using Cloudinary's transformation
            String thumbnailUrl = cloudinary.url()
                    .transformation(new Transformation<>()
                            .width(150)
                            .height(150)
                            .crop("fill")
                            .gravity("face")
                            .quality("auto:good")
                            .fetchFormat("auto"))
                    .secure(true)
                    .generate(publicId);

            return new ProfilePictureDTO(fullPictureUrl, thumbnailUrl, publicId);

        } catch (IOException e) {
            log.error("Error uploading image to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary", e);
        }
    }

    public void deleteProfilePicture(String publicId) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Successfully deleted image with public_id: {}", publicId);
            }
        } catch (IOException e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage(), e);
        }
    }

}
