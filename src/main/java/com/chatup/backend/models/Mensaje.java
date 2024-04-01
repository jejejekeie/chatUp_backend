package com.chatup.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Mensaje")
public class Mensaje {
    @Id
    private String id;
    private String chatId;
    private String sender;
    private String content;
    private Date timestamp;
    private String imageURL;
}
