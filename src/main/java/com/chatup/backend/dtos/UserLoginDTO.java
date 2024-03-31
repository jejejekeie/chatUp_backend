package com.chatup.backend.dtos;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document
public class UserLoginDTO {
    @Indexed(unique = true)
    private String email;
    private String password;
}
