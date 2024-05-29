package com.chatup.backend.dtos;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "Usuario")
public class UpdateUserDTO {
    private Optional<String> username = Optional.empty();
    private Optional<String> email = Optional.empty();
}
