package com.chatup.backend.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document
public class NotificationRequest {
    private String title;
    private String body;
    private String topic;
    private String token;
}
