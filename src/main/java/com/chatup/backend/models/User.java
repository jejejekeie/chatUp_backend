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
    private Date lastAccess;
    private Set<String> contacts = new HashSet<>();

    public User(User userDb) {
        this.authorities = "ROLE_USER";
        this.status = "ACTIVE";
        this.lastAccess = new Date();
        this.contacts = new HashSet<>();
    }
}
