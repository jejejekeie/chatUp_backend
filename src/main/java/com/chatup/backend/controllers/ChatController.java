package com.chatup.backend.controllers;

import com.chatup.backend.dtos.ChatPreviewDTO;
import com.chatup.backend.dtos.UserDTO;
import com.chatup.backend.models.*;
import com.chatup.backend.service.ChatService;
import com.chatup.backend.service.MensajeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeService messageService;
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    //region Process Message
    //@PreAuthorize("isAuthenticated()")
    @MessageMapping("/processMessage")
    public void processMessage(Mensaje chatMensaje) {
        logger.info("Received message: {}", chatMensaje);
        Mensaje msjGuardado = messageService.save(chatMensaje);
        logger.info("Saved message: {}", msjGuardado);
        messagingTemplate.convertAndSendToUser(
                chatMensaje.getSender(), "/queue/messages",
                msjGuardado
        );
        logger.info("Message sent to user {}: {}", chatMensaje.getSender(), msjGuardado);
    }

    //@PreAuthorize("isAuthenticated()")
    @MessageMapping("processMessage/{chatId}")
    public void processMessage(@DestinationVariable String chatId, Mensaje chatMensaje) {
        logger.info("Received message for chatId {}: {}", chatId, chatMensaje);
        chatMensaje.setChatId(chatId);
        logger.info("Saved message: {}", chatMensaje);
        Mensaje msjGuardado = messageService.save(chatMensaje);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, msjGuardado);
        logger.info("Message sent to topic /topic/chat/{}: {}", chatId, msjGuardado);
    }
    //endregion

    //region Get Messages
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
    //endregion

    //region Create/Delete Chat
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
    @DeleteMapping("/{chatId}")
    public ResponseEntity<?> deleteChat(@PathVariable String chatId) {
        try {
            chatService.deleteChat(chatId);
            return ResponseEntity.ok("Chat deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    //endregion

    //region Add/Remove User
    @PreAuthorize("isAuthenticated()")
    @PutMapping("{chatId}/addUser")
    public ResponseEntity<Chat> addUserToChat(
            @PathVariable String chatId,
            @RequestBody Map<String, String> user
    ) {
        String userId = user.get("userId");
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(chatService.addUserToChat(chatId, userId));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("{chatId}/removeUser/{userId}")
    public ResponseEntity<String> removeUserFromChat(
            @PathVariable String chatId,
            @PathVariable String userId
    ) {
        try {
            chatService.removeUserFromChat(chatId, userId);
            return ResponseEntity.ok("User removed sucessfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or chat not found");
        }
    }
    //endregion

    //region GetChat
    @PreAuthorize("isAuthenticated()")
    @GetMapping("user/{userId}")
    public ResponseEntity<List<Chat>> getChatsByUser(@PathVariable String userId) {
        List<Chat> chats = chatService.getChatsByMemberId(userId);
        return ResponseEntity.ok(chats);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/previews")
    public ResponseEntity<List<ChatPreviewDTO>> getChatPreviews(@RequestParam String userId) {
        List<ChatPreviewDTO> chatPreviews = chatService.getChatPreviews(userId);
        return ResponseEntity.ok(chatPreviews);
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
    @GetMapping("/{chatId}/details")
    public ResponseEntity<Chat> getChatDetails(@PathVariable String chatId) {
        Optional<Chat> chat = chatService.getChatById(chatId);
        return chat.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{chatId}/members")
    public ResponseEntity<List<UserDTO>> getChatMembers(@PathVariable String chatId) {
        List<UserDTO> members = chatService.getChatMembers(chatId);
        if (members != null) {
            return ResponseEntity.ok(members);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    //endregion
}
