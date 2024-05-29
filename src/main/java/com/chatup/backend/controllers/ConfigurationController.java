package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UpdateUserDTO;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.service.ImageService;
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

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, UpdateUserDTO updateUserDTO,
                                        @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil) {
        return userRepository.findById(userId)
                .map(user -> {
                    try {
                        updateUserData(user, updateUserDTO, fotoPerfil);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    userRepository.save(user);
                    return ResponseEntity.ok("User updated successfully");
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private void updateUserData(User user, UpdateUserDTO updateUserDTO, MultipartFile fotoPerfil) throws IOException {
        if (updateUserDTO.getUsername() != null) user.setUsername(updateUserDTO.getUsername());
        if (updateUserDTO.getEmail() != null) user.setEmail(updateUserDTO.getEmail());
        if (fotoPerfil != null && !fotoPerfil.isEmpty() && imageService.isValidFile(fotoPerfil)) {
            String fotoUrl = imageService.storeImage(user.getId(), fotoPerfil);
            user.setFotoPerfil(fotoUrl);
        } else if (fotoPerfil != null && !imageService.isValidFile(fotoPerfil)) {
            throw new IllegalArgumentException("Invalid file type or size");
        }
    }
}
