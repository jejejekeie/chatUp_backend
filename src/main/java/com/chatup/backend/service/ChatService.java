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

    public Optional<Chat> findChatByMembersAndName(Set<String> members, String chatName) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Members cannot be null or empty");
        }
        if (chatName == null || chatName.isEmpty()) {
            throw new IllegalArgumentException("Chat name cannot be null or empty");
        }
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

        if (chat.getMembers().contains(userId)) {
            throw new IllegalArgumentException("User already a member of the chat");
        }
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

    //region Get Chats
    public List<Chat> findChatsByName(String chatName) {
        if (chatName == null || chatName.isEmpty()) {
            throw new IllegalArgumentException("Chat name cannot be null or empty");
        }
        return chatRepository.findChatsByName(chatName);
    }

    @Cacheable(value = "chatsByMemberId", key = "#memberId")
    public List<Chat> getChatsByMemberId(String memberId) {
        if (memberId == null || memberId.isEmpty()) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
        return chatRepository.findChatsByMemberId(memberId);
    }

    public List<UserDTO> getChatMembers(String chatId) {
        if (chatId == null || chatId.isEmpty()) {
            throw new IllegalArgumentException("Chat ID cannot be null or empty");
        }

        Set<String> members = chatRepository.findChatByChatId(chatId).orElseThrow().getMembers();
        if (members.isEmpty()) {
            throw new IllegalArgumentException("No members found for the chat with ID: " + chatId);
        }

        List<User> users = userRepository.findUsersByIds(members);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("No users found for the members in the chat with ID: " + chatId);
        }

        return users.stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getFotoPerfil(), user.getStatus(), null)) // Assuming you have a suitable constructor
                .collect(Collectors.toList());
    }

    public List<ChatPreviewDTO> getChatPreviews(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        List<Chat> chats = chatRepository.findChatsByMemberId(userId);
        if (chats.isEmpty()) {
            throw new IllegalArgumentException("No chats found for the user with ID: " + userId);
        }

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
    //endregion

    //region Helpers
    public Chat getChatOrThrow(String chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));
    }
    //endregion
}
