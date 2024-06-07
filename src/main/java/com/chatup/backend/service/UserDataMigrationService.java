package com.chatup.backend.service;

import com.chatup.backend.repositories.UserRepository;
import com.chatup.backend.models.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDataMigrationService {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void assignDefaultRoleToUsers() {
        List<User> usersWithoutRole = userRepository.findAll().stream()
                .filter(user -> user.getRole() == null || user.getRole().isEmpty())
                .collect(Collectors.toList());

        usersWithoutRole.forEach(user -> {
            user.setRole(Collections.singleton(User.UserRoles.USER));
            userRepository.save(user);
        });

        System.out.println("Updated " + usersWithoutRole.size() + " users with default USER role.");
    }
}
