package com.chatup.backend.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ChatCreationRequest {
    private String chatName;
    private Set<String> members;
    private String chatType;
}
