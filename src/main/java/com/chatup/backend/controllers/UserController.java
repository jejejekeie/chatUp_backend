package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UserDTO;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request: userId is null");
        }
        return userRepository.findById(userId)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        List<User> users = userRepository.findByUsernameContainingOrEmailContaining(query, query);
        List<UserDTO> userDTOS = users.stream()
                .map(user -> new UserDTO(user.getId(),user.getUsername(), user.getEmail(), user.getFotoPerfil(), user.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOS);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOS);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fotoPerfil(user.getFotoPerfil())
                .status(user.getStatus())
                .build();
    }
}
