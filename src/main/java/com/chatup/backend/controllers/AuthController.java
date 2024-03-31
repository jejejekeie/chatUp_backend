package com.chatup.backend.controllers;

import com.chatup.backend.dtos.ChangePasswordDTO;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.dtos.UserLoginDTO;
import com.chatup.backend.dtos.UserRegisterDTO;
import com.chatup.backend.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO registerDTO) {
        if(userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email is already in use");
        }
        User newUser = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .hashPassword(passwordEncoder.encode(registerDTO.getPassword()))
                .build();
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO loginDTO) {
        if(userRepository.findByEmail(loginDTO.getEmail()).isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = userRepository.findByEmail(loginDTO.getEmail()).get();
        if(passwordEncoder.matches(loginDTO.getPassword(), user.getHashPassword())) {
            return ResponseEntity.ok("User logged in successfully");
        } else {
            return ResponseEntity.badRequest().body("Incorrect password");
        }
    }

    @PostMapping("/password/{email}")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO passwordDTO, @PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();

        user.setHashPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }
}
