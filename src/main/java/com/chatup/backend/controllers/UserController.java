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

    //region Contacts
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/addContact")
    public ResponseEntity<?> AddContact(@RequestParam("userEmail") String userEmail, @RequestParam("contactsEmail") String contactEmail) {
        if (userEmail == null || contactEmail == null) {
            return ResponseEntity.badRequest().body("Invalid request: userEmail or contactEmail is null");
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();
        user.getContacts().add(contactEmail);
        userRepository.save(user);

        return ResponseEntity.ok("Contact added successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/deleteContact/")
    public ResponseEntity<?> DeleteContact(@RequestParam("contactEmail") String contactEmail, @RequestParam("userEmail") String userEmail) {
        if (userEmail == null || contactEmail == null) {
            return ResponseEntity.badRequest().body("Invalid request: userEmail or contactEmail is null");
        }

        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();
        user.getContacts().remove(contactEmail);
        userRepository.save(user);

        return ResponseEntity.ok("Contact deleted successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/contacts/{email}")
    public ResponseEntity<?> getContacts(@PathVariable String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request: Email is null or empty");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        return ResponseEntity.ok(user.getContacts());
    }
    //endregion

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

