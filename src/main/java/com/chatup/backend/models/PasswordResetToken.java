package com.chatup.backend.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "PasswordResetToken")
public class PasswordResetToken {
    @Id
    private String id;
    private String token;
    private String email;
    private Date expirationDate;
}
