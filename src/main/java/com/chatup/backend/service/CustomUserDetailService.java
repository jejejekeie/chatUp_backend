package com.chatup.backend.service;

import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if(userRepository.findByEmail(email).isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        } else {
            String authorities = "USER";
            User user = userRepository.findByEmail(email).get();
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getHashPassword())
                    .roles(authorities)
                    .build();
        }
    }
}
