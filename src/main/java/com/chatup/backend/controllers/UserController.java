package com.chatup.backend.controllers;

import com.chatup.backend.dtos.UserDTO;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import com.chatup.backend.utils.ConvertToDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final Logger logger = Logger.getLogger(UserController.class.getName());

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
                .map(user -> {
                    UserDTO dto = ConvertToDTO.convertToDTO(user, false);
                    logger.info("Sending UserDTO: " + dto.toString());
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        List<User> users = userRepository.findByUsernameContainingOrEmailContaining(query, query);
        if (users.isEmpty()) {
            return ResponseEntity.ok("No users found matching your search criteria.");
        }
        List<UserDTO> userDTOS = users.stream()
                .map(user -> ConvertToDTO.convertToDTO(user, false))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOS);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOS = users.stream()
                .map(user -> ConvertToDTO.convertToDTO(user, true))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOS);
    }
}
