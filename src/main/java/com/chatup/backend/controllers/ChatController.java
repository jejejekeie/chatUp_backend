package com.chatup.backend.controllers;

import com.chatup.backend.dtos.ChatPreviewDTO;
import com.chatup.backend.models.*;
import com.chatup.backend.service.ChatService;
import com.chatup.backend.service.FCMService;
//import com.chatup.backend.service.ImageService;
import com.chatup.backend.service.MensajeService;
import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeService messageService;
    private final ChatService chatService;
    //private final ImageService imageService;
    private final FCMService fcmService;
/*
    @Value("${file.upload-allowed-mimetypes}")
    private String[] allowedMimeTypes;
 */
    //@PreAuthorize("isAuthenticated()")
    @MessageMapping("/processMessage")
    public void processMessage(Mensaje chatMensaje) {
        Mensaje msjGuardado = messageService.save(chatMensaje);
        messagingTemplate.convertAndSendToUser(
                chatMensaje.getSender(), "/queue/messages",
                msjGuardado
        );
    }

    //@PreAuthorize("isAuthenticated()")
    @MessageMapping("processMessage/{chatId}")
    public void processMessage(@DestinationVariable String chatId, Mensaje chatMensaje) {
        Mensaje msjGuardado = messageService.save(chatMensaje);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, msjGuardado);

        NotificationRequest notificationRequest = new NotificationRequest();
        notificationRequest.setTitle("Nuevo mensaje" + chatId);
        notificationRequest.setBody(chatMensaje.getContent());
        notificationRequest.setTopic(chatId);
        notificationRequest.setToken(chatMensaje.getSender());

        try {
            fcmService.sendMessageToToken(notificationRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar notificación");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/messages/{chatId}/{page}/{size}")
    public ResponseEntity<List<Mensaje>> getMensajesChatPaginated(
            @PathVariable String chatId,
            @PathVariable int page,
            @PathVariable int size) {
        Page<Mensaje> mensajes = messageService.findMensajesChatPageable(chatId, page, size);
        return ResponseEntity.ok(mensajes.getContent());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<List<Mensaje>> getMensajesChat(
            @PathVariable String chatId
    ) {
        return ResponseEntity.ok(messageService.findMensajesChat(chatId));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/newChat")
    public ResponseEntity<?> createChat(@RequestBody ChatCreationRequest request) {
        if (!membersAreValid(request.getMembers())) {
            return ResponseEntity.badRequest().body("No se puede crear un chat con un solo miembro.");
        }

        Chat chat = Chat.builder()
                .chatId(UUID.randomUUID().toString())
                .name(request.getChatName())
                .members(request.getMembers())
                .chatType(request.getChatType())
                .build();

        Chat savedChat = chatService.createChat(chat);
        return ResponseEntity.ok(savedChat);
    }

    private boolean membersAreValid(Set<String> members) {
        return members != null && members.size() > 1;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("user/{userId}")
    public ResponseEntity<List<Chat>> getChatsByUser(@PathVariable String userId) {
        List<Chat> chats = chatService.getChatsByMemberId(userId);
        return ResponseEntity.ok(chats);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/previews")
    public ResponseEntity<List<ChatPreviewDTO>> getChatPreviews(@RequestParam String userId) {
        try {
            List<Chat> chats = chatService.getChatsByMemberId(userId);
            List<ChatPreviewDTO> chatPreviews = new ArrayList<>();

            for (Chat chat : chats) {
                Mensaje lastMessage = messageService.findLastMessageByChatId(chat.getId());
                ChatPreviewDTO chatPreview = new ChatPreviewDTO(
                        chat.getChatId(),
                        chat.getName(),
                        chat.getMembers(),
                        chat.getChatType(),
                        lastMessage
                );
                chatPreviews.add(chatPreview);
            }
            return ResponseEntity.ok(chatPreviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("findByName")
    public ResponseEntity<List<Chat>> findChatsByName(@RequestBody String chatName) {
        try {
            List<Chat> chats = chatService.findChatsByName(chatName);
            if (chats.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("{chatId}/addUser")
    public ResponseEntity<Chat> addUserToChat(
            @PathVariable String chatId,
            @RequestBody String userId
    ) {
        return ResponseEntity.ok(chatService.addUserToChat(chatId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("{chatId}/removeUser/{userId}")
    public ResponseEntity<Chat> removeUserFromChat(
            @PathVariable String chatId,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(chatService.removeUserFromChat(chatId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("{chatId}/members")
    public ResponseEntity<List<User>> getChatMembers(@PathVariable String chatId) {
        return ResponseEntity.ok(chatService.getChatMembers(chatId));
    }

    /*
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
 */
/*
    private boolean isMimeTypeAllowed(String mimeType) {
        List<String> allowedMimeTypesList = Arrays.asList(allowedMimeTypes);
        return allowedMimeTypesList.contains(mimeType);
    }
 */
}
