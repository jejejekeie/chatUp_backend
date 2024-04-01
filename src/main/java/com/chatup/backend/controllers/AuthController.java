package com.chatup.backend.controllers;

import com.chatup.backend.component.JwtUtil;
import com.chatup.backend.dtos.ChangePasswordDTO;
import com.chatup.backend.models.AuthenticationResponse;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.dtos.UserLoginDTO;
import com.chatup.backend.dtos.UserRegisterDTO;
import com.chatup.backend.models.User;
import com.chatup.backend.services.CustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService userDetailsService;
    private final JwtUtil jwtTokenUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailService userDetailsService, JwtUtil jwtTokenUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
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
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO authenticationRequest) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getEmail());

        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
        /*
        if(userRepository.findByEmail(authenticationRequest.getEmail()).isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = userRepository.findByEmail(authenticationRequest.getEmail()).get();
        if(passwordEncoder.matches(authenticationRequest.getPassword(), user.getHashPassword())) {
            return ResponseEntity.ok("User logged in successfully");
        } else {
            return ResponseEntity.badRequest().body("Incorrect password");
        }
         */
    }

    @PostMapping("/password/{email}")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO passwordDTO, @PathVariable String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = userOptional.get();

        user.setHashPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));

        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }
}
