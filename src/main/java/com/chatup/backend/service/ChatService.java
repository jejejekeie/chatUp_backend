package com.chatup.backend.service;

import com.chatup.backend.dtos.ChatPreviewDTO;
import com.chatup.backend.dtos.UserDTO;
import com.chatup.backend.models.Chat;
import com.chatup.backend.models.Mensaje;
import com.chatup.backend.models.User;
import com.chatup.backend.repositories.ChatRepository;
import com.chatup.backend.repositories.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MensajeService messageService;

    public ChatService(ChatRepository chatRepository, UserRepository userRepository, MensajeService messageService) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    //region Create/Delete Chat
    @CachePut(value = "chatsByMemberId", key = "#chat.members")
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }

    /*
    private String createChatId(Set<String> members) {
        List<String> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);
        return String.join("-", sortedMembers);
    }
    */

    public Optional<Chat> findChatByMembersAndName(Set<String> members, String chatName) {
        return chatRepository.findChatByMembersAndName(members, chatName);
    }

    public void deleteChat(String chatId) {
        Optional<Chat> chatOptional = chatRepository.findChatByChatId(chatId);
        if (chatOptional.isPresent()) {
            chatRepository.deleteByChatId(chatId);
        } else {
            throw new IllegalArgumentException("Chat with id " + chatId + " not found");
        }
    }
    //endregion

    //region Add/Remove Member
    public Chat addUserToChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        chat.getMembers().add(userId);
        return chatRepository.save(chat);
    }

    @CacheEvict(value = "chatsByMemberId", key = "#userId")
    public void removeUserFromChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatByChatId(chatId).orElseThrow();
        if (!chat.getMembers().contains(userId)) {
            throw new IllegalArgumentException("User " + userId + " not in chat " + chatId);
        }
        chat.getMembers().remove(userId);
        chatRepository.save(chat);
    }
    //endregion

    public List<UserDTO> getChatMembers(String chatId) {
        Set<String> members = chatRepository.findChatByChatId(chatId).orElseThrow().getMembers();
        List<User> users = userRepository.findUsersByIds(members);
        return users.stream()
                .map(user -> UserDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fotoPerfil(user.getFotoPerfil())
                        .status(user.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    //region Get Chats
    public List<Chat> findChatsByName(String chatName) {
        return chatRepository.findChatsByName(chatName);
    }

    @Cacheable(value = "chatsByMemberId", key = "#memberId")
    public List<Chat> getChatsByMemberId(String memberId) {
        return chatRepository.findChatsByMemberId(memberId);
    }

    //endregion

    public List<ChatPreviewDTO> getChatPreviews(String userId) {
        List<Chat> chats = chatRepository.findChatsByMemberId(userId);
        List<ChatPreviewDTO> chatPreviews = new ArrayList<>();
        for (Chat chat : chats) {
            Mensaje lastMessage = messageService.findLastMessageByChatId(chat.getChatId());
            chatPreviews.add(new ChatPreviewDTO(
                    chat.getChatId(),
                    chat.getName(),
                    chat.getChatType(),
                    lastMessage
            ));
        }
        return chatPreviews;
    }

    //region Helpers
    public Chat getChatOrThrow(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));
    }
    //endregion
}
