package com.chatup.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data

@Builder
@Document(collection = "Chat")
public class Chat {
    @Id
    private String id;
    private String chatId;
    private String name;
    private Set<String> members;
    private String chatType;
}
