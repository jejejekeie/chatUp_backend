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
        } else if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            User userDb = userRepository.findById(userId).get();
            UserDTO userDto = convertToDTO(userDb);
            return ResponseEntity.ok(new User(userDb));
        }
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = new ArrayList<>();
        for (User user : users) {
            UserDTO dto = UserDTO.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fotoPerfil(user.getFotoPerfil())
                    .status(user.getStatus())
                    .build();
            userDTOS.add(dto);
        }
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
