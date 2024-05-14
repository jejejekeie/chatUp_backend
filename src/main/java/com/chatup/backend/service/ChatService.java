package com.chatup.backend.service;

import com.chatup.backend.dtos.ChatPreviewDTO;
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

    @CachePut(value = "chatsByMemberId", key = "#chat.members")
    public Chat createChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public Optional<String> getChatId (
            Set<String> members,
            boolean createNewChatIfNotExists
    ) {
        return chatRepository
                .findChatByMembers(members)
                .map(Chat::getChatId)
                .or(() -> {
                    if (createNewChatIfNotExists) {
                        var chatId = createChatId(members);
                        return Optional.of(chatId);
                    }
                    return Optional.empty();
                });
    }

    private String createChatId(Set<String> members) {
        List<String> sortedMembers = new ArrayList<>(members);
        Collections.sort(sortedMembers);
        return String.join("-", sortedMembers);
    }

    public List<Chat> findChatsByName(String chatName) {
        return chatRepository.findChatsByName(chatName);
    }

    @Cacheable(value = "chatsByMemberId", key = "#memberId")
    public List<Chat> getChatsByMemberId(String memberId) {
        return chatRepository.findChatsByMemberId(memberId);
    }

    public List<User> getChatMembers(String chatId) {
        Set<String> members = chatRepository.findChatById(chatId).orElseThrow().getMembers();
        return userRepository.findUsersByEmail(members);
    }

    public Chat addUserToChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow(
                () -> new IllegalArgumentException("Chat not found" + chatId + " not found")
        );
        chat.getMembers().add(userId);
        return chatRepository.save(chat);
    }

    @CacheEvict(value = "chatsByMemberId", key = "#userId")
    public Chat removeUserFromChat(String chatId, String userId) {
        Chat chat = chatRepository.findChatById(chatId).orElseThrow();
        chat.getMembers().remove(userId);
        return chatRepository.save(chat);
    }

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

    public void deleteChat(String chatId) {
        chatRepository.deleteById(chatId);
    }

    public Optional<Chat> getChatById(String chatId) {
        return chatRepository.findChatById(chatId);
    }
}
