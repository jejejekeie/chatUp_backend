package com.chatup.backend.controllers;

import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request: email is null");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        userRepository.delete(userOptional.get());
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/update/{email}")
    public ResponseEntity<?> updateUser(@RequestParam("username") String username, @RequestParam("fotoPerfil") MultipartFile fotoPerfil, @PathVariable String email) {
        if (email == null) {
            return ResponseEntity.badRequest().body("User not found");
        } else {
            String fileName = StoreProfilePicture(fotoPerfil);
            userRepository.save(User.builder()
                .username(username)
                .fotoPerfil(fileName)
                .build());
        return ResponseEntity.ok("User updated successfully");
        }
    }

    @GetMapping("/info/{email}")
    public ResponseEntity<?> getUser(@PathVariable String email) {
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid request: email is null");
        } else if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(userRepository.findByEmail(email));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

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

    @GetMapping("/contacts/{email}")
    public ResponseEntity<?> getContacts(@PathVariable String email) {
        if (email == null) {
            return ResponseEntity.badRequest().body("User not found");
        } else {
            User user = userRepository.findByEmail(email).get();
            return ResponseEntity.ok(user.getContacts());
        }
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
}
