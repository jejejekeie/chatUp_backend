package com.chatup.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UploadImageResponseDTO {
    private String message;
    private String fileId;

    public UploadImageResponseDTO(String message, String fileId) {
        this.message = message;
        this.fileId = fileId;
    }
}

