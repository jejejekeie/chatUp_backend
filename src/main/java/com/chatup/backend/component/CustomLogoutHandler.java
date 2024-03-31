package com.chatup.backend.component;

import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setLastAccess(new Date());
                userRepository.save(user);
            }
        }
    }
}
