package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UpdateUserDTO;
import com.chatup.backend.dtos.UploadImageResponseDTO;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.service.ImageService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/configuration")
public class ConfigurationController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);
    private final GridFsTemplate gridFsTemplate;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public ConfigurationController(UserRepository userRepository, ImageService imageService, GridFsTemplate gridFsTemplate) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.gridFsTemplate = gridFsTemplate;
    }

    //region Update/Delete User
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserDTO updateUserDTO) {
        return userRepository.findById(userId)
                .map(user -> {
                    updateUserDTO.getUsername().ifPresent(user::setUsername);
                    updateUserDTO.getEmail().ifPresent(user::setEmail);
                    userRepository.save(user);
                    return ResponseEntity.ok("User updated successfully");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                })
                .orElse(ResponseEntity.notFound().build());
    }
    //endregion

    //region Image Upload/Retrieval
    @GetMapping("/image/{userId}")
    public ResponseEntity<Resource> getImage(@PathVariable String userId) {
        Resource fileResource = imageService.loadImage(userId);
        if (fileResource == null) {
            logger.warn("No image found for user ID: {}", userId);
            return ResponseEntity.notFound().build();
        }

        try {
            String contentType = getResourceContentType(userId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileResource);
        } catch (Exception e) {
            logger.error("Failed to load image for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/uploadImage/{userId}")
    public ResponseEntity<UploadImageResponseDTO> uploadImage(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadImageResponseDTO("File is empty", null));
        }
        try {
            imageService.storeOrUpdateImage(userId, file);
            return ResponseEntity.ok(new UploadImageResponseDTO("Image uploaded successfully", userId));
        } catch (IOException e) {
            logger.error("Error uploading image for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadImageResponseDTO("Error uploading image", null));
        }
    }

    public String getResourceContentType(String userId) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(userId)));
        if (file == null || file.getMetadata() == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return file.getMetadata().get("contentType", String.class);
    }
    //endregion
}
