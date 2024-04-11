package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UpdateUserDTO;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    private final UserRepository userRepository;

    public ConfigurationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request: email is null");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userRepository.delete(userOptional.get());
        return ResponseEntity.ok("User deleted successfully");
    }

    private String storeProfilePicture(MultipartFile fotoPerfil) {
        if (fotoPerfil.isEmpty()) {
            return "";
        }
        try {
            Path path = Paths.get("src/main/resources/static/images/profile");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Path filePath = path.resolve(Objects.requireNonNull(fotoPerfil.getOriginalFilename()));
            Files.copy(fotoPerfil.getInputStream(), filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            UpdateUserDTO updateUserDTO,
            @RequestParam("fotoPerfil") MultipartFile fotoPerfil) {

        User user = userRepository.findById(userId).orElseThrow(()
                -> new UsernameNotFoundException("User not found"));

        if (updateUserDTO.getUsername() != null) {
            user.setUsername(updateUserDTO.getUsername());
        }
        if (updateUserDTO.getStatus() != null) {
            user.setStatus(updateUserDTO.getStatus());
        }
        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            String fotoUrl = storeProfilePicture(fotoPerfil);
            user.setFotoPerfil(fotoUrl);
        }
        userRepository.save(user);
        return ResponseEntity.ok("User updated successfully");
    }
}
