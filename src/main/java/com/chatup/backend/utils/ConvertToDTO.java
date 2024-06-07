package com.chatup.backend.utils;

import com.chatup.backend.dtos.UserDTO;
import com.chatup.backend.models.User;

import java.util.stream.Collectors;

public class ConvertToDTO {
    public static UserDTO convertToDTO(User user, boolean includeRoles) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fotoPerfil(user.getFotoPerfil())
                .status(user.getStatus());
        if (includeRoles) {
            builder.role(user.getRole().stream().map(Enum::name).collect(Collectors.joining(",")));
        }
        return builder.build();
    }
}
