package com.chatup.backend.dtos;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "users")
public class UpdateUserDTO {
    String username;
    String status;
    String fotoPerfil;
}
