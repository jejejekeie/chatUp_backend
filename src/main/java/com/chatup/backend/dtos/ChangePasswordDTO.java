package com.chatup.backend.dtos;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
