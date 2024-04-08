package com.chatup.backend.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class NotificationResponse {
    private int status;
    private String message;
}
