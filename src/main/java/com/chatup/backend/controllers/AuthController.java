package com.chatup.backend.controllers;

import com.chatup.backend.utils.JwtUtil;
import com.chatup.backend.models.AuthenticationResponse;
import com.chatup.backend.models.PasswordResetToken;
import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.dtos.UserLoginDTO;
import com.chatup.backend.dtos.UserRegisterDTO;
import com.chatup.backend.models.User;
import com.chatup.backend.service.CustomUserDetailService;
import com.chatup.backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService userDetailsService;
    private final JwtUtil jwtTokenUtil;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailService userDetailsService, JwtUtil jwtTokenUtil, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordResetService = passwordResetService;
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
        final String userId = userRepository.findByEmail(authenticationRequest.getEmail()).get().getId();

        return ResponseEntity.ok(new AuthenticationResponse(jwt, userId));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        passwordResetService.generatePasswordResetToken(email);
        return ResponseEntity.ok("Password reset link sent to email");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam("token") String token,
                                            @RequestParam("password") String password) {
        if (!passwordResetService.validatePasswordResetToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        PasswordResetToken prt = passwordResetService.getTokenDetails(token);
        User user = userRepository.findByEmail(prt.getEmail()).get();
        user.setHashPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        passwordResetService.invalidate(token);
        return ResponseEntity.ok("Password changed successfully");
    }
}
