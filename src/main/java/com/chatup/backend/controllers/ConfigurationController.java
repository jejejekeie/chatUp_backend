package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UpdateUserDTO;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
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

    private final UserRepository userRepository;
    private final ImageService imageService;

    public ConfigurationController(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
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

    @GetMapping("/image/{fileId}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileId) {
        return Optional.ofNullable(imageService.loadImage(fileId))
                .map(file -> ResponseEntity.ok().contentType(MediaType.parseMediaType("image/jpeg")).body(file))
                .orElse(ResponseEntity.notFound().build());
    }

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
    }

    @PostMapping("/uploadImage/{userId}")
    public ResponseEntity<String> uploadImage(@PathVariable String userId, @RequestParam("file") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String fileId = imageService.storeImage(userId, file);
                return ResponseEntity.ok("Image uploaded successfully with ID: " + fileId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
            }
        } catch (IOException e) {
            logger.error("Error uploading image for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
