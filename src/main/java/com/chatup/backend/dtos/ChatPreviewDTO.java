package com.chatup.backend.dtos;

import com.chatup.backend.models.Mensaje;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatPreviewDTO {
    private String chatId;
    private String chatName;
    private String chatType;
    private Mensaje lastMessage;
}
