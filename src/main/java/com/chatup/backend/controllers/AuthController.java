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
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String ADMIN_REGISTRATION_CODE = "SecretAdminCode123";

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailService userDetailsService;
    private final JwtUtil jwtTokenUtil;
    private final PasswordResetService passwordResetService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, CustomUserDetailService userDetailsService, JwtUtil jwtTokenUtil, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordResetService = passwordResetService;
    }

    //region Login/signup
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDTO registerDTO) {
        if (registerDTO.getUsername() == null || registerDTO.getEmail() == null || registerDTO.getPassword() == null) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        boolean emailExists = userRepository.findByEmail(registerDTO.getEmail()).isPresent();
        boolean usernameExists = userRepository.findByUsername(registerDTO.getUsername()).isPresent();
        if (emailExists || usernameExists) {
            return ResponseEntity.badRequest().body("Email or Username is already in use");
        }

        User newUser = User.builder()
                .username(registerDTO.getUsername())
                .email(registerDTO.getEmail())
                .hashPassword(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Collections.singleton(
                        Optional.ofNullable(registerDTO.getAdminCode())
                                .map(String::trim)
                                .filter(code -> code.equals(ADMIN_REGISTRATION_CODE))
                                .map(code -> User.UserRoles.ADMIN)
                                .orElse(User.UserRoles.USER)
                ))
                .build();
        logger.info("Registering user with username: {}, email: {}", registerDTO.getUsername(), registerDTO.getEmail());
        logger.info("Received admin code: '{}'", registerDTO.getAdminCode());
        logger.info("Assigned roles: {}", newUser.getRole().toString());

        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDTO authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(), authenticationRequest.getPassword())
            );
            final UserDetails userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getEmail());

            User user = userRepository.findByEmail(authenticationRequest.getEmail()).get();
            user.setStatus("ACTIVE");
            userRepository.save(user);

            final String jwt = jwtTokenUtil.generateToken(userDetails);
            final String userId = userRepository.findByEmail(authenticationRequest.getEmail()).get().getId();

            return ResponseEntity.ok(new AuthenticationResponse(jwt, userId));
        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        String username = jwtTokenUtil.extractUsername(token.substring(7));
        User user = userRepository.findByEmail(username).orElse(null);

        if (user != null) {
            user.setStatus("INACTIVE");
            userRepository.save(user);
            return ResponseEntity.ok("Logout successful");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    //endregion

    //region Password management
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
        User user = userRepository.findByEmail(prt.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        user.setHashPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        passwordResetService.invalidate(token);
        return ResponseEntity.ok("Password changed successfully");
    }
    //endregion

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                String username = jwtTokenUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    String newToken = jwtTokenUtil.generateToken(userDetails);
                    return ResponseEntity.ok(new AuthenticationResponse(newToken, userRepository.findByEmail(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found")).getId()));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid token");
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authorization header is missing or invalid");
    }
}
