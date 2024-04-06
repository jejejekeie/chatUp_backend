package com.chatup.backend.controllers;

import com.chatup.backend.models.User;
import com.chatup.backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
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
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        userRepository.delete(userOptional.get());
        return ResponseEntity.ok("User deleted successfully");
    }

    private String StoreProfilePicture(MultipartFile fotoPerfil) {
        if (fotoPerfil.isEmpty()) {
            return "";
        }
        try {
            Path path = Paths.get("src/main/resources/static/images/profile");
            Files.copy(fotoPerfil.getInputStream(), path.resolve(Objects.requireNonNull(fotoPerfil.getOriginalFilename())));
            return fotoPerfil.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(
            @RequestParam("username") String username,
            @RequestParam("fotoPerfil") MultipartFile fotoPerfil,
            @RequestParam("email") String email,
            @PathVariable String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        } else {
            User user = userOptional.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setFotoPerfil(StoreProfilePicture(fotoPerfil));
            userRepository.save(user);
            return ResponseEntity.ok("User updated successfully");
        }
    }
}
