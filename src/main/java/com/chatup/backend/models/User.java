package com.chatup.backend.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Usuario")
public class User {
    @Id
    private String id;
    private String username;
    @Indexed(unique = true)
    private String email;
    private String hashPassword;
    private String authorities;
    private String fotoPerfil;
    private String status;
    private Set<UserRoles> role;
    private Date lastAccess;

    public User(User user) {
        this.username = user.username;
        this.email = user.email;
        this.hashPassword = user.hashPassword;
        this.authorities = user.authorities;
        this.fotoPerfil = user.fotoPerfil;
        this.status = user.status;
        this.lastAccess = user.lastAccess;
        this.role = new HashSet<>();
    }

    public enum UserRoles {
        USER,
        ADMIN
    }
}
