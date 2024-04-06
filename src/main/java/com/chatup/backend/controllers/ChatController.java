package com.chatup.backend.controllers;

import com.chatup.backend.models.Chat;
import com.chatup.backend.models.ChatCreationRequest;
import com.chatup.backend.models.Mensaje;
import com.chatup.backend.models.User;
import com.chatup.backend.services.ChatService;
import com.chatup.backend.services.ImageService;
import com.chatup.backend.services.MensajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeService messageService;
    private final ChatService chatService;
    private final ImageService imageService;

    @Value("${file.upload-allowed-mimetypes}")
    private String[] allowedMimeTypes;

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat")
    public void processMessage(Mensaje chatMensaje) {
        Mensaje msjGuardado = messageService.save(chatMensaje);
        messagingTemplate.convertAndSendToUser(
                chatMensaje.getSender(), "/queue/messages",
                msjGuardado
        );
    }

    @PreAuthorize("isAuthenticated()")
    @MessageMapping("/chat/{chatId}")
    public void processMessage(@DestinationVariable String chatId, Mensaje chatMensaje) {
        Mensaje msjGuardado = messageService.save(chatMensaje);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, msjGuardado);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mensajes/{chatId}/{page}/{size}")
    public ResponseEntity<List<Mensaje>> getMensajesChatPaginated(
            @PathVariable String chatId,
            @PathVariable int page,
            @PathVariable int size) {
        Page<Mensaje> mensajes = messageService.findMensajesChatPageable(chatId, page, size);
        return ResponseEntity.ok(mensajes.getContent());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mensajes/{chatId}")
    public ResponseEntity<List<Mensaje>> getMensajesChat(
            @PathVariable String chatId
    ) {
        return ResponseEntity.ok(messageService.findMensajesChat(chatId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chats")
    public ResponseEntity<Chat> createChat(@RequestBody ChatCreationRequest request) {
        if (!membersAreValid(request.getMembers())) {
            ResponseEntity.badRequest().body("Lista de miembros inválida.");
        }
        Chat chat = Chat.builder()
                .name(request.getChatName())
                .members(request.getMembers())
                .chatType(request.getChatType())
                .build();
        return ResponseEntity.ok(chatService.createChat(chat));
    }

    private boolean membersAreValid(Set<String> members) {
        return members != null && members.size() > 1;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/uploadImage")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile image) {
        if (!isMimeTypeAllowed(image.getContentType())) {
            return ResponseEntity.badRequest().body("Tipo de archivo no permitido.");
        }
        try {
            String imageUrl = imageService.storeImage(image);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen:" + e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chats/user/{userId}")
    public ResponseEntity<List<Chat>> getChatsByUser(@PathVariable String userId) {
        List<Chat> chats = chatService.getChatsByMemberId(userId);
        return ResponseEntity.ok(chats);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/chats/{chatId}/addUser")
    public ResponseEntity<Chat> addUserToChat(
            @PathVariable String chatId,
            @RequestBody String userId
    ) {
        return ResponseEntity.ok(chatService.addUserToChat(chatId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/chats/{chatId}/removeUser/{userId}")
    public ResponseEntity<Chat> removeUserFromChat(
            @PathVariable String chatId,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(chatService.removeUserFromChat(chatId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chats/{chatId}/members")
    public ResponseEntity<List<User>> getChatMembers(@PathVariable String chatId) {
        return ResponseEntity.ok(chatService.getChatMembers(chatId));
    }

    private boolean isMimeTypeAllowed(String mimeType) {
        List<String> allowedMimeTypesList = Arrays.asList(allowedMimeTypes);
        return allowedMimeTypesList.contains(mimeType);
    }
}
